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
    int level = 0;
    int parent;
    ArrayList<Integer> treeNbrs = new ArrayList<Integer>();
    
    Node node;
    
    public GHS(Node node)
    {
        leader = node.uid;
        this.node = node;
        resetCCfromNbrs();
        for (int uid: node.neighbors())
          node.sendTo(uid, new SearchMsg(level, leader, node.uid).toString());
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
    }
    
    private void handleSearchMsg(SearchMsg m){
      if(m.leader == leader)
        handleSearchMsg_SameLeader(m);
      else if(m.leader!=leader)
        node.sendTo(m.sender, new MWOEMsg(level, m.leader, leader, m.sender, node.uid, node.getWeight(m.sender), node.uid));
    }
    private void handleSearchMsg_SameLeader(SearchMsg m){
      if(m.sender == parent){
        level = m.level;
        resetCCfromNbrs();
        ccFromNbr.put(parent, true);
        for(int nbr: node.neighbors())
          node.sendTo(nbr, new SearchMsg(level, leader, node.uid));
      }
      else if(m.sender != parent)
        node.sendTo(m.sender, new RejectMsg(level, node.uid));
    }
    
    private void handleMWOEMsg(MWOEMsg m){
      ccFromNbr.put(m.sender, true);
      if(m.weight < mwoeMsg.weight){ // update min wgt outgoing edge, if lesser
        mwoeMsg = m;
        mwoeMsg.sender = node.uid;
      }
      
      if(!isLeader() && rcvdFromAllNbrs())
        node.sendTo(parent, mwoeMsg);
      else if(isLeader())
        node.sendToSynchronizer(m);
        //node.sendTo(mwoeMsg.sender, new MergeMsg());
    }
    private void merge(MWOEMsg mwoeMsg){
      int newLeader = Math.max(mwoeMsg.extnode, mwoeMsg.leafnode);
      NewLeaderMsg newLeaderMsg = new NewLeaderMsg(level+1, newLeader, mwoeMsg.leader1, mwoeMsg.leader2, node.uid);
      node.sendTo(mwoeMsg.sender, newLeaderMsg);
      level++;
      leader = newLeader;
      parent = mwoeMsg.sender;
    }
    
    private void handleMergeMsg(MergeMsg m){
      if(node.uid!=m.leafNode)
        node.sentTo(m.path.next);
      else if(node.uid==m.leafNode){
        requestAbsorb(m.externalNode); //will add node.uid to m.externalNode's tree neighbors
        boolean inBroadcast = checkStatus(m.externalNode);
        if(inBroadcast){        // Case1
          
        }
        else if(!inBroadcast){  // Case2
          if(mwoeMsg.level==level)          // MERGE
            merge(mwoeMsg);
          else if(level < mwoeMsg.level){   // ABSORB
            level = Math.max(level, mwoeMsg.level);
            // INCOMPLETE
          }
          else if(level > mwoeMsg.level)
            System.out.println("Unexpected case: level > mwoeMsg.level");
        }
      }
    }
    
    private void handleRejectMsg(RejectMsg m){
      ccFromNbr.put(m.sender, true);
      if(!Arrays.asList(ccFromNbr.values()).contains(false))
        node.sendTo(parent, mwoeMsg);
    }
    
    private void handleNewLeaderMsg(NewLeaderMsg m){
      if(leader==m.oldLeader1 || leader==m.oldLeader2){
        leader = m.newLeader;
        broadcast(m);
        resetCCfromNbrs();
      }
    }
    
    private void broadcast(Object msg){
      for(int nbr: node.neighbors())
        node.sendTo(nbr, msg);
    }
    
    // returns uid of smaller
    private int compare(double w_a, int u_a,
                        double w_b, int u_b){
      if(w_a<w_b)
        return u_a;
      else if(w_a>w_b)
        return u_b;
      else
        return Math.min(u_a, u_b);
    }
    
    public boolean isLeader(){
      return node.uid==leader;
    }
    
    private void terminate(){
      String mstNbrs = treeNbrs.stream().map(Object::toString).collect(Collectors.joining("-"+node.uid+", "));
      System.out.println(node.uid + " terminated;\tNeighbors in MST: " + mstNbrs);
    }
}
