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
            ChatServer server = new ChatServer(MainApp.port);
            /*if(!path.equals("")) {
            System.out.print("Parsing configfile...\t\t");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = (factory).newDocumentBuilder();
            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
            public void error(SAXParseException e) {
            System.out.println("Error parsing xml: " + e.getMessage());
            };
            public void fatalError(SAXParseException e) {
            System.out.println("fatalError parsing xml: " + e.getMessage());
            };
            public void warning(SAXParseException e) {
            System.out.println("warning parsing xml: " + e.getMessage());
            };
            });
            Document doc = builder.parse(new java.io.File(path));
            Element root = doc.getDocumentElement();
            NodeList database = (root.getElementsByTagName("database"));
            Node type = database.item(0).getFirstChild();
            Node hostname = type.getNextSibling();
            Node dbname = hostname.getNextSibling();
            Node username = dbname.getNextSibling();
            Node password = username.getNextSibling();
            System.out.println("OK");
            System.out.print("Creating databaseinstance...\t");
            server.facade = new StorageFacade(
            Integer.parseInt(type.getFirstChild().getNodeValue()),
            hostname.getFirstChild().getNodeValue(),
            dbname.getFirstChild().getNodeValue(),
            username.getFirstChild().getNodeValue(),
            password.getFirstChild().getNodeValue()
            );
            } else {
            System.out.println("Databasefacade not initialized - server halted..check arguments");
            server = null;
            System.exit(0);
            }
            System.out.print("Starting server ...\t\t");
             */
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
            for (int i = 0; i < args.length; i++) {
                String[] temp = args[i].split("\\=");
                if (temp[0].equals("configfile")) {
                    ChatServer.path = temp[1];
                }
                if (temp[0].equals("port")) {
                    MainApp.port = Integer.parseInt(temp[1]);
                }
            }
        } else {
            System.out.println("Usage: ChatServer <configfile=settingsfile> [port=<port no>] \n <..> means required - [..] optional.");
            return;
        }
    }

}
