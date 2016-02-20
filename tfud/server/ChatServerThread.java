package tfud.server;

import tfud.communication.*;
import tfud.parsers.*;

import java.io.*;
import java.net.*;
import java.util.*;

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
    private final TextParser tparser;
    private int id;											// users ID 

    private ObjectInputStream in;
    private ObjectOutputStream out;

    protected int myaccesslevel;							// accesslevel can be 0=root, 1=super, 2=normal
    protected Vector outBuffer; 							// outputdata buffer - Vector
    protected ChatServer server;    						// reference to server instance

    /**
     * Constructor
     *
     * @param	server	reference to server instance
     * @param	socket	socket to which this thread binds to
     */
    public ChatServerThread(Server server, Socket socket) throws SocketException, IOException {

        super(socket);

        this.server = (ChatServer) server;   			// REFERENCE TO MAIN SERVER 
        this.id = this.server.getNextID();			// set this threads ID

        this.outBuffer = new Vector(MINBUFFERSIZE); 		// initial size    

        myaccesslevel = 2;					// default accesslevel

        this.chatRoom = "Start";				// Start chatroom = default chatroom
        this.handle = "";

        tparser = new TextParser("replace.xml");   		// create Parser

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
    public void handleConnection() {

        boolean banned = false;

        try {

            System.out.println(" ** Connection from: " + hostaddress);

            Object res = null;
            DataPackage data;								// DataPackage reference
            DataPackage d;									// DataPackage reference

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

            try {
                /* Read first data - is array of objects in this case Strings */
                Object[] ret = (Object[]) data.getData();
                String username = ret[0].toString();
                String password = ret[1].toString();
                /* Check DB for USER */
                System.out.println("User " + username + " is logging in.. ");

                // violation of "dont talk to strangers"-pattern
                returnValue = server.facade.checkLogin(username, password);

                if (returnValue == 0) {
                    out.writeObject(new DataPackage(this.id, 0, this.handle, "Login", "0"));
                    throw new Exception("User (" + username + "/" + password + ") not found");
                } else if (returnValue == -1) {
                    out.writeObject(new DataPackage(this.id, 0, this.handle, "ServerMessage", "You have been banned"));
                    banned = true;
                    throw new Exception("User (" + username + "/" + password + ") is banned..");
                }
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }

            this.id = returnValue;

            /* NB NB NB */
            data.setID(this.id);

            myaccesslevel = server.facade.getAccessLevel(this.handle);					// set myaccesslevel

            /* beskriv */
            server.facade.updateOnlineStatus(this.handle, 1);

            /* Write We've logged in */
            out.writeObject(new DataPackage(this.id, 0, this.handle, "Login", returnValue + ""));

            /* Write Userlist */
            out.writeObject(new DataPackage(this.id, 0, this.handle, "UserList", server.getOnlineUsers(this.chatRoom)));

            server.relayMessage(data, this.chatRoom, this.hostaddress);		// relays message

            //server.facade.log(data, this.hostaddress);  		
            /* END INIT */
            /**
             * BEGIN WHILE LOOP Keeps connection alive untill some error or
             * client disconnects 
  			 *
             */
            while (true) {

                res = null;										// set res to null per loop

                textData = "";

                /* THIS IS DEPRECATED - TOO MUCH MEMORY WASTED FOR INSTANTIATING OBJECT */
 /*data = new DataPackage();
  				
  				data.setID(this.id);
  				data.setTargetID(0);
  				data.setHandle(this.handle);
  				data.setEventType("Message");
  				data.setData("");
                 */
 /* 
  				 *	empty outBuffer   				 	
  				 **/
                if (outBuffer.size() > 0) {

                    data = (DataPackage) outBuffer.remove(0);	// get first element from buffer

                    out.writeObject(data);
                    data = null;
                } else {
                    // if buffer length == 0
                    out.writeObject(null);						// writes null value 
                    // for optimizing network and memory usage	

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
                            isCommand = "";
                            if (server.handleCommand(textData, this)) {
                                continue;	  					// if server command no need to parse text
                            }
                        }

                    }
                    // PARSE THE TEXT FOR SMILEYS, EMOTICONS AND STUFF
                    textData = tparser.parseText(textData);

                    d.setData(textData);

                    if (d.getData() != null && !clientdata.equals("")) {

                        String type = d.getEventType();
                        if (type.equals("PrivateMessage")) {

                            setDataPackage(d);

                            server.relayMessage(d, this.chatRoom, this.hostaddress);

                        } else if (type.equals("ChangeRoom")) {
                            /* If changeroom requested */

 /* Notify others that we're changing room */
                            server.relayMessage(new DataPackage(this.id, 0, this.handle, "ChangeRoom", "Leave"), this.chatRoom, this.hostaddress);

                            d.setID(this.id);

                            this.chatRoom = clientdata;			// which chatroom requested ?

                            /* send the userlist for the new chatroom to the client */
                            setDataPackage(new DataPackage(this.id, 0, this.handle, "UserList", server.getOnlineUsers(this.chatRoom)));

                            /* notify other clients that We have arrived  */
                            server.relayMessage(new DataPackage(this.id, 0, this.handle, "ChangeRoom", "Arrive"), this.chatRoom, this.hostaddress);

                        } else {
                            /* This is a normal message - thus normal relaying */

                            server.relayMessage(d, this.chatRoom, this.hostaddress);
                        }

                    }

                    d = null;	// clean up	

                }

                //Thread.sleep(250);	// put to sleep for about 250 milliseconds
            }

        } catch (IOException io) {
            System.out.println("Error ChatServerThread: IO \n" + io.getMessage());
        } //io.printStackTrace();
        catch (Exception e) {
            System.out.println("Error ChatServerThread: General Exception " + e.getMessage());
        } // e.printStackTrace(); 
        finally {
            /* Do some cleaning */
            try {

                server.remove(this);							// remove this thread from container

                // relays offline message to all remaining threads
                if (!banned) {
                    server.relayMessage(new DataPackage(this.id, 0, this.handle, "Offline", "Offline"), this.chatRoom, this.hostaddress);
                    server.facade.updateOnlineStatus(this.handle, 0);
                }

                in.close();														// Close streams           	
                out.close();

            } catch (IOException io) {
                System.out.println("Error IO closing IOException: " + io.getMessage());
            } catch (Exception e) {
                System.out.println("Error IO closing Exception: " + e.getMessage());
            } finally {

                // clear buffer
                outBuffer.clear();
                outBuffer = null;
                in = null;
                out = null;

                //System.gc();			// runs garbage collector ?
                System.out.println("ChatServerThread for client '" + handle + "' at address: '" + hostaddress + "' stopped..");
            }

        }
    }

    public String toString() {
        return this.id + "; [" + this.handle + "]";
    }
}
