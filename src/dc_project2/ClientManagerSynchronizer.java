package dc_project2;

import java.net.*;
import java.io.*;

public class ClientManagerSynchronizer implements Runnable {
    private Socket client;
    Synchronizer sync;

    public ClientManagerSynchronizer(Socket client, Synchronizer sync) {
      this.client = client;
      this.sync = sync;
      System.out.println(sync.uid + " is connected to " + client.toString());
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
         sync.handleMsg(message);
       } catch (Exception e) {
           System.out.println(e);
       }
    }
}