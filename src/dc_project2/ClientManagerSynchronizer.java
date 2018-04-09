package dc_project2;

import java.net.*;
import java.io.*;

public class ClientManagerSynchronizer implements Runnable {
	
    private Socket client;
    Synchronizer owner;

    public ClientManagerSynchronizer(Socket client, Synchronizer owner) {
      this.client = client;
      this.owner = owner;
    }

    public ClientManagerSynchronizer(Socket client) {
      this.client = client;
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
         owner.handleMsg(message);
       } catch (Exception e) {
           System.out.println(e);
       }
    }
}