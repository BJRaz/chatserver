/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tfud.pstorage;

import tfud.communication.DataPackage;

/**
 *
 * @author brian
 */
public interface IStorageFacade {

    /**
     * This method takes as argument the name of the user which is to be banned
     * - the user will be banned for 24 hours
     *
     * @param	username	the username as string which is to be banned
     */
    void banUser(String username);

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
    int checkLogin(String username, String password);

    /**
     * This method returns the users accesslevel ...
     *
     * @param username the username as string
     * @return the users accesslevel - 2 is default (normal user)
     */
    int getAccessLevel(String username);

    /**
     * Method log
     *
     *
     * @param pack	the datapackage containing information to be logged.
     * @param hostaddress	the string with clients TCP/IP or domain address
     *
     */
    void log(Object pack, String hostaddress);

    void updateOnlineStatus(String username, int status);
    
}
