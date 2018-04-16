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
    if(!leaders.keySet().contains(m.sender)) return;  // don't accept messages from non-mergedLeaders
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
  
  public LeaderToken(int uid){  this.uid = uid;   component.add(uid); }
  public LeaderToken(LeaderToken oldLeaderA, LeaderToken oldLeaderB){
    this(Math.max(oldLeaderA.mwoe.externalNode, oldLeaderA.mwoe.leafnode));
    component.addAll(oldLeaderA.component);
    component.addAll(oldLeaderB.component);
  }
  public void handleMWOEMsg(MWOEMsg m){
    wantsToMergeWith = m.externalLeader;
    mwoe = m;
    rcvdMsg = true;
  }
  public void    resetRcvdMsg()                      { rcvdMsg = false;                     }
  public boolean wantsToMergeWith(LeaderToken leader){ return wantsToMergeWith==leader.uid; }
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
  HashMap<Integer, LeaderToken> mergedLeaders;
  public HashMap<Integer, LeaderToken> getLeaders(){ return mergedLeaders; }
  
  public MergePhase(HashMap<Integer, LeaderToken> leadersToMerge){
    TestingMode.print("Synchronizer is merging the following leaders: ");
    for(LeaderToken leader: leadersToMerge.values())
      TestingMode.print(leader.uid+" ");
    
    mergedLeaders = new HashMap<Integer, LeaderToken>();
    mergeLeaders(leadersToMerge);
    absorbLeaders(leadersToMerge);
  }
  
  private void mergeLeaders(HashMap<Integer, LeaderToken> leadersToMerge){
    for(LeaderToken leaderFrom: leadersToMerge.values()){
      LeaderToken leaderTo = leadersToMerge.get(leaderFrom.wantsToMergeWith);
      if(leaderTo.wantsToMergeWith(leaderFrom))
        merge(leaderFrom, leaderTo, leadersToMerge);
    }
  }
  private void merge(LeaderToken leaderFrom, LeaderToken leaderTo, 
        HashMap<Integer, LeaderToken> leadersToMerge){
    LeaderToken mergedLeader = new LeaderToken(leaderFrom, leaderTo);
    leaderFrom.wantsToMergeWith = mergedLeader.uid; mergedLeaders.put(leaderFrom.uid, leaderFrom);
    leaderTo.wantsToMergeWith = mergedLeader.uid;   mergedLeaders.put(leaderTo.uid, leaderTo);
    for(LeaderToken leader: leadersToMerge.values())
      if(leader.wantsToMergeWith(leaderFrom) || leader.wantsToMergeWith(leaderTo))          {
        leader.wantsToMergeWith = mergedLeader.uid; mergedLeaders.put(leader.uid, leader);  }
    mergedLeaders.put(mergedLeader.uid, mergedLeader);
  }
  private void absorbLeaders(HashMap<Integer, LeaderToken> leaders){
    for(LeaderToken leader: leaders.values())
      absorb(leader);
  }
  private void absorb(LeaderToken leader){
    LeaderToken newLeader = findNewLeader(leader);
    newLeader.component.addAll(leader.component);
  }
  private LeaderToken findNewLeader(LeaderToken leader){
    while(!mergedLeaders.containsKey(leader.wantsToMergeWith))
      leader.wantsToMergeWith = this.mergedLeaders.get(leader.wantsToMergeWith).wantsToMergeWith;
    return mergedLeaders.get(leader.wantsToMergeWith);
  }
}
