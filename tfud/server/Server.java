/**
 * AWT Sample application
 *
 * @author BJR
 * @version 1.00 04/03/02
 *
 * Server is Base server class - implemented with the Template Method Pattern in
 * mind
 */
package tfud.server;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 *
 * class Server
 *
 * Baseclass for types of multithreaded, relaying servers subclasses need to
 * define method "execute", which is abstract
 *
 * @author BJR
 */
abstract class Server {

    protected static int MINSERVERTHREADS = 50;

    protected static ServerSocket s;
    protected Vector serverContainer;
    protected int port;

    /**
     * Constructor Server
     *
     * @throws IOException
     * @param port int sets the port number on which the server will listens for
     * connections
     */
    public Server(int port) throws IOException {
        this.port = port;
        s = new ServerSocket(this.port);
    }

    /**
     * @return int the port number
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Abstract method execute - need to be defined in subclasses
     */
    public abstract void execute();

    /**
     * Abstract method execute - need to be defined in subclasses
     *
     * @param message Object
     */
    public abstract void relayMessage(Object message);

}
