package dc_project2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class Node{
    // node stuff
    int uid;
    int port;
    String hostname;
    GHS ghs;
    
    // neighbor stuff
    private HashMap<Integer, Double> neighbors2weights  = new HashMap<>();
    private HashMap<Integer, String> neighbors2lastsent = new HashMap<>();
    public  Set<Integer>             neighbors()        { return neighbors2weights.keySet(); }
    public  double                   getWeight(int nbr) { return neighbors2weights.get(nbr); }
    private String                   sendToSynchronizer = "";
    
    // used by DC_Project2 for verification before initiating GHS
    boolean server = false;
    int numEdges = 0;
    
    
    public Node(int u, String hn, int p) {
        uid = u;  port = p;
        hostname = (TestingMode.isOn()) ? "localhost" : hn;
        startServer();
    }
    private void startServer() {
        server = true;
        Node t = this;
        
        (new Thread() {
            @Override
            public void run() 
            {
                boolean successfullyConnected = false;
                while(!successfullyConnected)
                try{
                    ServerSocket ss = new ServerSocket(port);
                    while(true)
                    try {
                        Socket s = ss.accept();
                        ClientManager w = new ClientManager(s, t);
                        Thread t = new Thread(w);
                        t.start();
                        //successfullyConnected = true;
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
    public void connectTo(String nbrhostname, int nbrport, int nbrUID, double w){
      neighbors2lastsent.put(nbrUID, "");
      startSender(nbrport, nbrhostname, nbrUID);
      neighbors2weights.put(nbrUID, w);
      numEdges++;
    }
    private void startSender(int nbrport, String nbrhostname, int nbrUID) {
        boolean successfullyConnected = false;
        while(!successfullyConnected) try {
            Socket s = new Socket(nbrhostname, nbrport);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            (new Thread() {
                @Override
                public void run() {
                  while(true){
                    try{out.write(neighbors2lastsent.get(nbrUID));}
                    catch(IOException e){ e.printStackTrace(); }
                  }
                }
            }).start();
            successfullyConnected = true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        TestingMode.print("Number of threads after starting edge " + uid + ", " + nbrUID + ": " + TestingMode.threadCount());
        try{
          TimeUnit.SECONDS.sleep(1);
        } catch(InterruptedException e){
          System.out.println(e);
        }
    }
    public void connectToSynchronizer(String syncHostname, int syncPort){
        boolean successfullyConnected = false;
        while(!successfullyConnected) try {
            Socket s = new Socket(syncHostname, syncPort);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            (new Thread() {
                @Override
                public void run() {
                  while(true)
                    try                   { out.write(sendToSynchronizer); }
                    catch(IOException e)  { e.printStackTrace(); }
                }
            }).start();
            successfullyConnected = true;
            numEdges++;
        } catch (UnknownHostException e)  { e.printStackTrace();
        } catch (IOException e)           { e.printStackTrace();
        }
    }
    public void initGHS(){
      ghs = new GHS(this);
    }
    
    
    // Update message being sent to neighbor
    public void sendTo(int rcvrUid, Message newMsg){
      newMsg.sender = uid;
      try{
        if(rcvrUid==-1) // -1 = synchronizer uid
          sendToSynchronizer = newMsg.serialize();
        else
          neighbors2lastsent.put(rcvrUid, newMsg.serialize());
      } catch (IOException e) {
          System.out.println(e);
          System.out.println("Attempt to serialize " + newMsg.toString() + " failed");
      }
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        sb.append(uid).append(" ");
        sb.append(hostname).append(" ");
        sb.append(port).append(" ");
        
        for(int neighbor: neighbors2weights.keySet())
            sb.append(neighbor).append("    ");
        
        return sb.toString();
    }  
}