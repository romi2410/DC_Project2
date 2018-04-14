package dc_project2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;



public class Synchronizer {
  
  int uid = -1;
  int level = 0;
  String hostname;
  int port;
  
  // use for synchronizing GHS start
  boolean serverUp = false;
  boolean sendersUp = false;
  
  HashMap<Integer, LeaderToken> leaders  = new HashMap<Integer, LeaderToken>();
  HashMap<Integer, Sender> senders = new HashMap<Integer, Sender>();
  

  public Synchronizer(HashMap<Integer, Node> nodes, String hostname, int port){
    System.out.println("Inside Synchronizer constructor");
    for(int nodeUID: nodes.keySet())
      leaders.put(nodeUID, new LeaderToken(nodeUID));
    this.hostname = (TestingMode.isOn()) ? "localhost" : hostname;
    this.port = port;
    startServer();
    if(TestingMode.isOn()) startPrintThread();
    System.out.println("Terminating Synchronizer constructor");
  }
  
  private void startServer(){
    ServerThread server = new ServerThread(this, port);
    server.start();
    while(!server.up){ Wait.aSec(); }
    serverUp = true;
  }
  
  public void handleMsg(MWOEMsg m){
    LeaderToken sender = leaders.get(m.sender);
    sender.handleMWOEMsg(m);
    if(BooleanCollection.allTrue(leaders.values(), LeaderToken.rcvdMsg())){
      leaders = new MergePhase(leaders).getLeaders();
      broadcastNewLeaders();
      level++;
      if(leaders.size()==1)
        terminate();
    }
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
    for(Node node: nodes)
      senders.put(node.uid, new Sender(node.hostname, node.port, uid));
    while(!BooleanCollection.allTrue(senders.values(), Sender.successfullyConnected())){Wait.aSec();}
    sendersUp = true;
  }
  
  private void startPrintThread(){
    (new Thread() {
      @Override
      public void run() {
        while(true){
          Wait.thirtySeconds();
          printAll();
        }
      }
    }).start();
  }
  private void printAll(){
    TestingMode.print(String.valueOf(level));
    for(LeaderToken leader: leaders.values())
      TestingMode.print(leader.toString());
    //for(Sender sender: senders.values())
    //  System.out.println(sender.toString());
  }
}




  /* --- HELPER CLASSES --- */


class LeaderToken{
  int uid, wantsToMergeWith;
  HashSet<Integer> component = new HashSet<Integer>();
  MWOEMsg mwoe;
  boolean rcvdMsg = false;
  public static Predicate<LeaderToken> rcvdMsg(){ return leader->leader.rcvdMsg; }
  
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
  @Override
  public String toString(){
    StringJoiner sj = new StringJoiner(" ");
    sj.add(String.valueOf(uid)).add(" has component <");
    for (int node : component) 
      sj.add(String.valueOf(node));
    sj.add(">\t");
    
    if(rcvdMsg){
      sj.add(" and wants to merge with").add(String.valueOf(wantsToMergeWith));
      sj.add("\t\tits MWOEMsg is\t").add(mwoe.toString());
    }
    else
      sj.add(" and has not send a MWOEMsg yet this level");
    return sj.toString();
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
