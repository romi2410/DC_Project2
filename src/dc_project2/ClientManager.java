package dc_project2;

import java.net.*;
import java.io.*;

public class ClientManager implements Runnable {
	
    private Socket client;
    Node owner;

    public ClientManager(Socket client, Node owner) {
      this.client = client;
      this.owner = owner;
      System.out.println(owner.uid + " is connected to " + client.toString());
    }

    public ClientManager(Socket client) {
	this.client = client;
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