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
  
  int uid = -1 ; // uniquely identifies synchronization messages
  int level = 0;
  HashMap<Integer, Integer> wantsToMergeWith  = new HashMap<Integer, Integer>();
  HashMap<Integer, HashSet> leaders2component = new HashMap<Integer, HashSet>();
  
  String hostname;
  int port;
  
  HashMap<Integer, String> nodes2lastsent = new HashMap<>();
  HashMap<Integer, String> nodes2lastrcvd = new HashMap<>();
  
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
      startSender(node.hostname, node.port, node.uid);
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
    wantsToMergeWith.put(m.sender, m.mergeWithLeader);
    if(wantsToMergeWith.size() == leaders2component.size())
      mergePhase();
  }
  private void mergePhase(){
    for(int leaderFrom: wantsToMergeWith.keySet()){
      int leaderTo = wantsToMergeWith.get(leaderFrom);
      if(wantsToMergeWith.get(leaderTo).equals(leaderFrom))
        resolveMutualMerge(leaderFrom, leaderTo);
    }
    HashMap<Integer, HashSet> leaders2componentNew = new HashMap<Integer, HashSet>();
    for(int leaderFrom: wantsToMergeWith.keySet()){
      int leaderTo = wantsToMergeWith.get(leaderFrom);
      leaders2componentNew.put(leaderTo, merge(leaderTo, leaderFrom));
    }
    leaders2component = leaders2componentNew;   level++;  //update leaders2component and level
    wantsToMergeWith = new HashMap<Integer, Integer>();   //reset wantsToMergeWith
    if(leaders2component.size()==1)
      terminate();
  }
  private void resolveMutualMerge(int leaderFrom, int leaderTo){
    int bigLeader = Math.max(leaderFrom, leaderTo);
    int smallLeader = Math.min(leaderFrom, leaderTo);
    wantsToMergeWith.put(bigLeader, bigLeader);       //bigLeader points to itself.
    
    for(int leaderFrom2: wantsToMergeWith.keySet())   //everyone who wanted to merge with smallLeader,
      if(wantsToMergeWith.get(leaderFrom2).equals(smallLeader)) //will now merge with bigLeader.
        wantsToMergeWith.put(leaderFrom2, bigLeader);
  }
  private HashSet<Integer> merge(int leader1, int leader2){
    HashSet mergedSet = new HashSet(leaders2component.get(leader1));
    mergedSet.addAll(leaders2component.get(leader2));
    return mergedSet;
  }
  private void terminate(){
    TerminateMsg terminateMsg = new TerminateMsg(level, this.uid);
    for(Integer node: nodes2lastsent.keySet())
      sendTo(node, terminateMsg);
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
  
    // Update message being sent to neighbor
    public void sendTo(int rcvrUid, Message newMsg){
      newMsg.sender = uid;
      try{
        nodes2lastsent.put(rcvrUid, newMsg.serialize());
      } catch (IOException e) {
          System.out.println(e);
          System.out.println("Attempt to serialize " + newMsg.toString() + " failed");
      }
    }
  
}