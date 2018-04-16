package dc_project2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread{
  Process t;
  int port;
  boolean up = false;
  
  ServerThread(Node t, int port){
    this.t = t;
    this.port = port;
  }
  ServerThread(Synchronizer t, int port){
    this.t = t;
    this.port = port;
  }
  
  @Override
  public void run(){
    try{
      ServerSocket ss = new ServerSocket(port);
      TestingMode.print("Opening up new ServerSocket at port " + port);
      up = true;
      while(true) try {
        Socket s = ss.accept();
        Runnable w = new Thread();
        w = new ClientManager(s, t);
        Thread t = new Thread(w);
        t.start();
      } catch(IOException e) {
        System.out.println("accept failed");
        System.exit(100);
      }		
    } catch(IOException ex) {
      ex.printStackTrace();
    }
  }
}



class ClientManager implements Runnable {
  Socket client;
  Process owner;
  public ClientManager(Socket client, Process owner) { this.client = client; this.owner = owner; }

  @Override
  public void run() {
    try {
      Message msg;
      ObjectInputStream inputStream = new ObjectInputStream(client.getInputStream());
      try{
        while ((msg = (Message) inputStream.readObject()) != null){ handleMsg(msg); }
      }catch(ClassNotFoundException e){
        System.out.println(owner+"'s connection to " + client.toString() + " failed; " + e);
      }
    } catch(IOException e) {  e.printStackTrace();  }
  }

  public void handleMsg(Message m){
    TestingMode.print(String.valueOf(owner)+ " rcvd " + m);
    owner.handleMsg(m);
  }
}