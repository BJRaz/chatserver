package tfud.communication;

/**
 * @version 1.0
 * @author BJR
 */
import java.io.Serializable;

/**
 * This class is used for holding data - will be serialized in networkstream,
 * encapsulation for data
 *
 * @author BJR
 */
public class DataPackage implements Serializable {

    protected int id, targetid;
    protected String handle;
    protected String event;
    protected Object data;

    /**
     * Constructor DataPackage
     *
     *
     */
    public DataPackage() {
        // TODO: Add your code here
        id = 0;
        targetid = 0;
        handle = "";
        event = "";
        data = "";
    }

    /**
     * Constructor
     *
     * @param id integer
     * @param	targetid	integer
     * @param	handle	String
     * @param event	String
     * @param	data	Object
     */
    public DataPackage(int id, int targetid, String handle, String event, Object data) {
        // TODO: Add your code here
        this.id = id;
        this.targetid = targetid;
        this.handle = handle;
        this.event = event;
        this.data = data;
    }

    /**
     * Method getID
     *
     *
     * @return id integer specifying id of user
     *
     */
    public int getID() {
        // TODO: Add your code here
        return id;
    }

    /**
     * Method setID
     *
     *
     * @param id	integer
     *
     *
     */
    public void setID(int id) {
        // TODO: Add your code here
        this.id = id;
    }

    /**
     * Method getHandle
     *
     *
     * @return	handle String
     *
     */
    public String getHandle() {
        // TODO: Add your code here
        return handle;
    }

    /**
     * Method setHandle
     *
     *
     * @param handle	String
     *
     */
    public void setHandle(String handle) {
        // TODO: Add your code here
        this.handle = handle;
    }

    /**
     * Method getEventType
     *
     *
     * @return	event	String
     *
     */
    public String getEventType() {
        // TODO: Add your code here
        return event;
    }

    /**
     * Method setEventType
     *
     *
     * @param event	String
     *
     */
    public void setEventType(String event) {
        // TODO: Add your code here
        this.event = event;
    }

    /**
     * Method getData
     *
     *
     * @return	data Object
     *
     */
    public Object getData() {
        // TODO: Add your code here
        return data;
    }

    /**
     * Method setData
     *
     * @param	data Object
     */
    public void setData(Object data) {
        // TODO: Add your code here
        this.data = data;
    }

    /**
     * Method setTargetID
     *
     *
     * @param id	integer
     *
     */
    public void setTargetID(int id) {
        // TODO: Add your code here
        this.targetid = id;
    }

    /**
     * Method getTargetID
     *
     *
     * @return	id	integer
     *
     */
    public int getTargetID() {
        // TODO: Add your code here
        return this.targetid;
    }

    /**
     * @return objectinfo	String
     */
    public String toString() {
        return "DataPackage[ID: " + this.id + " TargetID: " + this.targetid + " Handle: " + this.handle + " Event: " + this.event + " Data: " + this.data.toString();
    }

}
