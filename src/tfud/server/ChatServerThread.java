package tfud.server;

import java.io.*;
import java.net.*;
import java.util.*;
import tfud.communication.*;
import tfud.events.EventType;
import tfud.parsers.*;
import tfud.pstorage.IStorageFacade;
import tfud.utils.ILogger;

/**
 * class ChatServerThread
 *
 * Implements handleConnection
 *
 * <br><i>more to come</i>
 *
 * @author BJR
 */
public class ChatServerThread extends ServerThread {

    final static int MINBUFFERSIZE = 50;					// minimum size of buffer 

    private String handle;							// users handle    
    private String chatRoom;							// chatroom name
    private String username, password;
    private final IParser tparser;
    private final ILogger logger;
    private int id;								// users ID 

    private ObjectInputStream input;
    private ObjectOutputStream output;

    protected int myaccesslevel;						// accesslevel can be 0=root, 1=super, 2=normal
    protected List<DataPackage> outputBuffer; 					// outputdata buffer - Vector
    protected ChatServer server;    						// reference to server instance
    private boolean banned;
    private final IStorageFacade facade;

    /**
     * Constructor
     *
     * @param	server	reference to server instance
     * @param	socket	socket to which this thread binds to
     * @param parser
     * @throws java.net.SocketException, IOException
     */
    public ChatServerThread(ChatServer server, Socket socket, IParser parser) throws SocketException, IOException {

        super(socket);

        id = server.getNextID();                                                // set this threads ID
        logger = server.getLogger();                                            // 
        facade = server.getFacade();
        outputBuffer = new ArrayList<>(MINBUFFERSIZE);                          // initial size    

        myaccesslevel = 2;                                                      // default accesslevel

        chatRoom = "Start";                                                     // Start chatroom = default chatroom
        handle = "";

        tparser = parser;                                                       // assign Parser

        input = new ObjectInputStream(in);                                      // sets streams
        output = new ObjectOutputStream(out);

        this.server = server;                                                   // REFERENCE TO MAIN SERVER 

        start();                                                                // starts thread
    }

    /**
     * Method getID	returns the ID of this thread
     *
     * @return int id if thread
     */
    protected int getID() {
        return id;
    }

    /**
     * Method getHandle returns the handle of the client who's bound to this
     * thread
     *
     * @return string handle
     */
    protected String getHandle() {
        return handle;
    }

    /**
     * Method getChatRoom This thread can be in specific chatrooms - "start" is
     * the default chatroom.
     *
     * @return string name of chatroom
     */
    protected String getChatRoom() {
        return chatRoom;
    }

    /**
     * Method setChatRoom
     *
     * @param	room	the name of the chatroom as string
     */
    protected void setChatRoom(String room) {
        this.chatRoom = room;
    }

    /**
     * Method getAccessLevel
     *
     * @return int accesslevel	- the accesslevel is set to 2 as default
     */
    protected int getAccessLevel() {
        return myaccesslevel;
    }

    /**
     * This method is used to set a DataPackage to the current Thread's output
     * buffer
     *
     * @param data Datapackage
     *
     */
    protected synchronized void setDataPackage(DataPackage data) {
        debug("Sending package: " + data);
        outputBuffer.add(data);
    }

    protected synchronized int getOutputBufferSize() {
        return outputBuffer.size();
    }

    protected synchronized DataPackage getNextMessage() {
        return outputBuffer.remove(0);
    }

    private void debug(String msg) {
        logger.log(id + " " + msg);
    }

    private void info(String msg) {
        logger.log(id + " " + msg);
    }

    @Override
    protected void initiateConnection() {
        info(" ** Connection from: " + hostaddress);
        DataPackage initialData = null;
        try {
            /**
             *
             * PROTOCOL: 1) Reads Clients first datapackage as a ONLINE event 2)
             * Check users credentials an send an event to client if fails 3) if
             * ok send LOGIN event to client 4) send a USERLIST immediatly
             */

            initialData = (DataPackage) input.readObject();                            // 1) read clients ONLINE event    

            this.handle = initialData.getHandle();

            /**
             * Check for users credentials Read first data - is array of objects
             * in this case Strings
             */
            Object[] ret = (Object[]) initialData.getData();
            username = ret[0].toString();
            password = ret[1].toString();

            info("User '" + username + "' is logging in.. ");

            /**
             * Check DB for USER TODO: refactor to not talk directly to facade
             * etc.
             */
            int access = facade.checkLogin(username, password);

            switch (access) {
                case 0:
                    // if 0 failed to login
                    output.writeObject(new DataPackage(this.id, 0, this.handle, EventType.LOGIN, "0"));
                    throw new Exception("User (" + username + "/" + password + ") not found");
                case -1:
                    // if -1 the user has been banned
                    output.writeObject(new DataPackage(this.id, 0, this.handle, EventType.SERVERMESSAGE, "You have been banned"));
                    banned = true;
                    throw new Exception("User (" + username + "/" + password + ") is banned..");
            }

            /**
             * NB NB NB
             */
            DataPackage initialResponseData = new DataPackage();
            initialResponseData.setID(access);

            myaccesslevel = facade.getAccessLevel(handle);

            /**
             * Update storage with the users online status
             */
            facade.updateOnlineStatus(handle, 1);

            /**
             * 3) send logged in to client
             */
            setDataPackage(new DataPackage(access, 0, handle, EventType.LOGIN, access));

            /**
             * 4) send Userlist to client
             */
            setDataPackage(new DataPackage(access, 0, handle, EventType.USERLIST, getOnlineUsers(chatRoom)));

            /**
             * Relay message to other threads
             */
            relayMessage(this, initialResponseData);

            /* END INIT */
        } catch (Exception e) {
            // TODO: handle error 
            facade.log("INIT:  " + e.getMessage());           // 
        }

    }

    /**
     * Overridden from ServerThread Handles the connection to between this
     * thread and the client
     *
     * The client sends username and password, which is checked
     *
     * Runs a infinite loop untill some exception occurs - i.e the client
     * closing the connection
     *
     * If the connection is broken - the thread frees used variables and finally
     * removes itself from the collection of threads in the server.
     */
    @Override
    public void handleConnection() {

        try {

            /**
             * BEGIN WHILE LOOP Keeps connection alive untill some error or
             * client disconnects
             *
             */
            while (true) {
                sendToClient();
                readFromClient();
            }

        } catch (IOException io) {
            debug("Error ChatServerThread: IO \n" + io.getMessage());
            io.getStackTrace();
        } catch (ClassNotFoundException e) {
            debug("Error ChatServerThread: Class not found exception - " + e.getMessage());
            e.getStackTrace();
        } finally {
            // TODO:
        }
    }


    private boolean readFromClient() throws IOException, ClassNotFoundException {
        debug("(Read) waiting for client ... ");
        DataPackage inputData = (DataPackage) input.readObject();

        EventType type = inputData.getEventType();

        debug("(Read) relaying event: " + type.toString());

        switch (type) {
            case DUMMY: {
                // Do nothing.. 
                setDataPackage(new DataPackage());
            }
            break;
            default: {
                /**
                 * Can cause null exception if connection is closed relays
                 * client message to all other threads must be reimplemented to
                 * handle chatroom etc.
                 */
                debug(inputData.toString());

                Object clientMessageData = inputData.getData();                 // read data
                if (clientMessageData == null) {
                    return false;                                               // if data is null ignore and return
                }
                
                debug("(Read) data received ... ");

                String clientMessageDataText = clientMessageData.toString();
                /**
                 * Handle Server Commands
                 *
                 */
                if (clientMessageDataText.length() > 3) {
                    String command = clientMessageDataText.substring(0, 2);
                    if (command.equals("//")) {
                        DataPackage response = handleCommand(clientMessageDataText, this);
                        setDataPackage(response);
                        return false;                                           // if server command no need to parse text

                    }
                }
                
                debug("(Read) command handled ... ");

                /**
                 * PARSE THE TEXT FOR SMILEYS, EMOTICONS AND STUFF
                 *
                 */
                clientMessageDataText = tparser.parseText(clientMessageDataText);

                inputData.setData(clientMessageDataText);

                relayMessage(this, inputData);
            }
        }

        return true;
    }

    private void sendToClient() throws IOException {
        /**
         * WRITE outBuffer
         */
        if (getOutputBufferSize() > 0) {
            debug("Ouputbuffer size: " + getOutputBufferSize());
            while (getOutputBufferSize() > 0) {
                output.writeObject(getNextMessage());                            // get first element from buffer
            }
        }

    }

    @Override
    protected void closeConnection() {
        /**
         * Do some cleaning
         */
        try {

            remove(this);						// remove this thread from container

            /**
             * relays offline message to all remaining threads
             *
             */
            if (!banned) {
                relayMessage(
                        this,
                        new DataPackage(
                                id,
                                0,
                                handle,
                                EventType.OFFLINE,
                                "Offline"
                        )
                );
                facade.updateOnlineStatus(handle, 0);
            }

            input.close();							// Close streams           	
            output.close();

        } catch (IOException io) {
            debug("Error IO closing IOException: " + io.getMessage());
        } finally {
            // clear buffer
            outputBuffer.clear();
            outputBuffer = null;
            input = null;
            output = null;
            logger.log(" *** ChatServerThread for client '" + handle + "' at address: '" + hostaddress + "' stopped..");
        }
    }

    /**
     *
     * @param serverThread
     * @param command
     * @param handle
     * @throws InterruptedException
     */
    protected void respondToServerThread(ServerThread serverThread, String command, String handle) throws InterruptedException {
        String response = "";
        if (command.equals("//kick")) {
            response = "KICK";
        }
        if (command.equals("//ban")) {
            response = "BAN";
            facade.banUser(handle);
        }

        DataPackage temp = new DataPackage(getID(),
                getID(),
                getHandle(),
                EventType.SERVERMESSAGE,
                response);
        setDataPackage(temp);
    }

    @Override
    public String toString() {
        return this.id + "; [" + this.handle + "]";
    }

    private void relayMessage(ChatServerThread aThis, DataPackage dataPackage) {
        server.relayMessage(aThis, dataPackage);
    }

    private void remove(ChatServerThread aThis) {
        server.remove(aThis);
    }

    private DataPackage handleCommand(String clientMessageDataText, ChatServerThread aThis) {
        return server.handleCommand(clientMessageDataText, aThis);
    }

    private Object getOnlineUsers(String chatRoom) {
        return server.getOnlineUsers(chatRoom);
    }

}
