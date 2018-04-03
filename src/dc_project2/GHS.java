package dc_project2;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringJoiner;

public class GHS {
    int roundNo=0;
    HashMap<Integer, Double> neighbors2weights;
    HashMap<Integer, Boolean> ccFromNbr = new HashMap<>();
    MWOEMsg mwoeMsg;
    
    int leader;
    int level = 0;
    int parent;
    
    Node owner;
    
    public GHS(HashMap<Integer, Double> n2w, Node owner)
    {
        leader = owner.uid;
        neighbors2weights = n2w;
        for (int uid: n2w.keySet())
            this.ccFromNbr.put(uid, false);
        this.owner = owner;
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
      if(m.leader==leader){
        if(m.sender==parent){
          level = m.level;
          for(int nbr: ccFromNbr.keySet())
            if(m.sender == nbr)
              ccFromNbr.put(nbr, true);
            else
              ccFromNbr.put(nbr, false);
          for(int nbr: owner.neighbors2socket.keySet())
            owner.sendTo(nbr, SearchMsg);
        }
        else{ //reject broadcast msgs from same leader but not from parent
          owner.sendTo(nbr, RejectMsg);
        }
      }
      else{
        owner.sendTo(m.sender, MWOEMsg);
      }
    }
    
    private void handleMWOEMsg(MWOEMsg m){
      ccFromNbr.put(m.sender, true);
      if(m.weight < mwoeMsg.weight)
        mwoeMsg = m;
      
      if(!isLeader()){
        if(!Arrays.asList(ccFromNbr.values()).contains(false))
          owner.sendTo(parent, mwoeMsg);
      }else{
        if(mwoeMsg.level==level){ // MERGE
          int newLeader = Math.max(mwoeMsg.extnode, mwoeMsg.leafnode);
          owner.sendTo(mwoeMsg.sender, new NewLeaderMsg(level+1, newLeader, mwoeMsg.leader1, mwoeMsg.leader2, owner.uid));
        }
        else{ // ABSORB
          if(mwoeMsg.level > level)
            
        }
      }
    }
    private void handleRejectMsg(Object m){
      ccFromNbr.put(m.sender, true);
      if(!Arrays.asList(ccFromNbr.values()).contains(false))
        owner.sendTo(parent, mwoeMsg);
    }
    
    private void handleNewLeaderMsg(NewLeaderMsg m){
      if(leader==m.oldLeader1 || leader==m.oldLeader2){
        leader = m.newLeader;
        broadcast(m);
        for (int uid: ccFromNbr.keySet())
            this.ccFromNbr.put(uid, false);
      }
    }
    
    private void broadcast(Object msg){
        for(int nbr: owner.neighbors2socket.keySet())
          owner.sendTo(nbr, msg);
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
      return owner.uid==leader;
    }
}
