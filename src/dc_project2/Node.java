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

class Node{
    // node stuff
    int uid;
    int port;
    String hostname;
    
    // neighbor stuff
    HashMap<Integer, Double> neighbors2weights = new HashMap<>();
    HashMap<Integer, BufferedWriter> neighbors2socket = new HashMap<>();
    HashMap<Integer, String> neighbors2lastmsg = new HashMap<>();
    
    // algo stuff
    GHS ghs;
    int round;
    
    // used by DC_Project2 for verification before initiating GHS
    boolean server = false;
    int numEdges = 0;
    
    
    public Node(int u, String hn, int p, boolean test) {
        uid = u;
        port = p;
        hostname = (test) ? "localhost" : hn;
        
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
    
    public void sendTo(int rcvrUid, Object msg){
      String serializedMsg;
      try {
          ByteArrayOutputStream bo = new ByteArrayOutputStream();
          ObjectOutputStream so = new ObjectOutputStream(bo);
          so.writeObject(msg);
          so.flush();
          serializedMsg = bo.toString();
          neighbors2lastmsg.put(rcvrUid, serializedMsg);
      } catch (Exception e) {
          System.out.println(e);
      }
    }
    
    public void initGHS(){
      ghs = new GHS(this);
    }
    
    public void connectTo(String hostname, int port, int u, double w){
        neighbors2socket.put(u, startSender(port, hostname, u));
        neighbors2weights.put(u, w);
        //neighbors2lastmsg.put(u, new BroadcastMessage().toString());
        numEdges++;
    }
    
    private BufferedWriter startSender(int port, String hostname, int neighborUID) {
        while(true) try {
            Socket s = new Socket(hostname, port);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            (new Thread() {
                @Override
                public void run() {
                  while(true){
                    try{out.write(neighbors2lastmsg.get(neighborUID).toString());}
                    catch(IOException e){ e.printStackTrace(); }
                  }
                }
            }).start();
            return out;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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