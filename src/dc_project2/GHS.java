package dc_project2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class GHS {
    // send CC to parent only once rcvd CC/Reject from all nbrs
    HashMap<Integer, Boolean> ccFromNbr = new HashMap<>();
    public boolean rcvdFromAllNbrs(){
      return !Arrays.asList(ccFromNbr.values()).contains(false);
    }
    
    MWOEMsg mwoeMsg;
    
    int leader;
    int parent = -1;  // parent of leader = -1 = synchronizer uid
    ArrayList<Integer> treeNbrs = new ArrayList<Integer>();
    
    Node node;
    
    public GHS(Node node)
    {
        leader = node.uid;
        this.node = node;
        resetCCfromNbrs();
        for (int uid: node.neighbors())
          node.sendTo(uid, new SearchMsg(leader, node.uid));
    }
    
    private void resetCCfromNbrs(){
      for (int uid: node.neighbors())
          this.ccFromNbr.put(uid, false);
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
      if(m.leader == leader)
        handleSearchMsg_SameLeader(m);
      else if(m.leader!=leader)
        node.sendTo(m.sender, new MWOEMsg(m.leader, leader, m.sender, node.uid, node.getWeight(m.sender), node.uid));
    }
    private void handleSearchMsg_SameLeader(SearchMsg m){
      if(m.sender == parent){
        resetCCfromNbrs();
        ccFromNbr.put(parent, true);
        for(int nbr: node.neighbors())
          node.sendTo(nbr, new SearchMsg(leader, node.uid));
      }
          else if(m.sender != parent)
            node.sendTo(m.sender, new RejectMsg(node.uid));
    }
    
    private void handleMWOEMsg(MWOEMsg m){
      ccFromNbr.put(m.sender, true);
      m.appendToPath(m.sender);
      if(mwoeMsg.compareTo(m)<0)  // update MWOE if new one greater
        mwoeMsg = m;
      
      node.sendTo(parent, mwoeMsg);
      //node.sendTo(mwoeMsg.sender, new MergeMsg());
    }
    
    private void handleRejectMsg(RejectMsg m){
      ccFromNbr.put(m.sender, true);
      if(!Arrays.asList(ccFromNbr.values()).contains(false))
        node.sendTo(parent, mwoeMsg);
    }
    
    private void handleNewLeaderMsg(NewLeaderMsg m){
      if(treeNbrs.contains(m.sender)){
        leader = m.newLeader;
        parent = m.sender;
        broadcast(m);
        resetCCfromNbrs();
      }
    }
    
    private void broadcast(Message msg){
      for(int nbr: node.neighbors())
        node.sendTo(nbr, msg);
    }
    
    public boolean isLeader(){
      return node.uid==leader;
    }
    
    private void terminate(){
      String mstNbrs = treeNbrs.stream().map(Object::toString).collect(Collectors.joining("-"+node.uid+", "));
      System.out.println(node.uid + " terminated;\tNeighbors in MST: " + mstNbrs);
    }
}
