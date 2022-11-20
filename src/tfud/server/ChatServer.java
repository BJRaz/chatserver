package tfud.server;

import java.util.*;
import java.io.*;
import tfud.pstorage.IStorageFacade;
import tfud.utils.ILogger;

/**
 *
 * class ChatServer
 *
 * Implements execute
 *
 * Fig 1:
 *
 *
 */
public class ChatServer extends Server {

    private Date startup;
    private int id;
    final ILogger logger;

    /**
     *
     */
    protected IStorageFacade facade;		// facade to persistent storage	

    /**
     * Defaults to port 8900
     */
    //protected static int port = 8900;
    protected static String path = "";

    /**
     * Method ChatServer
     *
     * @param logger
     * @throws IOException
     * @param port int sets the port number on which the server will listens for
     * connections - defaults to port 8900
     */
    public ChatServer(int port, ILogger logger, IStorageFacade facade) throws IOException {
        // TODO: Add your code here
        super(port);
        this.logger = logger;
        serverContainer = new Vector(MINSERVERTHREADS);			// set initial size and incremental size
        // ID's - counts up pr new ServerThread
        id = 0;
        startup = new Date();
        this.facade = facade;
    }

    /**
     * Method getNextID Increments ID and returns it
     *
     * @return int
     */
    protected synchronized int getNextID() {
        return ++id;
    }

    /**
     * Gets online users as String - format: [threadinstance, threadinstance,
     * ... ] where threadinstance is represented with
     * ChatServerThread.toString() - 'id + "; [" + handle + "]"'
     *
     * @return String
     */
    protected synchronized String getOnlineUsers() {
        return serverContainer.toString();
    }

    /**
     * Gets online users as String - format: [threadinstance, threadinstance,
     * ... ] where threadinstance is represented with
     * ChatServerThread.toString() - 'id + "; [" + handle + "]"' Only users in
     * the room recognized by myRoom
     *
     * @param myRoom	String the room the client currently is in
     * @return String
     */
    protected synchronized String getOnlineUsers(String myRoom) {
        Vector temp = new Vector();
        ChatServerThread t;
        for (Iterator e = serverContainer.iterator(); e.hasNext();) {

            if ((t = (ChatServerThread) e.next()).getChatRoom().equals(myRoom)) {
                temp.add(t);
            }
        }
        return temp.toString();
    }

    /**
     * Method uptime Returns the servers uptime - typically when issued from
     * servercommand
     *
     * @return	String
     */
    protected synchronized String getUptime() {
        java.util.Date now = new java.util.Date();
        long diff = (now.getTime() - startup.getTime());
        long seconds = (diff / 1000);

        int days = (int) (seconds / 86400);                                     // antal hele dage
        int days_r = (int) (seconds % 86400);                                   // sekunder til rest

        int hours = days_r / 3600;                                              // heltals division	giver timer 
        int r_secs = days_r % 3600;                                             // sekunder til rest

        int mins = r_secs / 60;                                                 // heltals division giver minutter af sekunder til rest
        int secs = r_secs % 60;                                                 // sekunder til rest 	

        String mydays = days + "";
        String myhours = hours + "";
        String mymins = mins + "";
        String mysecs = secs + "";

        return "Uptime: " + mydays + " days " + myhours + " hrs " + mymins + " mins " + mysecs + " secs ";
    }

    /**
     * Method getStatus Returns the servers status - typically when issued from
     * servercommand
     *
     * @return status String
     */
    protected synchronized String getStatus() {
        return getUptime() + "\n<br>Users online: " + serverContainer.size();
    }

    protected synchronized void remove(ChatServerThread t) {
        serverContainer.remove(t);
    }
    
    public tfud.communication.DataPackage buildPackage(ChatServerThread source, String response) throws InterruptedException {
        tfud.communication.DataPackage temp = new tfud.communication.DataPackage(source.getID(), source.getID(), source.getHandle(), "ServerMessage", response);
        source.setDataPackage(temp);
        return temp;
    }
    

    /**
     * Method handleCommand
     *
     * Handles commands to the server
     *
     * @param	commandstring	String command
     * @param	source ChatServerThread source of client who wants to run a
     * command
     * @return boolean
     */
    protected synchronized boolean handleCommand(String commandstring, ChatServerThread source) {
        try {

            tfud.communication.DataPackage temp = null;

            logger.log(commandstring);
            String[] args = commandstring.split(" ");                           // args[0] == Command, args[1] == User handle
            String command = args[0];
            String handle = args[1];

            logger.log(source.myaccesslevel);

            if ((command.equals("//ban") || command.equals("//kick")) && source.getAccessLevel() <= 1) {	// only super or root
                
                String response = "";

                if (args.length > 1) {

                    ChatServerThread t = findServerThreadByHandle(handle);
                    if (t != null) {
                       t.respondToServerThread(source, command, handle);
                    } else {
                        response = " " + command + ": user \"" + handle + "\" not found\n";
                        temp = buildPackage(source, response);
                    }
                } else {
                    response = " " + command + ": not enough arguments\n";
                    temp = buildPackage(source, response);
                }
                facade.log(temp, source.getHostAddress());

                return true;
            } else if (source.getAccessLevel() == 0) {                          // only root

                String response = "";
                if (command.equals("//uptime")) {
                    response = getUptime();
                } else if (command.equals("//status")) {
                    response = getStatus();
                } else if (command.equals("//kill")) {
                    response = "Kill not implemented";
                } else if (command.equals("//restart")) {
                    response = "Restart not implemented";
                }

                temp = buildPackage(source, response);

                facade.log(temp, source.getHostAddress());

                logger.log("Command: " + commandstring);
                return true;
            } else if (command.equals("//whois") && source.getAccessLevel() <= 2) {	// only super or root or normal users
                logger.log("Command: " + commandstring);
                return true;
            }

        } catch (InterruptedException ie) {
            logger.log("HandleCommand: " + ie.getMessage());
        } finally {
            //
        }
        return false;

    }

    /**
     * Search for clientthread by handle in container and returns it - if none
     * found returns null
     *
     * @param handle String
     * @return ChatServerThread
     */
    protected synchronized ChatServerThread findServerThreadByHandle(String handle) {
        ChatServerThread t;
        for (Iterator e = serverContainer.iterator(); e.hasNext();) {

            if ((t = (ChatServerThread) e.next()).getHandle().equals(handle)) {
                return t;
            }

        }
        return null;
    }

    /**
     *
     */
    public synchronized void relayMessage(Object message) {
    }

    ;
     
    /**
     *	@param 	pkg				tfud.communication.DataPackage 
     *	@param 	myRoom			String - the room the client is in
     *	@param hostaddress		String the clients IP-adress
     */ 
    public synchronized void relayMessage(tfud.communication.DataPackage pkg, String myRoom, String hostaddress) throws InterruptedException {
        String type = pkg.getEventType();
        ChatServerThread t;

        for (Iterator e = serverContainer.iterator(); e.hasNext();) {

            t = (ChatServerThread) e.next();

            if (type.equals("PrivateMessage")) {
                if (t.getID() == pkg.getTargetID() && t.getChatRoom().equals(myRoom)) {
                    t.setDataPackage(pkg);
                    break;
                }
            } else if (t.getChatRoom().equals(myRoom)) {
                t.setDataPackage(pkg);
            }

        }
        facade.log(pkg, hostaddress);
        return;

    }

    /**
     * Server primary executing method - waits for connections and when
     * connection is made, adds an instance of ChatServerThread to
     * serverContainer
     */
    public void execute() {
        startup = new Date();

        try {
            logger.log("OK\n\nWaiting for connections on port: " + this.port);

            while (true) {
                
                serverContainer.add(new ChatServerThread(this, s.accept()));
            }

        } catch (IOException ie) {
            logger.log("IOException in Server .. " + ie.getMessage());
        } catch (Exception e) {
            logger.log("Exception in Server .. " + e.getMessage());
        }

    }



}