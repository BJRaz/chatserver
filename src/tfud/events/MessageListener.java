package tfud.events;

/**
 * @author BJR
 */
public interface MessageListener {

    public void messageReceived(Object source, Object data);			// standard message

    public void privateMessageReceived(Object source, Object data);		// private message

    public void offlineMessageReceived(Object source, Object data);		// offline message (some user logged off)

    public void onlineMessageReceived(Object source, Object data);		// online message (some user became online)

    public void loginMessageReceived(Object source, Object data);		// login message (user logged in)

    public void serverMessageReceived(Object source, Object data);		// server related message (kick, status, whatever)

    public void awayMessageReceived(Object source, Object data);		// some is away

    public void userListReceived(Object source, Object data);			// list of users received

    public void changeRoomReceived(Object source, Object data);			// change room message

}
