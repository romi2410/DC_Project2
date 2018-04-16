package dc_project2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.Arrays;


public class Synchronizer extends Process{
  
  int uid = -1;
  int level = 0;
  String hostname;
  int port;
  
  // use for synchronizing GHS start
  boolean serverUp = false;
  boolean sendersUp = false;
  
  HashMap<Integer, LeaderToken> leaders  = new HashMap<Integer, LeaderToken>();
  HashMap<Integer, Sender> senders = new HashMap<Integer, Sender>();
  
  HashMap<Integer, HashSet<Integer>> newNbrs = new HashMap<Integer, HashSet<Integer>>();
  HashMap<Integer, Boolean> ackedNewLeader = new HashMap<Integer, Boolean>();
  
  public Synchronizer(HashMap<Integer, Node> nodes, String hostname, int port){
    TestingMode.print("new Synchronizer at thread " + Thread.currentThread().getName());
    for(int nodeUID: nodes.keySet())
      leaders.put(nodeUID, new LeaderToken(nodeUID));
    this.hostname = (TestingMode.isOn()) ? "localhost" : hostname;
    this.port = port;
    startServer();
    if(TestingMode.isOn()) TestingMode.startPrintThread(this);
  }
  
  private void startServer(){
    ServerThread server = new ServerThread(this, port);
    server.start();
    while(!server.up){ Wait.threeSeconds(); }
    serverUp = true;
  }
  
  public synchronized void handleMsg(Message msg){
    synchronized(this){
      Class msgType = msg.getClass();
      if(msgType == NewLeaderAckMsg.class)
        handleNewLeaderAckMsg((NewLeaderAckMsg) msg);
      else if(msgType == MWOEMsg.class)    
        handleMWOEMsg((MWOEMsg) msg);
    }
  }
  public void handleNewLeaderAckMsg(NewLeaderAckMsg m){  ackedNewLeader.put(m.sender, true); }
  public void handleMWOEMsg(MWOEMsg m){
    if(!leaders.keySet().contains(m.sender)) return;  // don't accept messages from non-leaders
    LeaderToken leader = leaders.get(m.sender);
    leader.handleMWOEMsg(m);
    addMWOEtoNewNbrs(m.externalNode, m.leafnode);
    if(BooleanCollection.allTrue(leaders.values(), LeaderToken.rcvdMsg())){
      leaders = new MergePhase(leaders).getLeaders();
      broadcastNewLeaders();
      level++;
      if(leaders.size()==1)
        terminate();
    }
  }
  private void addMWOEtoNewNbrs(int nodeA, int nodeB){
    newNbrs.put(nodeA, newNbrs.getOrDefault(nodeA, new HashSet<Integer>()));
    newNbrs.get(nodeA).add(nodeB);
    newNbrs.put(nodeB, newNbrs.getOrDefault(nodeB, new HashSet<Integer>()));
    newNbrs.get(nodeB).add(nodeA);
  }
  
  private void broadcastNewLeaders(){
    for(LeaderToken leader: leaders.values())
      for(int node: leader.component){
        ackedNewLeader.put(node, false);
        senders.get(node).send(new NewLeaderMsg(uid, leader.uid, newNbrs.getOrDefault(node, new HashSet<Integer>())));
      }
    while(!Arrays.asList(ackedNewLeader.values()).contains(false)){}
    for(LeaderToken leader: leaders.values()){
      senders.get(leader).send(new NewSearchPhaseMsg(-1));
      leader.resetRcvdMsg();
    }
    newNbrs.clear();
  }
  
  
  private void terminate(){
    TerminateMsg terminateMsg = new TerminateMsg(level, this.uid);
    for(Integer node: senders.keySet())
      senders.get(node).send(terminateMsg);
  }
  
  public void connectToNodes(Set<Node> nodes){
    for(Node node: nodes)
      senders.put(node.uid, new Sender(node.hostname, node.port, uid));
    while(!BooleanCollection.allTrue(senders.values(), Sender.successfullyConnected())){Wait.threeSeconds();}
    sendersUp = true;
  }
  
  public String toString(){
    StringJoiner sj = new StringJoiner("\n");
    sj.add("Level: " + level);
    for(LeaderToken leader: leaders.values())
      sj.add(leader.toString());
    return sj.toString();
  }
}




  /* --- HELPER CLASSES --- */


class LeaderToken{
  int uid, wantsToMergeWith=0;
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
  public HashMap<Integer, LeaderToken> getLeaders(){ return leaders; }
  
  public MergePhase(HashMap<Integer, LeaderToken> leadersToMerge){
    TestingMode.print("Synchronizer is merging the following leaders: ");
    for(LeaderToken leader: leadersToMerge.values())
      TestingMode.print(leader.uid+" ");
    TestingMode.print("\n");
    
    HashMap<Integer, LeaderToken> mergedLeaders = mergeLeaders(leadersToMerge);
    leaders = absorbLeaders(leadersToMerge, mergedLeaders);
  }
  
  private HashMap<Integer, LeaderToken> mergeLeaders(HashMap<Integer, LeaderToken> leaders){
    HashMap<Integer, LeaderToken> newLeaders = new HashMap<Integer, LeaderToken>();
    
    Iterator<LeaderToken> leaderIter = leaders.values().iterator(); 
    while(leaderIter.hasNext()){
      LeaderToken leaderFrom = leaderIter.next();
      MWOEMsg m = leaderFrom.mwoe;
      LeaderToken leaderTo = leaders.get(leaderFrom.wantsToMergeWith);
      if(leaderTo.wantsToMergeWith == leaderFrom.uid){
        int newLeaderUID = Math.max(m.externalNode, m.leafnode);
        LeaderToken newLeader = new LeaderToken(newLeaderUID, leaderFrom, leaderTo);
        leaderFrom.wantsToMergeWith = newLeaderUID; newLeaders.put(leaderFrom.uid, leaderFrom);
        leaderTo.wantsToMergeWith = newLeaderUID;   newLeaders.put(leaderTo.uid, leaderTo);
        for(LeaderToken leader: leaders.values())
          if(leader.wantsToMergeWith == leaderFrom.uid || leader.wantsToMergeWith == leaderTo.uid){
            leader.wantsToMergeWith = newLeaderUID;   newLeaders.put(leader.uid, leader);
          }
        newLeaders.put(newLeaderUID, newLeader);
      }
    }
    return newLeaders;
  }
  private HashMap<Integer, LeaderToken> absorbLeaders(HashMap<Integer, LeaderToken> leaders, HashMap<Integer, LeaderToken> mergedLeaders){
    for(LeaderToken leader: leaders.values()){
      while(!mergedLeaders.containsKey(leader.wantsToMergeWith))
        leader.wantsToMergeWith = leaders.get(leader.wantsToMergeWith).wantsToMergeWith;
      mergedLeaders.get(leader.wantsToMergeWith).component.addAll(leader.component);
    }
    return mergedLeaders;
  }
  
  private void merge(LeaderToken leaderA, LeaderToken leaderB, MWOEMsg m){
    // new leader will be the larger of the two nodes incident to the core edge
    int newLeaderUID = Math.max(m.externalNode, m.leafnode);
    LeaderToken newLeader = new LeaderToken(newLeaderUID, leaderA, leaderB);
    
    //everyone who wanted to absorb into leaderA or leaderB
      //will now absorb into newLeader
    leaderA.wantsToMergeWith = newLeaderUID;
    leaderB.wantsToMergeWith = newLeaderUID;
    for(LeaderToken leader: leaders.values())
      if(leader.wantsToMergeWith == leaderA.uid || leader.wantsToMergeWith == leaderB.uid)
        leader.wantsToMergeWith = newLeaderUID;
    
    // execute merge
    leaders.remove(leaderA.uid);
    leaders.remove(leaderB.uid);
    leaders.put(newLeaderUID, newLeader);
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
