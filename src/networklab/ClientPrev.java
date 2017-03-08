/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklab;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author cem
 */
public class ClientPrev {
    private Socket socket;
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private ServerListenThread ListenThread;
    private String server;
    private int port;
    private String username;
    
    ServerMain serverMain = new ServerMain();
    
    public ClientPrev(String server, int port, String username){
        this.server=server;
        this.port=port;
        this.username=username;        
    }
    
    public boolean start(){
        try {
            socket=new Socket(this.server,this.port);
            sInput=new ObjectInputStream(socket.getInputStream());
            sOutput=new ObjectOutputStream(socket.getOutputStream());
            this.ListenThread=new ServerListenThread();
            this.ListenThread.start();
            String msg="Connection accepted "+socket.getInetAddress()+":"+socket.getPort();
            serverMain.setServerLogs(msg);
        } catch (Exception e) {
            serverMain.setServerLogs("Error connecting to server: "+e);
            System.out.println("Error connecting to server: "+e);
        }
        try {
            sOutput.writeObject(username);
        } catch (Exception e) {
            System.out.println("Error doing login: "+e);
            disconnect();
            return false;
        }
        return true;
    }
    public void display1(String msg){
        System.out.println(msg);
    }
    public void sendMessage(String msg){
        try {
            sOutput.writeObject(msg);
        } catch (Exception e) {
            System.out.println("Exception writing to server: "+e);
        }
    }
    
    public void sendMessage(Object msg){
        try {
            sOutput.writeObject(msg);
        } catch (Exception e) {
        }
    }
    
    public void disconnect(){
        try {
            if (sInput != null) {
                sInput.close();
            }
            if (sOutput != null) {
                sOutput.close();
            }
            if (this.ListenThread != null) {
                this.ListenThread.interrupt();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
        }
    }
    
    class ServerListenThread extends Thread{
        public void run(){
            while(true){
                try {
                    Object msg=sInput.readObject();
                    if (msg instanceof String) {
                        String message=msg.toString();
                        System.out.println(message);
                    }
                } catch (Exception e) {
                    System.out.println("Server Kapatıldı "+e);
                    break;
                }
            }
        }
    }     
}
