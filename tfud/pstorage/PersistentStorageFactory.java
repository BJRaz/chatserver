/*
 * PersistentStorageFactory.java
 *
 * Created on 27. december 2005, 10:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package tfud.pstorage;

/**
 * Factory class for instantiating DB objects from types PGSQL, MYSQL or ODBC
 *
 * @author BJR
 */
public abstract class PersistentStorageFactory {

    public final static int PGSQL = 0;
    public final static int MYSQL = 1;
    public final static int ODBC = 2;

    /**
     * Creates a new instance of PersistentStorageFactory
     */
    public PersistentStorageFactory() {
        //default
    }

    /**
     * @param type	integer defined as CONSTANTS
     * @return DB DB instance defined by type, or null if type not found
     */
    public static DB createDBInstance(int type) {
        // TODO: Add your code here
        /*if(type == PGSQL) {
			return DBPostgreSQL.getInstance();
		} else if(type == MYSQL) {
			return DBMySQL.getInstance();			
		} else if(type == ODBC) {
			return DBOdbc.getInstance();			
		}*/
        return null;
    }

}
