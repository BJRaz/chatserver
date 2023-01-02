/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tfud.server;

import tfud.parsers.FakeParser;
import tfud.utils.ILogger;

/**
 *
 * @author brian
 */
public class MainApp {

    private int port = 8900;
    ILogger logger;
    private String configpath;

    MainApp() {
        logger = new SystemLogger();
    }

    private void showBanner() {
        StringBuilder banner = new StringBuilder();
        banner.append("*********************************\n");
        banner.append("Welcome to Chatserver version 2.0\n");
        banner.append("(c) 2006 Brian Juul Rasmussen\n");
        banner.append("*********************************\n\n");
        System.out.println(banner.toString());
    }

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

        MainApp app = new MainApp();

        app.start(args);
    }

    public void start(String[] args) {
        
        parseInputParameters(args);
        
        showBanner();

        try {
            Server server = new ChatServer(
                    port,
                    logger,
                    new tfud.pstorage.FakeStorageFacade(),
                    new FakeParser()
            );

            server.execute();
        } catch (Exception e) {
            logger.log("Error " + e.getMessage());
        }
    }

    /**
     * Method parseInputParameters parses arguments
     *
     * @param args
     *
     */
    private void parseInputParameters(String[] args) {

        // TODO: Add your code here
        if (args.length > 0) {
            for (String arg : args) {
                String[] temp = arg.split("\\=");
                if (temp[0].equals("configfile")) {
                    configpath = temp[1];
                }
                if (temp[0].equals("port")) {
                    port = Integer.parseInt(temp[1]);
                }
            }
        } else {
            System.out.println("Usage: ChatServer <configfile=settingsfile> [port=<port no>] \n <..> means required - [..] optional.");
        }
    }

}
