package tfud.client;

import java.io.*;
import java.util.*;
import tfud.events.*;
import tfud.communication.*;

/**
 * @author BJR
 */
public class ChatClient extends Client {

    protected final static int MAXBUFFERLENGTH = 50;
    protected Vector outBuffer;

    protected String serverhost;

    protected int id;
    protected String handle;

    protected boolean finished;

    protected String username = "";
    protected String password = "";

    /* Client/Server protocol based on ObjectStreams */
    protected ObjectOutputStream output;
    protected ObjectInputStream input;

    protected MessageHandler handler;
    protected ConnectionHandler connhandler;

    /**
     * @param	serveraddress string the domainaddress or TCP/IP address of server
     * @param	port	int of port to connect to
     */
    public ChatClient(String serveraddress, int port) throws IOException {

        super(serveraddress, port);

        /* set object streams */
        output = new ObjectOutputStream(out);
        input = new ObjectInputStream(in);

        this.serverhost = serveraddress;

        this.id = 0;
        this.handle = "";

        this.outBuffer = new Vector(MAXBUFFERLENGTH);
        this.handler = new MessageHandler();
        this.connhandler = new ConnectionHandler();

        // not running
        finished = true;

    }

    public synchronized void setMessage(String event, String data) throws InterruptedException {
        if (!isStopped()) {
            while (outBuffer.size() == MAXBUFFERLENGTH) // if vector full wait
            {
                wait();
            }
            outBuffer.addElement(new DataPackage(this.id, 0, this.handle, event, data));
        }
    }

    public synchronized void setMessage(int target, String event, String data) throws InterruptedException {
        if (!isStopped()) {
            while (outBuffer.size() == MAXBUFFERLENGTH) {
                wait();
            }
            outBuffer.addElement(new DataPackage(this.id, target, this.handle, event, data));
        }
    }

    public void setID(int id) {
        this.id = id;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public void startClient() {
        if (finished) {
            start();				// Thread.start()
            finished = false;
        }
    }

    public void stopClient() {
        if (!finished) {
            finished = true;
        }

        try {

            closeIO();

        } catch (IOException io) {
            System.out.println("STOP: IO close failed: " + io.getMessage());
            io.printStackTrace();
        }

    }

    public boolean isStopped() {
        return finished;
    }

    /**
     * Method setUsername
     *
     *
     * @param username
     *
     */
    public void setUsername(String username) {
        // TODO: Add your code here
        this.username = username;
    }

    /**
     * Method setPassword
     *
     *
     * @param password
     *
     */
    public void setPassword(String password) {
        // TODO: Add your code here
        this.password = password;
    }

    protected void handleConnection() {
        try {

            Object res = null;

            /* INIT */
            Object[] data1 = new Object[2];
            data1[0] = new String(username);
            data1[1] = new String(password);

            output.writeObject(new DataPackage(this.id, 0, this.handle, "Online", data1));

            DataPackage data = (DataPackage) input.readObject();		// USERLIST, NOT ALLOWED TO LOGIN etc...
            //System.out.println("HER: " + data);
            this.id = data.getID();
            this.handler.handleMessage(data);

            output.writeObject(new DataPackage());

            /* END INIT */
 /*data = null;	
				
			data = new DataPackage();	
			data.setTargetID(0);
			data.setHandle(this.handle);
			data.setEventType("Message");
			data.setData("");
             */
            while (!finished) {

                res = null;

                // This reads messages from server ...
                res = input.readObject();

                //if(res.length() > 0) {	
                if (res instanceof DataPackage) {
                    // can cause null exception - (res can be null is connection is down)
                    // Alert message listeners
                    /*
					 *	Here insert a handler of messages 
					 *	handler should parse message - and raise events respectively
					 **/

                    data = (DataPackage) res;

                    if (data.getData() != null && !data.getData().toString().equals("")) {
                        this.handler.handleMessage(data);
                    }

                }

                /* 	Empty outBuffer 
				 *	for messages from this client to the server 
				 *
				 **/
                if (outBuffer.size() > 0) {

                    // removes first element in Vector 
                    // - in effect simulates an Queue but not that efficient
                    data = (DataPackage) outBuffer.remove(0);

                    // Release lock as soon there's room in the Vector
                    // NOTICE - must be called from releaseLock function (sync) - otherwise 
                    //	Thread-Not-Owner Exception thrown
                    releaseLock();

                    //DEBUG
                    System.out.println("Eating from MessageBuffer - now size: " + outBuffer.size());

                    output.writeObject(data);
                    data = null;
                } else {
                    // write null for optimizing network and memory usage
                    output.writeObject(null);

                }
            }

        } catch (java.security.AccessControlException a) {
            System.out.println("Access Execption: " + a.getMessage());
            // DEBUG
            //io.printStackTrace();
            this.connhandler.fireConnectionUpdated("Access denied while connecting - SET serverhostname in <PARAM .. >\n" + a.getMessage());
        } catch (IOException io) {
            System.out.println("ChatClient IOExecption: " + io.getMessage());
            // DEBUG
            io.printStackTrace();
            this.connhandler.fireConnectionUpdated(io.getMessage());
        } catch (Exception e) {
            //DEBUG
            System.out.println("Chat client general error: " + e.getMessage());
            this.connhandler.fireConnectionUpdated("General error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {

                closeIO();

            } catch (IOException o) {
                System.out.println("IO Error in closing I/O ; " + o.getMessage() + "\n");
                // DEBUG
                o.printStackTrace();
            } catch (Exception e) {
                System.out.println("General Closing IO; " + e.getMessage() + "\n");
                // DEBUG
                e.printStackTrace();
            } finally {
                outBuffer.clear();
                finished = true;
                System.out.println("ChatClient ended");
            }
        }
    }

    private void closeIO() throws IOException {
        output.close();
        input.close();
        server.close();
    }

    private synchronized void releaseLock() {
        notifyAll();
    }

}
