package dc_project2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class GHS {
  
  // rcvd msgs
  private HashMap<Integer, Message> rcvdFromNbr = new HashMap<Integer, Message>();
  public boolean rcvdCCFromAllNbrs(){
    Printer.print(node.uid + " has rcvd this rnd: " + rcvdFromNbrs());
    //rcvdFromNbr.entrySet().forEach(e -> System.out.print(e.getKey()+":"+e.getValue()));
    //return !Arrays.asList(rcvdFromNbr.values()).contains(NullMsg.getInstance());
    return BoolCollection.allTrue(rcvdFromNbr.values(), Message.isConvergeCast());
  }
  public String rcvdFromNbrs(){
    try{
      StringJoiner sj = new StringJoiner("\n\t");
      rcvdFromNbr.entrySet().forEach(e -> sj.add(e.getKey()+":"+e.getValue()));
      return (sj.toString().trim().length()>0) ? sj.toString() : "no messages";
    }catch(NullPointerException e){ return "no messages"; }
  }

  MWOEMsg mwoeMsg;

  int parent = -1;  // parent of leader = -1
  int leader;
  public boolean isLeader(){  return node.uid==leader;  }
  HashSet<Integer> treeNbrs = new HashSet<Integer>();

  Node node;

  public GHS(Node node){
    leader = node.uid;
    this.node = node;
  }

  public void newSearchPhase(){
    mwoeMsg = null;
    for(int uid: node.neighbors())
      rcvdFromNbr.put(uid, NullMsg.getInstance());
    broadcast(new SearchMsg(leader, node.uid));
  }
  private void broadcast(Message msg){
    node.neighbors().forEach(nbr -> node.sendTo(nbr, msg));
  }

  public synchronized void handleMsg(Message msg){
    if(msg.is(SearchMsg.class))
      handleSearchMsg((SearchMsg) msg);
    else if(msg.is(MWOEMsg.class))
      handleMWOEMsg((MWOEMsg) msg);
    else if(msg.is(RejectMsg.class))
      handleRejectMsg((RejectMsg) msg);
    else if(msg.is(NewLeaderMsg.class))
      handleNewLeaderMsg((NewLeaderMsg) msg);
    else if(msg.is(NewSearchPhaseMsg.class))
      handleNewSearchPhaseMsg((NewSearchPhaseMsg) msg);
    else if(msg.is(TerminateMsg.class))
      terminate();
  }

  private void handleSearchMsg(SearchMsg m){
    TestingMode.print(node.uid + "'s leader is " + leader);
    if(treeNbrs.contains(m.sender)){
      parent = m.sender;
      newSearchPhase();
      rcvdFromNbr.put(parent, m);
    }else if(m.leader == leader)
      node.sendTo(m.sender, new RejectMsg(node.uid));
    else
      node.sendTo(m.sender, new MWOEMsg(m.leader, leader, m.sender, node.uid, node.getWeight(m.sender), node.uid));
  }

  private void handleMWOEMsg(MWOEMsg newMwoeMsg){
    rcvdFromNbr.put(newMwoeMsg.sender, newMwoeMsg);
    mwoeMsg = (mwoeMsg != null) ? MWOEMsg.min(mwoeMsg, newMwoeMsg) : newMwoeMsg;
    if(rcvdCCFromAllNbrs()){
      TestingMode.print(node.uid + " wants to send MWOEMsg " + mwoeMsg + " to its parent " + parent, node.uid);
      node.sendTo(parent, mwoeMsg);
    }
  }

  private void handleRejectMsg(RejectMsg m){
    rcvdFromNbr.put(m.sender, m);
    if(rcvdCCFromAllNbrs()){
      TestingMode.print(node.uid + " wants to send MWOEMsg " + mwoeMsg + " to its parent " + parent, node.uid);
      node.sendTo(parent, mwoeMsg);
    }
  }

  private void handleNewLeaderMsg(NewLeaderMsg m){
    leader = m.newLeader;
    treeNbrs.addAll(m.newNbrs);
    node.sendTo(-1, new NewLeaderAckMsg(node.uid));
  }
  private void handleNewSearchPhaseMsg(NewSearchPhaseMsg m){
    assert(this.isLeader());
    newSearchPhase();
  }
  
  private void terminate(){
    Printer.print(node.uid + " begin termination");
    String mstNbrs = treeNbrs.stream().map(Object::toString).collect(Collectors.joining("-"+node.uid+", "));
    node.terminate();
    Printer.print(node.uid + " terminated;\tNeighbors in MST: " + mstNbrs + " (size is " + treeNbrs.size());
  }

  public String toString(){
    StringJoiner sb = new StringJoiner(" ");
    sb.add("GHS Node").add(String.valueOf(node.uid));
//    sb.add("Received following messages this level:");
//    for(Entry<Integer, Message> rcvdMsg: rcvdFromNbr.entrySet())
//      sb.add("\nReceived " + rcvdMsg.getValue().toString() + " from " + rcvdMsg.getKey());
    return sb.toString();
  }
}
