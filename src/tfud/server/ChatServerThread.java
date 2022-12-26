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

    private String handle;									// users handle    
    private String chatRoom;								// chatroom name
    private String username, password;
    private final IParser tparser;
    private int id;											// users ID 

    private ObjectInputStream in;
    private ObjectOutputStream out;

    protected int myaccesslevel;							// accesslevel can be 0=root, 1=super, 2=normal
    protected Vector outBuffer; 							// outputdata buffer - Vector
    protected ChatServer server;    						// reference to server instance
    private final ILogger logger;

    /**
     * Constructor
     *
     * @param	server	reference to server instance
     * @param	socket	socket to which this thread binds to
     * @throws java.net.SocketException
     */
    public ChatServerThread(Server server, Socket socket, IParser parser) throws SocketException, IOException {

        super(socket);

        this.server = (ChatServer) server;   			// REFERENCE TO MAIN SERVER 
        this.id = this.server.getNextID();			// set this threads ID
        this.logger = this.server.logger;
        this.outBuffer = new Vector(MINBUFFERSIZE); 		// initial size    

        myaccesslevel = 2;					// default accesslevel

        this.chatRoom = "Start";				// Start chatroom = default chatroom
        this.handle = "";

        tparser = parser; //= new TextParser("replace.xml");   		// create Parser

        in = new ObjectInputStream(input);			// sets streams
        out = new ObjectOutputStream(output);

        start(); 						// starts thread
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
     * This adds message to buffer. Messages send from other threads, and is
     * called from Server instance relayMessage method (which is synchronized -
     * thus this should not be synchronized ?)
     *
     * @param	msg	string this is the message
     * @throws java.lang.InterruptedException
     */
    protected synchronized void setMessage(String msg) throws InterruptedException {

        outBuffer.addElement(msg);

    }

    /**
     * This method is used to set a DataPackage to the current Thread's
     * outputbuffer
     *
     * @param pkg	Datapackage
     * @throws java.lang.InterruptedException
     *
     */
    protected synchronized void setDataPackage(tfud.communication.DataPackage pkg) throws InterruptedException {

        // Adds string to buffer
        outBuffer.addElement(pkg);

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

        boolean banned = false;

        try {

            logger.log(" ** Connection from: " + hostaddress);

            Object res;
            DataPackage data;								// DataPackage reference
            DataPackage d;								// DataPackage reference

            String textData;
            String isCommand;

            /**
             *
             * NOTICE !!! Reads Client first datapacket
             *
             *
             */
            res = in.readObject();
            data = (DataPackage) res;

            this.handle = data.getHandle();

            /* Check for users credentials */
            int returnValue = 0;													// returnValue >0 == users id

            /* Read first data - is array of objects in this case Strings */
            Object[] ret = (Object[]) data.getData();
            String username = ret[0].toString();
            String password = ret[1].toString();
            /* Check DB for USER */
            logger.log("User " + username + " is logging in.. ");

            // violation of "dont talk to strangers"-pattern
            returnValue = server.facade.checkLogin(username, password);

            if (returnValue == 0) {
                out.writeObject(new DataPackage(this.id, 0, this.handle, EventType.LOGIN, "0"));
                throw new Exception("User (" + username + "/" + password + ") not found");
            } else if (returnValue == -1) {
                out.writeObject(new DataPackage(this.id, 0, this.handle, EventType.SERVERMESSAGE, "You have been banned"));
                banned = true;
                throw new Exception("User (" + username + "/" + password + ") is banned..");
            }

            this.id = returnValue;

            /* NB NB NB */
            data.setID(this.id);

            myaccesslevel = server.facade.getAccessLevel(this.handle);					// set myaccesslevel

            /* beskriv */
            server.facade.updateOnlineStatus(this.handle, 1);

            /* Write We've logged in */
            out.writeObject(new DataPackage(this.id, 0, this.handle, EventType.LOGIN, returnValue + ""));

            /* Write Userlist */
            out.writeObject(new DataPackage(this.id, 0, this.handle, EventType.USERLIST, server.getOnlineUsers(this.chatRoom)));

            server.relayMessage(data, this.chatRoom, this.hostaddress);		// relays message

            //server.facade.log(data, this.hostaddress);  		
            /* END INIT */
            /**
             * BEGIN WHILE LOOP Keeps connection alive untill some error or
             * client disconnects
             *
             */
            while (true) {

                textData = "";

                /**
                *   WRITE outBuffer   				 	
                */
                if (outBuffer.size() > 0) {

                    data = (DataPackage) outBuffer.remove(0);                           // get first element from buffer

                    out.writeObject(data);
                } else {
                    out.writeObject(null);						// writes null value 
                }

                res = in.readObject();							// read data from client 

                // can cause null exception
                // if connection is closed
                // relays client message to all other threads
                // must be reimplemented to handle chatroom etc.
                if (res instanceof DataPackage) {

                    d = (DataPackage) res;
                    String clientdata = d.getData().toString();
                    if (clientdata.length() > 0) {
                        textData = clientdata + "";
                    }

                    /**
                     * Handle Server Commands
                     *
                     */
                    if (textData.length() > 3) {

                        isCommand = textData.substring(0, 2);

                        if (isCommand.equals("//")) {
                            if (server.handleCommand(textData, this)) {
                                continue;	  					// if server command no need to parse text
                            }
                        }

                    }
                    // PARSE THE TEXT FOR SMILEYS, EMOTICONS AND STUFF
                    textData = tparser.parseText(textData);

                    d.setData(textData);

                    if (d.getData() != null && !clientdata.equals("")) {

                        EventType type = d.getEventType();

                        switch (type) {
                            case PRIVATEMESSAGE:
                                setDataPackage(d);
                                server.relayMessage(d, this.chatRoom, this.hostaddress);
                                break;
                            case CHANGEROOM:
                                /* If changeroom requested */

                                /* Notify others that we're changing room */
                                server.relayMessage(new DataPackage(this.id, 0, this.handle, EventType.CHANGEROOM, "Leave"), this.chatRoom, this.hostaddress);
                                d.setID(this.id);
                                this.chatRoom = clientdata;			// which chatroom requested ?
                                /* send the userlist for the new chatroom to the client */
                                setDataPackage(new DataPackage(this.id, 0, this.handle, EventType.USERLIST, server.getOnlineUsers(this.chatRoom)));
                                /* notify other clients that We have arrived  */
                                server.relayMessage(new DataPackage(this.id, 0, this.handle, EventType.CHANGEROOM, "Arrive"), this.chatRoom, this.hostaddress);
                                break;
                            default:
                                /* This is a normal message - thus normal relaying */
                                server.relayMessage(d, this.chatRoom, this.hostaddress);
                                break;
                        }

                    }

                }
                logger.log(res);
            }

        } catch (IOException io) {
            logger.log("Error ChatServerThread: IO \n" + io.getMessage());
        } catch (Exception e) {
            logger.log("Error ChatServerThread: General Exception " + e.getMessage());
            e.getStackTrace();
        } finally {
            /* Do some cleaning */
            try {

                server.remove(this);							// remove this thread from container

                // relays offline message to all remaining threads
                if (!banned) {
                    server.relayMessage(new DataPackage(this.id, 0, this.handle, EventType.OFFLINE, "Offline"), this.chatRoom, this.hostaddress);
                    server.facade.updateOnlineStatus(this.handle, 0);
                }

                in.close();														// Close streams           	
                out.close();

            } catch (IOException io) {
                logger.log("Error IO closing IOException: " + io.getMessage());
            } catch (Exception e) {
                logger.log("Error IO closing Exception: " + e.getMessage());
            } finally {

                // clear buffer
                outBuffer.clear();
                outBuffer = null;
                in = null;
                out = null;

                //System.gc();			// runs garbage collector ?
                logger.log(" *** ChatServerThread for client '" + handle + "' at address: '" + hostaddress + "' stopped..");
            }

        }
    }

    /**
     *
     * @param serverthrean
     * @param command
     * @param handle
     * @throws InterruptedException
     */
    public void respondToServerThread(ServerThread serverthrean, String command, String handle) throws InterruptedException {
        String response = "";
        if (command.equals("//kick")) {
            response = "KICK";
        }
        if (command.equals("//ban")) {
            response = "BAN";
            server.facade.banUser(handle);
        }

        /*for(int i=1;i<args.length;i++)
                                        response[i] = args[i];	// set ....
         */
        tfud.communication.DataPackage temp = new tfud.communication.DataPackage(getID(), getID(), getHandle(), EventType.SERVERMESSAGE, response);
        setDataPackage(temp);
    }

    @Override
    public String toString() {
        return this.id + "; [" + this.handle + "]";
    }

}
