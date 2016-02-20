package tfud.events;

import java.util.EventObject;

/**
 * @author BJR
 */
public class ConnectionEvent extends EventObject {

    private String command;

    /**
     * Method ConnectionEvent
     *
     *
     */
    public ConnectionEvent(Object source, String command) {
        // TODO: Add your code here
        super(source);
        this.command = command;
    }

    public String getCommand() {
        return this.command;
    }
}
