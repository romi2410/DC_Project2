package dc_project2;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

// keeps track of all leaders, and what nodes belong to those leaders
  // Hashmap: leaders -> component set
// does merging (broad/convergecast occurs at nodes/GHS)
// 1 server socket, n sender sockets/threads
  // sends new leader + new parent to every node, after receiving all merge requests from all leaders
// handles termination (single leader in system)
public class Synchronizer {
  
  int level = 0;
  HashMap<Integer, HashSet> leaders2component = new HashMap<Integer, HashSet>();
  
  String hostname;
  int port;
  
  HashMap<Integer, BufferedWriter> nodes2sockets  = new HashMap<>();
  HashMap<Integer, String>         nodes2lastsent = new HashMap<>();
  HashMap<Integer, String>         nodes2lastrcvd = new HashMap<>();
  
  boolean server = false;
  int numEdges = 0;

  public Synchronizer(HashMap<Integer, Node> nodes, String hostname, int port){
    for(int uid: nodes.keySet()){
      HashSet<Node> component = new HashSet<Node>();
      component.add(nodes.get(uid));
      leaders2component.put(uid, component);
    }
    startServer();
    for(Node node: nodes.values())
      nodes2sockets.put(node.uid, startSender(node.hostname, node.port, node.uid));
  }
  
  private void startServer(){
        server = true;
        Synchronizer t = this;
        
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
                        ClientManagerSynchronizer w = new ClientManagerSynchronizer(s, t);
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
  public void handleMsg(MWOEMsg m){
    
  }
  
  
  public void connectToNodes(Set<Node> nodes){
    for(Node node: nodes)
      connectTo(node.hostname, node.port, node.uid);
  }
  private void connectTo(String nodeHostname, int nodePort, int nodeUID){
    startSender(nodeHostname, nodePort, nodeUID);
    nodes2lastsent.put(nodeUID, "");
    nodes2lastrcvd.put(nodeUID, "");
    numEdges++;
  }
  private BufferedWriter startSender(String nodeHostname, int nodePort, int nodeUID){
        while(true) try {
            Socket s = new Socket(nodeHostname, nodePort);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            (new Thread() {
                @Override
                public void run() {
                  while(true){
                    try                 { out.write(nodes2lastsent.get(nodeUID)); }
                    catch(IOException e){ e.printStackTrace(); }
                  }
                }
            }).start();
            return out;
        } catch (UnknownHostException e){ e.printStackTrace();
        } catch (IOException e)         { e.printStackTrace();
        }
    }
  
  
}