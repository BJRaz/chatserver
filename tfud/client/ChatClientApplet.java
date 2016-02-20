package tfud.client;

// JSObject support?
import netscape.javascript.*;

import java.awt.*;
import javax.swing.*;
import tfud.events.*;

/**
 * @author BJR
 */
public class ChatClientApplet extends JApplet implements tfud.events.MessageListener, tfud.events.ConnectionListener {

    private JSObject window, document;

    private ChatClient client;
    private String host;
    private String username;
    private String password;

    private int id;
    private int port;	// port number on server 

    private ConnectionHandler connhandler;

    /**
     * Method ChatClientApplet
     *
     *
     */
    public ChatClientApplet() {
        // TODO: Add your code here

        //client.setHost("tfud.dyndns.org");
        client = null;

    }

    public void init() {
        try {
            window = JSObject.getWindow(this);
            document = (JSObject) window.getMember("document");

            host = getParameter("serverHostName");
            port = Integer.parseInt(getParameter("port"));
            username = getParameter("username");
            password = getParameter("password");

            connhandler = new ConnectionHandler();
            connhandler.addConnectionListener(this);

            /* TEST */
            //LoginFrame frame = new LoginFrame();
            this.setVisible(false);
            System.out.println("Init");

        } catch (Exception e) {
            System.out.println("Applet general error - INIT failed: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void start() {
        System.out.println("Starting...");
    }

    public void stop() {
        System.out.println("Stopping...");
        if (client != null) {
            client.stopClient();
        }
    }

    public void destoy() {
        System.out.println("Destroying...");
    }

    // Delegate function
    public void onConnect(String username, String password) {
        try {
            System.out.println(client);
            if (client != null) {
                client = null;
            }

            client = new ChatClient(host, 8900);

            client.handler.addMessageListener(this);
            client.connhandler.addConnectionListener(this);

            client.setHandle(username);
            /* TEST */
            client.setUsername(username);
            client.setPassword(password);
            /**/
            client.startClient();
            System.out.println("Starting client");

        } catch (java.io.IOException io) {
            connhandler.fireConnectionUpdated(io.getMessage());
        } catch (Exception e) {
            System.out.println("Error onConnect: " + e.getMessage());
        }

    }

    public void onDisconnect() {
        if (client != null && !client.isStopped()) {
            client.stopClient();
            System.out.println("Client stopped.. ");
            client = null;
        }
    }

    // Delegate function
    public void setMessage(String event, String data) throws InterruptedException {
        if (!client.isStopped()) {
            client.setMessage(event, data);
        }
        // alert not connected
    }

    // Delegate function 
    public void setPrivateMessage(int target, String event, String data) throws InterruptedException {
        if (!client.isStopped()) {
            client.setMessage(target, event, data);
        }
    }

    public void paint(Graphics g) {
    }

    public void messageReceived(Object source, Object data) {
        delegateMessageReceived("OnMessageReceived", data);
    }

    public void privateMessageReceived(Object source, Object data) {
        delegateMessageReceived("OnPrivateMessageReceived", data);
    }

    public void offlineMessageReceived(Object source, Object data) {
        delegateMessageReceived("OnOfflineMessageReceived", data);
    }

    public void onlineMessageReceived(Object source, Object data) {
        delegateMessageReceived("OnOnlineMessageReceived", data);
    }

    public void awayMessageReceived(Object source, Object data) {
        delegateMessageReceived("OnAwayMessageReceived", data);
    }

    public void userListReceived(Object source, Object data) {
        delegateMessageReceived("OnUserListReceived", data);
    }

    public void changeRoomReceived(Object source, Object data) {
        delegateMessageReceived("OnChangeRoomReceived", data);
    }

    public void loginMessageReceived(Object source, Object data) {
        delegateMessageReceived("OnLoginMessageReceived", data);
    }

    public void serverMessageReceived(Object source, Object data) {

        Object obj = ((tfud.communication.DataPackage) data).getData();	// +"" fusk
        String msg = obj.toString();
        /*String command = "";
		if(obj.getClass().isArray()) {
			Object[] temp = (Object[])obj;
			System.out.println("Is array");
			msg = temp[0].toString();
			for(int i=1;i<temp.length;i++)
				msg = msg + ";" + temp[i].toString();
			
			command = temp[0].toString();
			
			
		} else {
			msg = obj.toString();
			command = msg.toString();			
		}
		
		((tfud.communication.DataPackage)data).setData(msg);	
         */

        //System.out.println("HER: " + msg);	
        delegateMessageReceived("OnServerMessageReceived", data);
        if (msg.equals("KICK") || msg.equals("BAN")) {
            client.stopClient();
        }

    }

    /**
     *
     * DELEGATING Method
	 *
     */
    private void delegateMessageReceived(String what, Object data) {
        Object[] args = new Object[4];
        args[0] = new Integer(((tfud.communication.DataPackage) data).getID());
        args[1] = ((tfud.communication.DataPackage) data).getHandle();
        args[2] = ((tfud.communication.DataPackage) data).getEventType();
        args[3] = ((tfud.communication.DataPackage) data).getData();
        System.out.println("DATA: " + ((tfud.communication.DataPackage) data).getData());
        window.call(what, args);
    }

    public void connectionUpdated(tfud.events.ConnectionEvent cEvt) {
        Object[] args = new Object[2];
        args[0] = cEvt.getSource();
        args[1] = cEvt.getCommand();
        //if(cEvt.getCommand() == "Connection reset")

        window.call("OnConnectionUpdated", args);
    }

    public int getID() {
        return id;
    }
}
