package tfud.server;

import java.io.*;
import java.net.*;

/**
 * class ServerThread
 *
 * Baseclass for all clientthreads
 *
 * @author BJR
 */
abstract class ServerThread extends Thread {

    protected Socket socket;

    protected InputStream input;
    protected OutputStream output;

    protected String hostaddress;

    public ServerThread(Socket socket) throws SocketException, IOException {

        this.socket = socket;										// reference to socket instance

        this.hostaddress = socket.getInetAddress().getHostName();	// get remote host addr

        socket.setReceiveBufferSize(43000);

        input = socket.getInputStream();							// sets streams
        output = socket.getOutputStream();

    }

    /**
     * Template Method Pattern
     *
     *
     */
    public abstract void handleConnection();

    protected String getHostAddress() {
        return this.hostaddress;
    }

    /**
     * Calls handleConnection which must be declared in subclasses
     *
     *
     */
    @Override
    public final void run() {
        handleConnection();
    }

    @Override
    public String toString() {
        return "ServerThread";
    }

}
