package dc_project2;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread{
  Object t;
  int port;
  boolean successfullyConnected = false;
  
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



class ClientManager implements Runnable {
	
    private Socket client;
    Node owner;

    public ClientManager(Socket client, Node owner) {
      this.client = client;
      this.owner = owner;
    }

    @Override
    public void run() {
      try {
        String line;
        BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream()));

        while ((line = in.readLine()) != null){ handleMsg(line); }
	} catch(IOException e) {
            e.printStackTrace();
      }
    }
    
    public void handleMsg(String m){
      Object message;
      try {
         ByteArrayInputStream bi = new ByteArrayInputStream(m.getBytes());
         ObjectInputStream si = new ObjectInputStream(bi);
         message = si.readObject();
         owner.ghs.handleMsg(message);
       } catch (Exception e) {
           System.out.println(e);
       }
    }
}

class ClientManagerSynchronizer implements Runnable {
    private Socket client;
    Synchronizer sync;

    public ClientManagerSynchronizer(Socket client, Synchronizer sync) {
      this.client = client;
      this.sync = sync;
    }

    @Override
    public void run() {
      try {
            String line;
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            
            while ((line = in.readLine()) != null){
                handleMsg(line);
            }
	} catch(IOException e) {
            e.printStackTrace();
      }
    }
    
    public void handleMsg(String m){
      MWOEMsg message;
      try {
         ByteArrayInputStream bi = new ByteArrayInputStream(m.getBytes());
         ObjectInputStream si = new ObjectInputStream(bi);
         message =(MWOEMsg) si.readObject();
         sync.handleMsg(message);
       } catch (Exception e) {
           System.out.println(e);
       }
    }
}