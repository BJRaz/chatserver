package tfud.events;

import java.util.*;
import tfud.communication.DataPackage;

/**
 * @author BJR
 */
public class MessageHandler {

    private Vector messageListeners;

    public MessageHandler() {
        messageListeners = new Vector();
    }

    public void addMessageListener(MessageListener m) {
        messageListeners.addElement(m);
    }

    public void handleMessage(DataPackage p) {
        String eventType = p.getEventType();
        System.out.println("Event type: " + eventType);
        if (eventType.equals("Online")) {
            fireOnlineReceived(p);
        } else if (eventType.equals("Offline")) {
            fireOfflineReceived(p);
        } else if (eventType.equals("Away")) {
            fireAwayReceived(p);
        } else if (eventType.equals("Message")) {
            fireMessageReceived(p);
        } else if (eventType.equals("PrivateMessage")) {
            firePrivateMessageReceived(p);
        } else if (eventType.equals("UserList")) {
            fireUserListReceived(p);
        } else if (eventType.equals("ChangeRoom")) {
            fireChangeRoomReceived(p);		/* NOTICE THIS MUST BE GIVEN MORE THOUGHTS */
        } else if (eventType.equals("Login")) {
            fireLoginMessageReceived(p);	/* NOTICE THIS MUST BE GIVEN MORE THOUGHTS */
        } else if (eventType.equals("ServerMessage")) {
            fireServerMessageReceived(p);	/* NOTICE THIS MUST BE GIVEN MORE THOUGHTS */
        }
    }

    public void fireAwayReceived(DataPackage dp) {
        for (int i = 0; i < messageListeners.size(); i++) {
            ((MessageListener) messageListeners.elementAt(i)).awayMessageReceived(this, dp);
        }
    }

    public void fireMessageReceived(DataPackage dp) {
        for (int i = 0; i < messageListeners.size(); i++) {
            ((MessageListener) messageListeners.elementAt(i)).messageReceived(this, dp);
        }
    }

    public void firePrivateMessageReceived(DataPackage dp) {
        for (int i = 0; i < messageListeners.size(); i++) {
            ((MessageListener) messageListeners.elementAt(i)).privateMessageReceived(this, dp);
        }
    }

    public void fireOfflineReceived(DataPackage dp) {
        for (int i = 0; i < messageListeners.size(); i++) {
            ((MessageListener) messageListeners.elementAt(i)).offlineMessageReceived(this, dp);
        }
    }

    public void fireOnlineReceived(DataPackage dp) {
        for (int i = 0; i < messageListeners.size(); i++) {
            ((MessageListener) messageListeners.elementAt(i)).onlineMessageReceived(this, dp);
        }
    }

    public void fireUserListReceived(DataPackage dp) {
        for (int i = 0; i < messageListeners.size(); i++) {
            ((MessageListener) messageListeners.elementAt(i)).userListReceived(this, dp);
        }
    }

    /* NOTICE THIS MUST BE GIVEN MORE THOUGHTS */
    public void fireChangeRoomReceived(DataPackage dp) {
        for (int i = 0; i < messageListeners.size(); i++) {
            ((MessageListener) messageListeners.elementAt(i)).changeRoomReceived(this, dp);
        }
    }

    /* NOTICE THIS MUST BE GIVEN MORE THOUGHTS */
    public void fireLoginMessageReceived(DataPackage dp) {
        for (int i = 0; i < messageListeners.size(); i++) {
            ((MessageListener) messageListeners.elementAt(i)).loginMessageReceived(this, dp);
        }
    }

    public void fireServerMessageReceived(DataPackage dp) {
        System.out.println("ServerMessage handling...");
        for (int i = 0; i < messageListeners.size(); i++) {
            ((MessageListener) messageListeners.elementAt(i)).serverMessageReceived(this, dp);
        }
    }
}
