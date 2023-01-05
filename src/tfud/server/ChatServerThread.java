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
    protected synchronized void setDataPackage(DataPackage data) {
        writeToLog("Sending package: " + data);
        outBuffer.add(data);
    }
    
    protected synchronized int getOutBufferSize() {
        return outBuffer.size();
    }

    protected synchronized DataPackage getNextMessage() {
        return outBuffer.remove(0);
    }
    
    private void writeToLog(String msg) {
        logger.log(id + " " + msg);
    }
    
    Object res;
    DataPackage data;								// DataPackage reference

    @Override
    protected void initiateConnection() {
        writeToLog(" ** Connection from: " + hostaddress);

        try {
            /**
             *
             * PROTOCOL: 
             * 1)   Reads Clients first datapackage as a ONLINE event 
             * 2)   Check users credentials an send an event to client if fails 
             * 3)   if ok send LOGIN event to client 
             * 4)   send a USERLIST immediatly
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

            writeToLog("User '" + username + "' is logging in.. ");

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

            myaccesslevel = server.getFacade().getAccessLevel(handle);		

            /**
             * Update storage with the users online status
             */
            server.getFacade().updateOnlineStatus(handle, 1);

            /**
             * 3) send logged in to client
             */
            setDataPackage(new DataPackage(id, 0, handle, EventType.LOGIN, id));        

            /**
             * 4) send Userlist to client
             */
            setDataPackage(new DataPackage(id, 0, handle, EventType.USERLIST, server.getOnlineUsers(chatRoom)));

            /**
             * Relay message to other threads
             */
            server.relayMessage(data, chatRoom, hostaddress);                   

            
            /* END INIT */
        } catch (Exception e) {
            // TODO: handle error 
            server.getFacade().log(data, "INIT:  " + e.getMessage());           // 
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
            }

        } catch (IOException io) {
            writeToLog("Error ChatServerThread: IO \n" + io.getMessage());
            io.getStackTrace();
        } catch (ClassNotFoundException | InterruptedException e) {
            writeToLog("Error ChatServerThread: General Exception " + e.getMessage());
            e.getStackTrace();
        } finally {
            // TODO:
        }
    }

    private boolean readFromClient() throws InterruptedException, IOException, ClassNotFoundException {
        writeToLog("(Read) waiting for client ... ");
        String textData = "";
        String command;
        data = (DataPackage) input.readObject();			// read data from client 
        /**
         * Can cause null exception if connection is closed relays client
         * message to all other threads must be reimplemented to handle chatroom
         * etc.
         */
        writeToLog(data.toString());
        
        textData = data.getData().toString();
        
        writeToLog("(Read) data received ... ");
        
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
        writeToLog("(Read) command handled ... ");
        /**
         * PARSE THE TEXT FOR SMILEYS, EMOTICONS AND STUFF
         *
         */
        textData = tparser.parseText(textData);
        
        data.setData(textData);
        
        if (data.getData() != null && !textData.isEmpty()) {

            EventType type = data.getEventType();

            writeToLog("(Read) relaying event: " + type.toString());
            
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
        writeToLog("(Read) done ... ");
        return false;
    }

    private void sendToClient() throws IOException {
        /**
         * WRITE outBuffer
         */
        if (getOutBufferSize() > 0) {
            while(getOutBufferSize() > 0)
                output.writeObject(getNextMessage());                            // get first element from buffer
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
            writeToLog("Error IO closing IOException: " + io.getMessage());
        } catch (InterruptedException e) {
            writeToLog("Error IO closing Exception: " + e.getMessage());
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
