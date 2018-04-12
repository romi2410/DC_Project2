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
import java.util.concurrent.TimeUnit;

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
  HashMap<Integer, MWOEMsg> leaders2MWOE = new HashMap<Integer, MWOEMsg>();
  
  String hostname;
  int port;
  
  HashMap<Integer, String> sendTo = new HashMap<>();
  
  boolean server = false;
  int numEdges = 0;

  public Synchronizer(HashMap<Integer, Node> nodes, String hostname, int port, boolean test){
    for(int nodeUID: nodes.keySet()){
      HashSet<Node> component = new HashSet<Node>();
      component.add(nodes.get(nodeUID));
      leaders2component.put(nodeUID, component);
      sendTo.put(nodeUID, " ");
    }
    this.hostname = (test) ? "localhost" : hostname;
    this.port = port;
    startServer();
    System.out.println("Synchronizer started");
//    for(Node node: nodes.values())
//      startSender(node.hostname, node.port, node.uid);
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
    wantsToMergeWith.put(m.sender, m.externalLeader);
    leaders2MWOE.put(m.sender, m);
    if(wantsToMergeWith.size() == leaders2component.size())
      mergePhase();
  }
  private void mergePhase(){
    for(int leaderFrom: wantsToMergeWith.keySet()){
      int leaderTo = wantsToMergeWith.get(leaderFrom);
      if(wantsToMergeWith.get(leaderTo).equals(leaderFrom))
        resolveMutualMerge(leaderFrom, leaderTo, leaders2MWOE.get(leaderFrom));
    }
    HashMap<Integer, HashSet> leaders2componentNew = new HashMap<Integer, HashSet>();
    for(int leaderFrom: wantsToMergeWith.keySet()){
      int leaderTo = wantsToMergeWith.get(leaderFrom);
      if(leaders2componentNew.keySet().contains(leaderTo))
        leaders2componentNew.get(leaderTo).addAll(leaders2component.get(leaderFrom));
      else
        leaders2componentNew.put(leaderTo, leaders2component.get(leaderFrom));
    }
    leaders2component = leaders2componentNew;   level++;  //update leaders2component and level
    for(int oldLeader: leaders2component.keySet())
      sendTo(oldLeader, new NewLeaderMsg(uid, wantsToMergeWith.get(oldLeader), leaders2MWOE.get(oldLeader)));
    wantsToMergeWith = new HashMap<Integer, Integer>();   //reset wantsToMergeWith
    if(leaders2component.size()==1)
      terminate();
  }
  
  private void resolveMutualMerge(int leaderFrom, int leaderTo, MWOEMsg m){
    int newLeader = Math.max(m.externalNode, m.leafnode); // incident to core edge
    
    wantsToMergeWith.put(leaderFrom, newLeader);
    wantsToMergeWith.put(leaderTo, newLeader);
    for(int leaderFrom2: wantsToMergeWith.keySet())   //everyone who wanted to merge with smallLeader,
      if(wantsToMergeWith.get(leaderFrom2).equals(leaderFrom) || wantsToMergeWith.get(leaderFrom2).equals(leaderTo)) //will now merge with newLeader
        wantsToMergeWith.put(leaderFrom2, newLeader);
  }
  
  private void terminate(){
    TerminateMsg terminateMsg = new TerminateMsg(level, this.uid);
    for(Integer node: sendTo.keySet())
      sendTo(node, terminateMsg);
  }
  
  
  public void connectToNodes(Set<Node> nodes){
    for(Node node: nodes)
      connectTo(node.hostname, node.port, node.uid);
  }
  private void connectTo(String nodeHostname, int nodePort, int nodeUID){
    startSender(nodeHostname, nodePort, nodeUID);
    numEdges++;
  }
  private void startSender(String nodeHostname, int nodePort, int nodeUID){
        boolean successfullyConnected = false;
        while(!successfullyConnected) try {
            Socket s = new Socket(nodeHostname, nodePort);
            System.out.println("Synchronizer is connecting to " + nodePort);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            (new Thread() {
                @Override
                public void run() {
                  while(true){
                    try                 { out.write(sendTo.get(nodeUID)); }
                    catch(IOException e){ e.printStackTrace(); }
                  }
                }
            }).start();
            successfullyConnected = true;
        } catch (UnknownHostException e){ e.printStackTrace();
        } catch (IOException e)         { e.printStackTrace();
        }
        
        System.out.println("Number of threads after starting edge " + uid + ", " + nodeUID + ": " + Thread.activeCount());
        try{
          TimeUnit.SECONDS.sleep(1);
        } catch(InterruptedException e){
          System.out.println(e);
        }
    }
  
    // Update message being sent to neighbor
    public void sendTo(int rcvrUid, Message newMsg){
      newMsg.sender = uid;
      try{
        sendTo.put(rcvrUid, newMsg.serialize());
      } catch (IOException e) {
          System.out.println(e);
          System.out.println("Attempt to serialize " + newMsg.toString() + " failed");
      }
    }
  
}
