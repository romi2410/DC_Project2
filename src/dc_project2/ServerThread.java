package dc_project2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.net.Socket;

public class ServerThread extends Thread{
  Process t;
  int port;
  boolean up = false;
  HashSet<Thread> connections = new HashSet<Thread>();
  
  ServerThread(Process t, int port){
    this.t = t;
    this.port = port;
  }
  
  @Override
  public void run(){
    try{
      ServerSocket ss = new ServerSocket(port);
      up = true;
      while(true) try {
        Socket s = ss.accept();
        Printer.print(port + " received connection from " + s);
        //Runnable w = new ClientManager(s, t);
//        Thread t = new Thread(w);
//        t.start();
//        connections.add(t);
      } catch(IOException e) {
        System.out.println("accept failed");
        System.exit(100);
      }		
    } catch(IOException ex) {
      ex.printStackTrace();
    }
  }
}



class ClientManager{// implements Runnable {
  //Socket client;
  Process owner;
  //public ClientManager(Socket client, Process owner) { this.client = client; this.owner = owner; }
  public ClientManager(Process owner) { this.owner = owner; }

//  @Override
//  public void run() {
//    try {
//      Message msg;
//      ObjectInputStream inputStream = new ObjectInputStream(client.getInputStream());
//      try{
//        while (true){
//          while((msg = (Message) inputStream.readObject()) != null)
//            handleMsg(msg);
//        }
//      }catch(ClassNotFoundException e){
//        System.out.println(owner+"'s connection to " + client.toString() + " failed; " + e);
//      } catch(OptionalDataException e) {  e.printStackTrace();  TestingMode.print(owner.uid + " " + e.length + " " + e.eof + " " );}
//    } catch(IOException e) {  e.printStackTrace();  }
//  }

  public void handleMsg(Message m){
    if(!owner.terminated){
      TestingMode.print(owner.uid + " rcvd " + m, owner.uid);
      owner.handleMsg(m);
    }
  }
}