package dc_project2;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;

class Node{
    // node stuff
    int uid;
    int port;
    String hostname;
    
    // neighbor stuff
    private HashMap<Integer, Double> neighbors2weights  = new HashMap<>();
    private HashMap<Integer, String> neighbors2lastsent = new HashMap<>();
    private HashMap<Integer, String> neighbors2lastrcvd = new HashMap<>();
    public  Set<Integer>             neighbors()        { return neighbors2weights.keySet(); }
    public  double                   getWeight(int nbr) { return neighbors2weights.get(nbr); }
    
    // algo stuff
    GHS ghs;
    
    // synchronizer stuff
    String sendToSynchronizer = "";
    
    // used by DC_Project2 for verification before initiating GHS
    boolean server = false;
    int numEdges = 0;
    
    
    public Node(int u, String hn, int p, boolean test) {
        uid = u;  port = p;   hostname = (test) ? "localhost" : hn;
        System.out.println("Node " + uid + " started");
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
    public void connectTo(String nbrhostname, int nbrport, int nbrUID, double w){
      startSender(nbrport, nbrhostname, nbrUID);
      neighbors2weights.put(nbrUID, w);
      neighbors2lastsent.put(nbrUID, "");
      neighbors2lastrcvd.put(nbrUID, "");
      numEdges++;
    }
    private void startSender(int nbrport, String nbrhostname, int nbrUID) {
        while(true) try {
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
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void connectToSynchronizer(String syncHostname, int syncPort){
        while(true) try {
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
      String prevMsg = neighbors2lastsent.get(rcvrUid);  // use prevMsg if newMsg serialization fails
      neighbors2lastsent.put(rcvrUid, serialize(newMsg, prevMsg));
    }
    public void sendToSynchronizer(Message newMsg){
      newMsg.sender = uid;
      String prevMsg = sendToSynchronizer;  // use prevMsg if newMsg serialization fails
      sendToSynchronizer = serialize(newMsg, prevMsg);
    }
    private String serialize(Message msg, String defaultStr){
      ByteArrayOutputStream bo = new ByteArrayOutputStream();
      try {
          ObjectOutputStream so = new ObjectOutputStream(bo);
          so.writeObject(msg);
          so.flush();
          return so.toString();
      } catch (Exception e) {
          System.out.println(e);
          return defaultStr;
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