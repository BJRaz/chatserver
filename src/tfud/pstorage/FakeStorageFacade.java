/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tfud.pstorage;

/**
 *
 * @author brian
 */
public class FakeStorageFacade implements IStorageFacade {

    @Override
    public void banUser(String username) {
        System.out.println("banUser called: " + username);
    }

    @Override
    public int checkLogin(String username, String password) {
        System.out.println("checklogin called: " + username + " " + password);
        return 1;
    }

    @Override
    public int getAccessLevel(String username) {
        System.out.println("getAccessLevel called: " + username);
        return 0;
    }

    @Override
    public void log(String message) {
        System.out.println("log callled: " + message);
    }

    /**
     *
     * @param username
     * @param status
     */
    @Override
    public void updateOnlineStatus(String username, int status) {
        System.out.println("updateOnlineStatus called: " + status);
    }
    
}
