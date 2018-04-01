package dc_project2;

import java.net.*;
import java.io.*;
import java.lang.*;

public class ClientManager implements Runnable {
	
	private Socket client;
    Node owner;

	public ClientManager(Socket client, Node owner) {
		this.client = client;
        this.owner = owner;
	}

	public ClientManager(Socket client) {
		this.client = client;
	}

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
      // SERVER LOGIC HERE
    }
}