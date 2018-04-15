package dc_project2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class GHS {
  
  // rcvd msgs
  HashMap<Integer, Message> rcvdFromNbr = new HashMap<Integer, Message>();
  public boolean rcvdFromAllNbrs(){
    return !Arrays.asList(rcvdFromNbr.values()).contains(NullMsg.getInstance());
  }

  MWOEMsg mwoeMsg;

  int parent = -1;  // parent of leader = -1 = synchronizer uid
  int leader;
  public boolean isLeader(){  return node.uid==leader;  }
  HashSet<Integer> treeNbrs = new HashSet<Integer>();

  Node node;

  public GHS(Node node){
    leader = node.uid;
    this.node = node;
    newSearchPhase();
    if(TestingMode.isOn()) TestingMode.startPrintThread(this);
  }

  private void newSearchPhase(){
    TestingMode.print(String.valueOf(node.uid) + " is starting a new phase!");
    mwoeMsg = null;
    for(int uid: rcvdFromNbr.keySet())
      rcvdFromNbr.put(uid, NullMsg.getInstance());
    if(this.isLeader())
      broadcast(new SearchMsg(leader, node.uid));
  }
  private void broadcast(Message msg){
    node.neighbors().forEach(nbr -> node.sendTo(nbr, msg));
  }

  public void handleMsg(Object msg){
    Class msgType = msg.getClass();
    if(msgType == SearchMsg.class)
      handleSearchMsg((SearchMsg) msg);
    if(msgType == MWOEMsg.class)    
      handleMWOEMsg((MWOEMsg) msg);
    if(msgType == RejectMsg.class)    
      handleRejectMsg((RejectMsg) msg);
    if(msgType == NewLeaderMsg.class)
      handleNewLeaderMsg((NewLeaderMsg) msg);
    if(msgType == TerminateMsg.class)
      terminate();
  }

  private void handleSearchMsg(SearchMsg m){
    if(m.sender == parent){
      newSearchPhase();
      rcvdFromNbr.put(parent, m);
    }else if(m.leader == leader)
      node.sendTo(m.sender, new RejectMsg(node.uid));
    else
      node.sendTo(m.sender, new MWOEMsg(m.leader, leader, m.sender, node.uid, node.getWeight(m.sender), node.uid));
  }

  private void handleMWOEMsg(MWOEMsg newMwoeMsg){
    rcvdFromNbr.put(newMwoeMsg.sender, newMwoeMsg);
    //m.appendToPath(m.sender);
    mwoeMsg = (mwoeMsg != null) ? MWOEMsg.min(mwoeMsg, newMwoeMsg) : newMwoeMsg;
    if(rcvdFromAllNbrs())
      node.sendTo(parent, mwoeMsg);
  }

  private void handleRejectMsg(RejectMsg m){
    rcvdFromNbr.put(m.sender, m);
    if(rcvdFromAllNbrs())
      node.sendTo(parent, mwoeMsg);
  }

  private void handleNewLeaderMsg(NewLeaderMsg m){
    leader = m.newLeader;
    treeNbrs = intersection(node.neighbors(), m.component);
    if(this.isLeader())
      parent = -1;
    else
      parent = ;
    newSearchPhase();
  }
  private HashSet intersection(Set a, Set b){
    HashSet i = new HashSet(a);
    i.retainAll(b);
    return i;
  }

  private void terminate(){
    String mstNbrs = treeNbrs.stream().map(Object::toString).collect(Collectors.joining("-"+node.uid+", "));
    System.out.println(node.uid + " terminated;\tNeighbors in MST: " + mstNbrs);
  }

  public String toString(){
    StringJoiner sb = new StringJoiner(" ");
    sb.add("GHS Node").add(String.valueOf(node.uid));
    sb.add("Received following messages this level:");
    for(Entry<Integer, Message> rcvdMsg: rcvdFromNbr.entrySet())
      sb.add("\nReceived " + rcvdMsg.getValue().toString() + " from " + rcvdMsg.getKey());
    return sb.toString();
  }
}
