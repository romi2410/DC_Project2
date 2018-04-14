package dc_project2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
      server = true;        
      (new ServerThread(this, port)).start();
    }
    public void connectTo(String nbrhostname, int nbrport, int nbrUID, double w){
      neighbors2lastsent.put(nbrUID, "");
      neighbors2weights.put(nbrUID, w);
      boolean successfullyConnected = startSender(nbrport, nbrhostname, nbrUID);
      while(!successfullyConnected){ Wait.aSec();}
      numEdges++;
    }
    private boolean startSender(int nbrport, String nbrhostname, int nbrUID) {
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
                  Wait.aSec();
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
      
      return successfullyConnected;
    }
    public void connectToSynchronizer(String syncHostname, int syncPort){
        boolean successfullyConnected = false;
        while(!successfullyConnected) try {
            Socket s = new Socket(syncHostname, syncPort);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            (new Thread() {
                @Override
                public void run() {
                  while(true){
                    try                   { out.write(sendToSynchronizer); }
                    catch(IOException e)  { e.printStackTrace(); }
                    Wait.aSec();
                  }
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