package tfud.server;

import java.util.*;
import java.io.*;
import tfud.communication.DataPackage;
import tfud.events.EventType;
import tfud.parsers.FakeParser;
import tfud.parsers.IParser;
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
    private final IParser parser;

    /**
     * Method ChatServer
     *
     * @param logger
     * @param facade
     * @param parser
     * @throws IOException
     * @param port int sets the port number on which the server will listens for
     * connections - defaults to port 8900
     */
    public ChatServer(int port, ILogger logger, IStorageFacade facade, IParser parser) throws IOException {
        // TODO: Add your code here
        super(port);
        this.logger = logger;
        serverContainer = new ArrayList<>(MINSERVERTHREADS);			// set initial size and incremental size
        // ID's - counts up pr new ServerThread
        id = 0;
        startup = new Date();
        this.facade = facade;
        this.parser = parser;
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
        List<ServerThread> temp = new ArrayList<>();
        
        serverContainer.forEach(s -> {
            ChatServerThread t = (ChatServerThread)s;
            if(t.getChatRoom().equals(myRoom))
                temp.add(s);
        
        });

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

    public DataPackage buildPackage(ChatServerThread source, String response) throws InterruptedException {
        DataPackage temp = new DataPackage(source.getID(), source.getID(), source.getHandle(), EventType.SERVERMESSAGE, response);
        source.setDataPackage(temp);
        return temp;
    }

    protected synchronized boolean handleCommand(String commandstring, ChatServerThread source) {
        try {

            DataPackage temp = new DataPackage();
            String response = "";

            logger.log(commandstring);

            String[] args = commandstring.split(" ");
            String command = args[0];
            String handle = args[1];

            if (args.length <= 1) {
                response = " " + command + ": not enough arguments\n";
                temp = buildPackage(source, response);
            } else {

                ChatServerThread t = findServerThreadByHandle(handle);
                if (t != null) {
                    t.respondToServerThread(source, command, handle);
                } else {
                    response = " " + command + ": user \"" + handle + "\" not found\n";
                    temp = buildPackage(source, response);
                }
            }

            facade.log(temp, source.getHostAddress());

            //source.getAccessLevel() <= 1
            logger.log(source.myaccesslevel);

            switch (command) {
                case "//ban":
                case "//kick":
                    break;
                case "//uptime":
                    response = getUptime();
                    break;
                case "//status":
                    response = getStatus();
                    break;
                case "//kill":
                    response = "Kill not implemented";
                    break;
                case "//restart":
                    response = "Restart not implemented";
                    break;
                case "//whois":
                    logger.log("Command: " + commandstring);
                    break;
                default:
                    break;
            }

            temp = buildPackage(source, response);

            facade.log(temp, source.getHostAddress());

            logger.log("Command: " + commandstring);
            return true;

        } catch (InterruptedException ie) {
            logger.log("HandleCommand: " + ie.getMessage());
        } finally {
            //
        }
        return false;

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
    /*protected synchronized boolean handleCommand(String commandstring, ChatServerThread source) {
        try {

            DataPackage temp = new DataPackage();

            logger.log(commandstring);
            String[] args = commandstring.split(" ");                         
            String command = args[0];
            String handle = args[1];

            logger.log(source.myaccesslevel);
            
            String response = "";
            
            if ((command.equals("//ban") || command.equals("//kick")) && source.getAccessLevel() <= 1) {	// only super or root

                if (args.length <= 1) {
                    response = " " + command + ": not enough arguments\n";
                    temp = buildPackage(source, response);
                } else {
                    
                    ChatServerThread t = findServerThreadByHandle(handle);
                    if (t != null) {
                        t.respondToServerThread(source, command, handle);
                    } else {
                        response = " " + command + ": user \"" + handle + "\" not found\n";
                        temp = buildPackage(source, response);
                    }
                }
                facade.log(temp, source.getHostAddress());

                return true;
            } else if (source.getAccessLevel() == 0) {                          // only root
                switch (command) {
                    case "//uptime":
                        response = getUptime();
                        break;
                    case "//status":
                        response = getStatus();
                        break;
                    case "//kill":
                        response = "Kill not implemented";
                        break;
                    case "//restart":
                        response = "Restart not implemented";
                        break;
                    default:
                        break;
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

    }*/
    
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
    @Override
    public synchronized void relayMessage(Object message) {

    }

    /**
     * @param pkg	tfud.communication.DataPackage
     * @param myRoom	String - the room the client is in
     * @param hostaddress	String the clients IP-adress
     * @throws java.lang.InterruptedException
     */
    public synchronized void relayMessage(DataPackage pkg, String myRoom, String hostaddress) throws InterruptedException {
        EventType type = pkg.getEventType();
        ChatServerThread t;

        for (Iterator e = serverContainer.iterator(); e.hasNext();) {

            t = (ChatServerThread) e.next();

            if (type == EventType.PRIVATEMESSAGE) {
                if (t.getID() == pkg.getTargetID() && t.getChatRoom().equals(myRoom)) {
                    t.setDataPackage(pkg);
                    break;
                }
            } else if (t.getChatRoom().equals(myRoom)) {
                t.setDataPackage(pkg);
            }

        }
        facade.log(pkg, hostaddress);
    }

    /**
     * Server primary executing method - waits for connections and when
     * connection is made, adds an instance of ChatServerThread to
     * serverContainer
     */
    @Override
    public void execute() {
        startup = new Date();

        try {
            logger.log("OK\n\nWaiting for connections on port: " + this.port);

            while (true) {
                serverContainer.add(new ChatServerThread(this, s.accept(), parser));
            }

        } catch (IOException ie) {
            logger.log("IOException in Server .. " + ie.getMessage());
        } catch (Exception e) {
            logger.log("Exception in Server .. " + e.getMessage());
        }

    }

}
