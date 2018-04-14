package dc_project2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread{
  Object t;
  int port;
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
    boolean successfullyConnected = false;
    while(!successfullyConnected)
    try{
        ServerSocket ss = new ServerSocket(port);
        while(true)
        try {
            Socket s = ss.accept();
            Runnable w = new Thread();
            if(t.getClass().equals(Node.class))
              w = new ClientManager(s, (Node) t);
            else if(t.getClass().equals(Synchronizer.class))
              w = new ClientManagerSynchronizer(s, (Synchronizer) t);
            Thread t = new Thread(w);
            t.start();
            successfullyConnected = true;
        } catch(IOException e) {
            System.out.println("accept failed");
            System.exit(100);
        }		
    } catch(IOException ex) {
        ex.printStackTrace();
    }
  }
}