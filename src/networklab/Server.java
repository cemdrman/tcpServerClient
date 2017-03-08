package networklab;

/**
 *
 * @author cem
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.DefaultListModel;
import javax.swing.text.html.CSS;

public class Server {

    protected static ServerSocket serverSocket;
    private static ServerThread runThread;
    private final int port = 1500;
    protected static ArrayList<Client> connectedClientList;
    protected static ServerMain serverMain = new ServerMain();

    /* private void setServer() {
        try {
            Server.serverSocket = new ServerSocket(port);            
            Server.connectedClientList = new ArrayList<Client>();
            RunThread = new ServerThread();
            RunThread.start();
        } catch (Exception e) {
        }
    }*/
    public int getPort() {
        return port;
    }

    public void start() {
        try {
            Server.serverSocket = new ServerSocket(port);
            Server.connectedClientList = new ArrayList<Client>();
            Server.runThread = new ServerThread();
            runThread.start();

        } catch (Exception e) {
            //serverMain.setServerLogs("server başlatılamadı");
            System.out.println("server başlatılamadı");
        }
    }

    public void stop() {
        try {
            for (int i = connectedClientList.size(); --i >= 0;) {
                Client ct = connectedClientList.get(i);
                ct.close();;
                connectedClientList.remove(i);
            }
            Server.runThread.interrupt();
            Server.serverSocket.close();
            Server.serverSocket = null;
        } catch (Exception e) {
            serverMain.setServerLogs("server kapatılrken bir hata yaşandı!");
            System.out.println("server kapatılrken bir hata yaşandı!");
        }
    }

    public static synchronized void broadcast(String message) {
        for (int i = connectedClientList.size(); --i >= 0;) {
            Client ct = connectedClientList.get(i);
            if (!ct.writeMsg(message)) {
                connectedClientList.remove(i);
                serverMain.setServerLogs(message);
                serverMain.setServerLogs("Disconnected Client " + ct.username + "remove from list");
                //System.out.println("Disconnected Client " + ct.username + "remove from list");
                //sendLogToFrame("Disconnected Client " + ct.username + "remove from list");
            }
        }
    }

    public static synchronized void remove(int id) {
        for (int i = 0; i < connectedClientList.size(); i++) {
            Client ct = connectedClientList.get(i);
            if (ct.id == id) {
                ct.close();
                connectedClientList.remove(i);
                return;
            }
        }
    }
}

class ServerThread extends Thread {

    @Override
    public void run() {
        try {
            while (!Server.serverSocket.isClosed()) {
                Server.serverMain.setServerLogs("server waiting...");
                // Server.sendLogToFrame("server waiting...");
                Socket socket = Server.serverSocket.accept();
                Client newClient = new Client(socket);
                Server.connectedClientList.add(newClient);
                newClient.start();
                
                for (int i = 0; i < Server.connectedClientList.size(); i++) {
                    Server.broadcast(Server.connectedClientList.get(i).username + " baglandı");
                    System.out.println(Server.connectedClientList.get(i).username + " baglandı");
                }
                for (int i = 0; i < Server.connectedClientList.size(); i++) {
                   if( Server.connectedClientList.get(i).username != newClient.username){
                      Server.broadcast(Server.connectedClientList.get(i).username + " baglandı");
                   }
                }
            }
            try {
                Server.serverSocket.close();
                for (int i = 0; i < Server.connectedClientList.size(); i++) {
                    Client tc = Server.connectedClientList.get(i);
                    tc.close();
                }
            } catch (Exception e) {
                // Server.serverMain.setServerLogs("");
                // Server.sendLogToFrame("execption " + e);
            }

        } catch (IOException e) {
            String msg = new Date().toString() + "Exception " + e + "\n";
            Server.serverMain.setServerLogs(msg);
            // Server.sendLogToFrame(msg);
        }
    }

}
