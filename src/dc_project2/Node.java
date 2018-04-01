package dc_project2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

class Node{
    int uid;
    int port;
    String hostname;
    HashMap<Integer, Double> neighbors2weights = new HashMap<Integer, Double>();
    int round;
    int leader;
    boolean server = false;
    
    public Node(int u, String hn, int p, boolean test) {
        leader = u;
        uid = u;
        System.out.println("Node " + uid + " started");
        if(test){
          hostname = "localhost";
        }
        else
          hostname = hn;
        port = p;
        startServer();
    }
    
    public void connectTo(String hostname, int port, int u, double w){
      startSender(port, hostname, u);
      neighbors2weights.put(u, w);
    }

    public void startSender(int port, String hostname, int neighborUID) {
        (new Thread() {
            @Override
            public void run() {
              boolean successfullyConnected = false;
              while(!successfullyConnected)
                try {
                    Socket s = new Socket(hostname, port);
                    successfullyConnected = true;
                    BufferedWriter out = new BufferedWriter(
                            new OutputStreamWriter(s.getOutputStream()));
                    while (true) {
                        // SENDER LOGIC GOES HERE
                        out.newLine();
                        out.flush();
                        Thread.sleep(200);
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void startServer() {
        server = true;
        Node t = this;
        (new Thread() {
            @Override
            public void run() {
              boolean successfullyConnected = false;
              while(!successfullyConnected)
              try	{
                  ServerSocket ss = new ServerSocket(port);
                  while(true)
                    try {
                        Socket s = ss.accept();
                        ClientManager w = new ClientManager(s, t);
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
        }).start();
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(uid+" ");
        sb.append(hostname+" ");
        sb.append(port+" ");
        for(int neighbor: neighbors2weights.keySet())
            sb.append(neighbor+"    ");
        return sb.toString();
    }
    
    public boolean isLeader(){
      return uid==leader;
    }
   
}