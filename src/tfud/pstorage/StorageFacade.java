package tfud.pstorage;

import java.sql.*;

/**
 * class StorageFacade
 *
 * <br>
 *
 * @author BJR
 */
class StorageFacade implements IStorageFacade {

    protected DB db;

    /**
     * Method StorageFacade
     *
     *
     */
    StorageFacade() {
        // TODO: Add your code here

    }

    /**
     * Method StorageFacade - creates database instance
     *
     * @param	typedb	the int telling which type of RDBMS is in use
     * @param	hostname	the string of hostname or address of RDBMS-system to
     * connect to
     * @param	dbname	string name of database to use
     * @param	username	string name of database user
     * @param	password string password related to database user
     */
    StorageFacade(int typedb, String hostname, String dbname, String username, String password) {

        db = PersistentStorageFactory.createDBInstance(typedb);
        // connect = host, dbname, user, pass
        System.out.println("OK (" + typedb + " " + hostname + " " + dbname + " " + username + " " + password + ")");
        //if(db != 0)
        db.connect(hostname, dbname, username, password);
        /*       else
                                System.out.println("DB instance creation failed");*/
    }

    /**
     * Method checkLogin - checks if a user is allowed access to this server
     *
     *
     * @param username the username which is to be checked
     * @param password	the password related to username
     *
     * @return a int bigger than 0 if check succeded. Typically the users ID in
     * the RDMS. returns 0 if check failed
     *
     */
    @Override
    public synchronized int checkLogin(String username, String password) {
        
        int returnValue = 0;
        try {
            db.open();
            db.query("SELECT id, accesslevel, ban FROM users WHERE username='" + username + "' AND password='" + password + "'");// AND (ban IS NULL OR ban < now()) 

            ResultSet result = db.getResultSet();

            if (result.next()) {
                if (result.getTimestamp("ban") == null || result.getTimestamp("ban").getTime() <= (new java.util.Date().getTime())) {
                    returnValue = result.getInt("id");
                } else {
                    returnValue = -1;	// banned
                }
            }
            
            db.close();
        } catch (Exception e) {

        }

        return returnValue;
    }

    /**
     * Method log
     *
     *
     * @param pack	the datapackage containing information to be logged.
     * @param hostaddress	the string with clients TCP/IP or domain address
     *
     */
    @Override
    public synchronized void log(String message) {

        // TODO: Add your code here
        try {
            /*String sql = "INSERT INTO logging(time, from_id, target_id, event, data, hostaddress) VALUES('" + new java.util.Date() + "', '" + pack.getID() + "', '" + pack.getTargetID() + "', '" + pack.getEventType() + "', '" + pack.getData() + "', '" + hostaddress + "')";
			//DEBUG System.out.println(sql);
			db.open();
			db.insert(sql);	
			db.close();
             */
        } catch (Exception e) {
            System.out.println("StorageFacade (log): " + e.getMessage());
        }
    }

    /**
     * This method takes as argument the name of the user which is to be banned
     * - the user will be banned for 24 hours
     *
     * @param	username	the username as string which is to be banned
     */
    @Override
    public synchronized void banUser(String username) {
        try {
            Timestamp now = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
            now.setTime(now.getTime() + 86400000);	// +24 hours
            String sql = "UPDATE users SET ban = '" + now.toString() + "' WHERE username = '" + username + "'";
            System.out.println(sql);
            db.open();
            db.update(sql);
            db.close();
            System.out.println("Ban done: " + sql);
        } catch (Exception e) {
            System.out.println("StorageFacade (banUser): " + e.getMessage());
        }
    }

    /**
     * This method returns the users accesslevel ...
     *
     * @param username the username as string
     * @return the users accesslevel - 2 is default (normal user)
     */
    @Override
    public synchronized int getAccessLevel(String username) {
        int returnValue = 2;		// default accesslevel
        try {
            db.open();
            db.query("SELECT accesslevel FROM users WHERE username='" + username + "'");

            ResultSet result = db.getResultSet();

            if (result.next()) {
                returnValue = result.getInt("accesslevel");
            }

            db.close();
        } catch (Exception e) {
            System.out.println("StorageFacade(checkLogin): " + e.getMessage());
        }
        return returnValue;
    }

    @Override
    public synchronized void updateOnlineStatus(String username, int status) {
        String sql = "UPDATE users SET online = " + status + " WHERE username = '" + username + "'";
        try {
            db.open();
            db.update(sql);
            db.close();
        } catch (Exception e) {
            System.out.println("StorageFacade(updateOnlineStatus): " + e.getMessage() + "\n" + sql);
        }

    }
}
