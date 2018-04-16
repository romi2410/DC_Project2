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
        System.out.println(t.uid + " accepted from " + s);
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
//        while ((msg = (Message) inputStream.readObject()) != null){
        while (true){
          msg = (Message) inputStream.readObject();
          handleMsg(msg);
          System.out.println(owner.uid + " server connection to " + client + " still alive");
          //Wait.threeSeconds();
        }
//        TestingMode.print(owner.uid + "Received null from " + client.toString());
      }catch(ClassNotFoundException e){
        System.out.println(owner+"'s connection to " + client.toString() + " failed; " + e);
      }
    } catch(IOException e) {  e.printStackTrace();  }
  }

  public void handleMsg(Message m){
    if(owner.getClass() == Synchronizer.class)
      System.out.print(owner.uid + " rcvd (ServerThread) " + m);
      //TestingMode.print(owner.uid + " rcvd (ServerThread) " + m);
    owner.handleMsg(m);
  }
}