/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tfud.server;

/**
 *
 * @author brian
 */
public class MainApp {

    private static int port = 8900;
    /**
     * Method main
     *
     *
     * @param args	String array
     *
     */
    public static void main(String[] args) {
        // TODO: Add your code here
        // Default port = 8900
        System.out.println("*********************************");
        System.out.println("Welcome to Chatserver version 2.0");
        System.out.println("(c) 2006 Brian Juul Rasmussen");
        System.out.println("*********************************");
        System.out.println("");
        
        parseInputParameters(args);
        
        try {
            Server server = new ChatServer(MainApp.port, new SystemLogger(), new tfud.pstorage.FakeStorageFacade());
            
            server.execute();
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
    }

    /**
     * Method parseInputParameters parses arguments
     *
     * @param args
     *
     */
    private static void parseInputParameters(String[] args) {
        // TODO: Add your code here
        if (args.length > 0) {
            for (String arg : args) {
                String[] temp = arg.split("\\=");
                if (temp[0].equals("configfile")) {
                    ChatServer.path = temp[1];
                }
                if (temp[0].equals("port")) {
                    MainApp.port = Integer.parseInt(temp[1]);
                }
            }
        } else {
            System.out.println("Usage: ChatServer <configfile=settingsfile> [port=<port no>] \n <..> means required - [..] optional.");
        }
    }
   

}
