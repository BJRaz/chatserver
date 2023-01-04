package tfud.server;

import tfud.events.EventType;
import tfud.communication.*;
import tfud.parsers.*;

import java.io.*;
import java.net.*;
import java.util.*;
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
    protected List<DataPackage> outBuffer; 					// outputdata buffer - Vector
    protected ChatServer server;    						// reference to server instance
    private boolean banned;

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
        outBuffer = new ArrayList<>(MINBUFFERSIZE);                             // initial size    

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
     * @throws java.lang.InterruptedException
     *
     */
    protected synchronized void setDataPackage(DataPackage data) throws InterruptedException {
        outBuffer.add(data);
    }

    Object res;
    DataPackage data;								// DataPackage reference

    @Override
    protected void initiateConnection() {
        logger.log(" ** Connection from: " + hostaddress);

        try {
            /**
             *
             * PROTOCOL: 1) Reads Clients first datapackage as a ONLINE event 2)
             * Check users credentials an send an event to client if fails 3) if
             * ok send LOGIN event to client 4) send a USERLIST immediatly
             */

            data = (DataPackage) input.readObject();                            // 1) read clients ONLINE event    

            this.handle = data.getHandle();

            /**
             * Check for users credentials Read first data - is array of objects
             * in this case Strings
             */
            Object[] ret = (Object[]) data.getData();
            username = ret[0].toString();
            password = ret[1].toString();

            logger.log("User " + username + " is logging in.. ");

            /**
             * Check DB for USER TODO: refactor to not talk directly to facade
             * etc.
             */
            id = server.getFacade().checkLogin(username, password);

            switch (id) {
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
            data.setID(id);

            myaccesslevel = server.getFacade().getAccessLevel(this.handle);		// get accesslevel

            /**
             * Update storage with the users online status
             */
            server.getFacade().updateOnlineStatus(handle, 1);

            /**
             * send We've logged in to client
             */
            output.writeObject(new DataPackage(id, 0, handle, EventType.LOGIN, id));

            /**
             * send Userlist to client
             */
            output.writeObject(new DataPackage(id, 0, handle, EventType.USERLIST, server.getOnlineUsers(chatRoom)));

            server.relayMessage(data, chatRoom, hostaddress);                   // relays message to other serverthreads

            server.getFacade().log(data, hostaddress);                               // 
            /* END INIT */
        } catch (Exception e) {
            // TODO: handle error 
            server.getFacade().log(data, "INIT:  " + e.getMessage());                // 
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

                if (readFromClient()) {
                    continue;
                }

//                logger.log(res);
            }

        } catch (IOException io) {
            logger.log("Error ChatServerThread: IO \n" + io.getMessage());
        } catch (ClassNotFoundException | InterruptedException e) {
            logger.log("Error ChatServerThread: General Exception " + e.getMessage());
            e.getStackTrace();
        } finally {
            // TODO:
        }
    }

    private boolean readFromClient() throws InterruptedException, IOException, ClassNotFoundException {
        String textData = "";
        String command;
        data = (DataPackage) input.readObject();			// read data from client 
        /**
         * Can cause null exception if connection is closed relays client
         * message to all other threads must be reimplemented to handle chatroom
         * etc.
         */
        server.getLogger().log(data);
        
        textData = data.getData().toString();
        
        /**
         * Handle Server Commands
         *
         */
        if (textData.length() > 3) {
            command = textData.substring(0, 2);
            if (command.equals("//")) {
                if (server.handleCommand(textData, this)) {
                    return true; // if server command no need to parse text
                }
            }
        }
        /**
         * PARSE THE TEXT FOR SMILEYS, EMOTICONS AND STUFF
         *
         */
        textData = tparser.parseText(textData);
        
        data.setData(textData);
        
        if (data.getData() != null && !textData.isEmpty()) {

            EventType type = data.getEventType();

            switch (type) {
                case PRIVATEMESSAGE:
                    setDataPackage(data);
                    server.relayMessage(data, chatRoom, hostaddress);
                    break;
                case CHANGEROOM:
                    /**
                     * If changeroom requested
                     */

                    /**
                     * Notify others that we're changing room
                     */
                    server.relayMessage(
                            new DataPackage(
                                    id,
                                    0,
                                    handle,
                                    EventType.CHANGEROOM,
                                    "Leave"),
                            chatRoom,
                            hostaddress);
                    data.setID(this.id);
                    chatRoom = textData;                          // which chatroom requested ?
                    /**
                     * send the userlist for the new chatroom to the client
                     */
                    setDataPackage(
                            new DataPackage(
                                    id,
                                    0,
                                    handle,
                                    EventType.USERLIST,
                                    server.getOnlineUsers(chatRoom)
                            )
                    );
                    /* notify other clients that We have arrived  */
                    server.relayMessage(
                            new DataPackage(
                                    id,
                                    0,
                                    handle,
                                    EventType.CHANGEROOM,
                                    "Arrive"
                            ),
                            chatRoom,
                            hostaddress
                    );
                    break;
                default:
                    /**
                     * This is a normal message - thus normal relaying
                     */
                    server.relayMessage(data, chatRoom, hostaddress);
                    break;
            }

        }
        return false;
    }

    private void sendToClient() throws IOException {
        /**
         * WRITE outBuffer
         */
        if (outBuffer.size() > 0) {
            output.writeObject(outBuffer.remove(0));                            // get first element from buffer
        } else {
            output.writeObject(new DataPackage());                              // writes null value
        }
    }

    @Override
    protected void closeConnection() {
        /**
         * Do some cleaning
         */
        try {

            server.remove(this);						// remove this thread from container

            /**
             * relays offline message to all remaining threads
             *
             */
            if (!banned) {
                server.relayMessage(
                        new DataPackage(
                                id,
                                0,
                                handle,
                                EventType.OFFLINE,
                                "Offline"
                        ),
                        chatRoom,
                        hostaddress
                );
                server.getFacade().updateOnlineStatus(handle, 0);
            }

            input.close();							// Close streams           	
            output.close();

        } catch (IOException io) {
            logger.log("Error IO closing IOException: " + io.getMessage());
        } catch (InterruptedException e) {
            logger.log("Error IO closing Exception: " + e.getMessage());
        } finally {
            // clear buffer
            outBuffer.clear();
            outBuffer = null;
            input = null;
            output = null;
            logger.log(" *** ChatServerThread for client '" + handle + "' at address: '" + hostaddress + "' stopped..");
        }
    }

    /**
     *
     * @param serverthrean
     * @param command
     * @param handle
     * @throws InterruptedException
     */
    protected void respondToServerThread(ServerThread serverthrean, String command, String handle) throws InterruptedException {
        String response = "";
        if (command.equals("//kick")) {
            response = "KICK";
        }
        if (command.equals("//ban")) {
            response = "BAN";
            server.getFacade().banUser(handle);
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

}
