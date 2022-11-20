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

    public void banUser(String username) {
        System.out.println("banUser callled: " + username);
    }

    public int checkLogin(String username, String password) {
        System.out.println("Checklogin callled: " + username + " " + password);
        return 1;
    }

    public int getAccessLevel(String username) {
        System.out.println("getAccessLevel callled: " + username);
        return 0;
    }

    public void log(Object pack, String hostaddress) {
        System.out.println("log callled: " + " " + pack.toString() + " " + hostaddress);
    }

    public void updateOnlineStatus(String username, int status) {
        System.out.println("updateOnlineStatus called: " + status);
    }
    
}
