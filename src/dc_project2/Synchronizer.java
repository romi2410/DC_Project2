package dc_project2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;


public class Synchronizer extends Process{
  int level = 0;
  
  // use for synchronizing GHS start
  boolean serverUp = false;
  boolean sendersUp = false;
//  ServerThread server;
  ClientManager server;
  
  HashMap<Integer, LeaderToken> leaders  = new HashMap<Integer, LeaderToken>();
  HashMap<Integer, Sender> senders = new HashMap<Integer, Sender>();
  
  HashMap<Integer, HashSet<Integer>> newNbrs = new HashMap<Integer, HashSet<Integer>>();
  HashMap<Integer, Boolean> ackedNewLeader = new HashMap<Integer, Boolean>();
  
  public Synchronizer(HashMap<Integer, Node> nodes, String hostname, int port){
    uid = -1;
    TestingMode.print("new Synchronizer at thread " + Thread.currentThread().getName());
    for(int nodeUID: nodes.keySet())
      leaders.put(nodeUID, new LeaderToken(nodeUID));
    this.hostname = (TestingMode.isOn()) ? "localhost" : hostname;
    this.port = port;
//    server = new ServerThread(this, port);
//    server.start();
    server = new ClientManager(this);
    serverUp = true;
  }
  
  public void handleMsg(Message msg){
    if(msg.is(NewLeaderAckMsg.class))
      handleNewLeaderAckMsg((NewLeaderAckMsg) msg);
    else if(msg.is(MWOEMsg.class))
      handleMWOEMsg((MWOEMsg) msg);
  }
  public void handleNewLeaderAckMsg(NewLeaderAckMsg m){ ackedNewLeader.put(m.sender, true); }
  public synchronized void handleMWOEMsg(MWOEMsg m){
    Printer.print("Synchronizer rcvd " + m.toString());
    if(!leaders.keySet().contains(m.sender)) return;  // don't accept messages from non-mergedLeaders
    Printer.print("That msg is from a valid leader");
    LeaderToken leader = leaders.get(m.sender);
    leader.handleMWOEMsg(m);
    addNewNbrs(m.externalNode, m.leafnode);
    Printer.print(leaders.values().toString());
    if(BoolCollection.allTrue(leaders.values(), LeaderToken.rcvdMsg()))
      mergePhase(leaders);
  }
  private void mergePhase(HashMap<Integer, LeaderToken> leadersToMerge){
    leaders = new MergePhase(leadersToMerge).getLeaders();
    broadcastNewLeaders();
    level++;
    if(leaders.size()==1)
      terminate();
    else
      for(LeaderToken leader: leaders.values())
        sendNewSearchPhaseMsg(leader);
  }
  private void addNewNbrs(int nodeA, int nodeB){  addNewEdge(nodeA, nodeB); addNewEdge(nodeB, nodeA); }
  private void addNewEdge(int nodeFrom, int nodeTo){
    if(!newNbrs.containsKey(nodeFrom))
      newNbrs.put(nodeFrom, new HashSet<Integer>());
    newNbrs.get(nodeFrom).add(nodeTo);
  }
  
  private void broadcastNewLeaders(){
    for(LeaderToken leader: leaders.values())
      for(int node: leader.component)
        sendNewLeaderMsg(node, leader);
    ackedNewLeader.entrySet().forEach(e -> TestingMode.print(e.getKey() + " " + e.getValue()));
    Wait.untilAllTrue(ackedNewLeader);
    ackedNewLeader.entrySet().forEach(e -> TestingMode.print(e.getKey() + " " + e.getValue()));
    newNbrs.clear();
  }
  private void sendNewLeaderMsg(int node, LeaderToken leader){
    ackedNewLeader.put(node, false);
    HashSet<Integer> addedNbrs = newNbrs.getOrDefault(node, new HashSet<Integer>());
    NewLeaderMsg newLeaderMsg = new NewLeaderMsg(uid, leader.uid, addedNbrs);
    senders.get(node).loadNewMsg(newLeaderMsg);
  }
  private void sendNewSearchPhaseMsg(LeaderToken leader){
    leader.resetRcvdMsg();
    senders.get(leader.uid).loadNewMsg(new NewSearchPhaseMsg(-1));
  }
  
  private void terminate(){
    TestingMode.print("TERMINATING");
    TerminateMsg terminateMsg = new TerminateMsg(level, this.uid);
    for(Sender sender: senders.values())
      sender.loadNewMsg(terminateMsg);
    Wait.untilAllTrue(senders.values(), Sender.terminated());
    terminated = true;
  }
  
  public void connectToNodes(Set<Node> nodes){
    for(Node node: nodes)
      senders.put(node.uid, new Sender(node.server, uid));
    while(!BoolCollection.allTrue(senders.values(), Sender.successfullyConnected())){Wait.threeSeconds();}
    sendersUp = true;
  }
  
  //print level, and each node's component
  public String status(){
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
  private boolean rcvdMsg = false;
  public static Predicate<LeaderToken> rcvdMsg(){ return leader->leader.rcvdMsg; }
  
  public LeaderToken(int uid){  this.uid = uid;   component.add(uid); }
  public LeaderToken(LeaderToken leaderA, LeaderToken leaderB){
    this(MWOEMsg.newLeaderUID(leaderA.mwoe, leaderB.mwoe));
    absorb(leaderA); absorb(leaderB);
  }
  public void handleMWOEMsg(MWOEMsg m){
    wantsToMergeWith = m.externalLeader;
    mwoe = m;
    rcvdMsg = true;
  }
  public void    resetRcvdMsg()                      { rcvdMsg = false; Printer.print(uid + " had its rcvdMsg reset in Synchronizer");}
  public boolean wantsToMergeWith(LeaderToken leader){ return wantsToMergeWith==leader.uid; }
  public void    absorb(LeaderToken food)            { component.addAll(food.component);    }
  @Override
  public String toString(){
    StringJoiner sj = new StringJoiner(" ");
    sj.add(String.valueOf(uid)).add(" has component <");
    for (int node : component) 
      sj.add(String.valueOf(node));
    sj.add(">\t");
    
    if(rcvdMsg)
      sj.add(" and wants to merge with").add(String.valueOf(wantsToMergeWith));
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
    
    TestingMode.print("Synchronizer has produced the following leaders: ");
    for(LeaderToken leader: mergedLeaders.values())
      TestingMode.print(leader.uid+" ");
  }
  
  private void mergeLeaders(HashMap<Integer, LeaderToken> leadersToMerge){
    for(LeaderToken leaderA: leadersToMerge.values()){
      LeaderToken leaderB = leadersToMerge.get(leaderA.wantsToMergeWith);
      if(leaderB.wantsToMergeWith(leaderA))
        merge(leaderA, leaderB, leadersToMerge);
      else
        Printer.print(leaderA.uid + " wants to merge with " + leaderB.uid + ", but " + leaderB.uid + " wants to merge with " + leaderB.wantsToMergeWith);
    }
  }
  private void merge(LeaderToken leaderA, LeaderToken leaderB,
        HashMap<Integer, LeaderToken> leadersToMerge){
    LeaderToken mergedLeader = new LeaderToken(leaderA, leaderB);
    leaderA.wantsToMergeWith = mergedLeader.uid; mergedLeaders.put(leaderA.uid, leaderA);
    leaderB.wantsToMergeWith = mergedLeader.uid;   mergedLeaders.put(leaderB.uid, leaderB);
    for(LeaderToken leader: leadersToMerge.values())
      if(leader.wantsToMergeWith(leaderA) || leader.wantsToMergeWith(leaderB))          {
        leader.wantsToMergeWith = mergedLeader.uid; mergedLeaders.put(leader.uid, leader);  }
    mergedLeaders.put(mergedLeader.uid, mergedLeader);
    TestingMode.print(leaderA.uid + " and " + leaderB.uid + " merged into " + mergedLeader.uid);
  }
  private void absorbLeaders(HashMap<Integer, LeaderToken> leaders){
    for(LeaderToken leader: leaders.values())
      absorb(leader);
  }
  private void absorb(LeaderToken leader){
    LeaderToken newLeader = findNewLeader(leader);
    if(newLeader.uid != leader.uid){
      newLeader.absorb(leader);
      mergedLeaders.remove(leader.uid);
      TestingMode.print(leader.uid + " was absorbed into " + newLeader.uid);
      TestingMode.print(leader.toString());
      TestingMode.print(newLeader.toString());
    }
  }
  private LeaderToken findNewLeader(LeaderToken leader){
    while(!mergedLeaders.containsKey(leader.wantsToMergeWith))
      leader.wantsToMergeWith = this.mergedLeaders.get(leader.wantsToMergeWith).wantsToMergeWith;
    return mergedLeaders.get(leader.wantsToMergeWith);
  }
}
