package dc_project2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

// keeps track of all leaders, and what nodes belong to those leaders
  // Hashmap: leaders -> component set
// does merging (broad/convergecast occurs at nodes/GHS)
// 1 server socket, n sender sockets/threads
  // sends new leader + new parent to every node, after receiving all merge requests from all leaders
// handles termination (single leader in system)
public class Synchronizer {
  
  int uid = -1;
  int level = 0;
  String hostname;
  int port;
  boolean server = false;   int numEdges = 0;   // use for synchronizing GHS start
  
  HashMap<Integer, LeaderToken> leaders  = new HashMap<Integer, LeaderToken>();
  HashMap<Integer, Sender> senders = new HashMap<>();
  

  public Synchronizer(HashMap<Integer, Node> nodes, String hostname, int port){
    for(int nodeUID: nodes.keySet())
      leaders.put(nodeUID, new LeaderToken(nodeUID));
    this.hostname = (TestingMode.isOn()) ? "localhost" : hostname;
    this.port = port;
    server = true;
    (new ServerThread(this, port)).start();
  }
  
  public void handleMsg(MWOEMsg m){
    LeaderToken sender = leaders.get(m.sender);
    sender.handleMWOEMsg(m);
    if(rcvdAllMsgs()){
      leaders = new MergePhase(leaders).getLeaders();
      broadcastNewLeaders();
      level++;
      if(leaders.size()==1)
        terminate();
    }
  }
  private boolean rcvdAllMsgs(){
    Predicate<LeaderToken> rcvdMsg = leader -> leader.rcvdMsg;
    return leaders.values().stream().allMatch(rcvdMsg);
  }
  
  private void broadcastNewLeaders(){
    for(LeaderToken leader: leaders.values())
      for(int node: leader.component)
        senders.get(node).send(new NewLeaderMsg(uid, leader.uid, leader.mwoe));
    for(LeaderToken leader: leaders.values())
      leader.resetRcvdMsg();
  }
  
  private void terminate(){
    TerminateMsg terminateMsg = new TerminateMsg(level, this.uid);
    for(Integer node: senders.keySet())
      senders.get(node).send(terminateMsg);
  }
  
  public void connectToNodes(Set<Node> nodes){
    for(Node node: nodes){
      startSender(node.hostname, node.port, node.uid);
      numEdges++;
    }
  }
  private void startSender(String nodeHostname, int nodePort, int nodeUID){
    senders.put(nodeUID, new Sender(nodeHostname, nodePort, uid));
  }
}


class Sender{
  String serializedMsg = " ";
  int ownerUID;
  
  Sender(String nodeHostname, int nodePort, int ownerUID){
    boolean successfullyConnected = false;
    this.ownerUID = ownerUID;
    while(!successfullyConnected) try {
        Socket s = new Socket(nodeHostname, nodePort);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        (new Thread() {
            @Override
            public void run() {
              while(true){
                try                 { out.write(serializedMsg); }
                catch(IOException e){ e.printStackTrace(); }
              }
            }
        }).start();
        successfullyConnected = true;
      } catch (UnknownHostException e){ e.printStackTrace();
      } catch (IOException e)         { e.printStackTrace();
    }

    TestingMode.print("Number of threads after starting edge " + ownerUID + ", " + nodePort + ": " + TestingMode.threadCount());
    try{
      TimeUnit.SECONDS.sleep(1);
    } catch(InterruptedException e){
      System.out.println(e);
    }
  }
  
  public void send(Message msg){
    msg.sender = ownerUID;
    try{
      serializedMsg = msg.serialize();
    } catch (IOException e) {
      System.out.println(e);
      System.out.println("Attempt to serialize " + msg.toString() + " failed");
    }
  }
}


class LeaderToken{
  int uid, wantsToMergeWith;
  HashSet<Integer> component = new HashSet<Integer>();
  MWOEMsg mwoe;
  boolean rcvdMsg = false;
  public LeaderToken(int uid){
    this.uid = uid;   component.add(uid);
  }
  public LeaderToken(int uid, LeaderToken oldLeaderA, LeaderToken oldLeaderB){
    this(uid);
    component.addAll(oldLeaderA.component);
    component.addAll(oldLeaderB.component);
  }
  public void handleMWOEMsg(MWOEMsg m){
    wantsToMergeWith = m.externalLeader;
    mwoe = m;
    rcvdMsg = true;
  }
  public void resetRcvdMsg(){
    rcvdMsg = false;
  }
}


class MergePhase{
  HashMap<Integer, LeaderToken> leaders;
  public HashMap<Integer, LeaderToken> getLeaders(){
    return leaders;
  }
  
  public MergePhase(HashMap<Integer, LeaderToken> leadersToMerge){
    leaders = leadersToMerge;
    
    // Iterator is used for safety for deleting elements from leaders while iterating over its elements
    Iterator<LeaderToken> leaderIter = leaders.values().iterator(); 
    while(leaderIter.hasNext()){
      LeaderToken leaderFrom = leaderIter.next();
      LeaderToken leaderTo = leaders.get(leaderFrom.wantsToMergeWith);
      if(leaderTo.wantsToMergeWith == leaderFrom.uid)
        merge(leaderFrom, leaderTo, leaderFrom.mwoe);
      else
        absorb(leaderFrom, leaderTo);
    }
  }
  
  private void merge(LeaderToken leaderA, LeaderToken leaderB, MWOEMsg m){
    // new leader will be the larger of the two nodes incident to the core edge
    int newLeaderUID = Math.max(m.externalNode, m.leafnode);
    leaders.put(newLeaderUID, new LeaderToken(newLeaderUID, leaderA, leaderB));
    
    leaderA.wantsToMergeWith = newLeaderUID;
    leaderB.wantsToMergeWith = newLeaderUID;
    
    //everyone who wanted to absorb into leaderA or leaderB
      //will now absorb into newLeader
    for(LeaderToken leader: leaders.values())
      if(leader.wantsToMergeWith == leaderA.uid || leader.wantsToMergeWith == leaderB.uid)
        leader.wantsToMergeWith = newLeaderUID;
    
    leaders.remove(leaderA.uid);
    leaders.remove(leaderB.uid);
  }
  
  private void absorb(LeaderToken leaderFrom, LeaderToken leaderTo){
    leaderTo.component.addAll(leaderFrom.component);
    
    //everyone who wanted to absorb into leaderFrom
      //will now merge with leaderTo
    for(LeaderToken leader: leaders.values())
      if(leader.wantsToMergeWith == leaderFrom.uid)
        leader.wantsToMergeWith = leaderTo.uid;
    
    leaders.remove(leaderFrom.uid);
  }
}
