/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklab;

/**
 *
 * @author cem
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;


public class Client {

    public Socket socket;

    public ObjectInputStream sInput;

    public ObjectOutputStream sOutput;

    public int id;

    public String username;

    public Date ConDate;

    public ClientThread ListenThread;
    
    ServerMain serverMain = new ServerMain();
    
    public Client(Socket socket) {
        //this.id = ++Server.uniqueId;
        this.socket = socket;

        try {
            this.sOutput = new ObjectOutputStream(socket.getOutputStream());
            this.sInput = new ObjectInputStream(socket.getInputStream());
            this.username = (String) sInput.readObject();
            this.ConDate = new Date();
            this.ListenThread = new ClientThread(this);
        } catch (IOException e) {
            System.out.println("Exception .... " + e);
            return;
        } catch (ClassNotFoundException e) {

        }
    }

    public void start() {
        this.ListenThread = new ClientThread(this);
        this.ListenThread.start();
    }

    public void close() {
        try {
            if (this.ListenThread != null) {
                this.ListenThread.interrupt();
            }
            if (this.sOutput != null) {
                this.sOutput.close();
            }

            if (this.sInput != null) {
                this.sInput.close();
            }

            if (this.socket != null) {
                this.socket.close();
            }
        } catch (Exception e) {
        }
    }

    public boolean writeMsg(Object msg) {
        if (!this.socket.isConnected()) {
            close();
            return false;
        }
        try {
            this.sOutput.writeObject(msg);
        } catch (Exception e) {
            serverMain.setServerLogs("Error sending message to " + username);
           // Server.sendLogToFrame("Error sending message to " + username);
            System.out.println(e.toString());
        }
        return true;
    }

    public class ClientThread extends Thread {
        Client TheClient;
        public ClientThread(Client TheClient) {
            this.TheClient = TheClient;
        }
        public void run() {
            while (TheClient.socket.isConnected()) {
                try {
                    String message = (String) this.TheClient.sInput.readObject();
                    System.out.println("gelen mesaj: " + message);
                } catch (IOException e) {
                    serverMain.setServerLogs(this.TheClient.username + "exception reading Streams :" + e);
                    //Server.sendLogToFrame(this.TheClient.username + "exception reading Streams :" + e);
                    break;
                } catch (ClassNotFoundException ex) {
                    serverMain.setServerLogs(this.TheClient.username + "Exception reading Streams " + ex);
                    //Server.sendLogToFrame(this.TheClient.username + "Exception reading Streams " + ex);
                }
            }
            Server.remove(this.TheClient.id);
        }
    }
}
