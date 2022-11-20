package tfud.events;

import java.util.Vector;

/**
 * @author BJR
 */
public class ConnectionHandler {

    private Vector connectionListeners;

    /**
     * Method ConnectionHandler
     *
     *
     */
    public ConnectionHandler() {
        // TODO: Add your code here
        connectionListeners = new Vector();
    }

    public void addConnectionListener(ConnectionListener c) {
        connectionListeners.addElement(c);
    }

    public void fireConnectionUpdated(String command) {

        for (int i = 0; i < connectionListeners.size(); i++) {
            ConnectionListener l = ((ConnectionListener) connectionListeners.elementAt(i));
            ConnectionEvent cEvt = new ConnectionEvent(l, command);

            l.connectionUpdated(cEvt);
        }

    }
}
