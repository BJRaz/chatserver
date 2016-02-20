/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tfud.server;

import tfud.utils.ILogger;

/**
 *
 * @author brian
 */
public class SystemLogger implements ILogger {

    public void log(String message) {
        System.out.println(message);
    }

    public void log(Object message) {
        log(message.toString());
    }
    
}
