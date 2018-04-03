package dc_project2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;

public class GHS {
    int roundNo=0;
    HashMap<Integer, Double> neighbors2weights;
    HashMap<Integer, Boolean> rcvdFromNbr = new HashMap<>();
    HashMap<Integer, Boolean> inTree = new HashMap<>();
    
    int leader;
    
    // reset every round
    int parent;
    boolean rcvdBC;
    
    ArrayList<String> buffer;
    Node owner;
    
    public GHS(HashMap<Integer, Double> n2w, Node owner)
    {
        int leader = owner.uid;
        buffer  = new ArrayList<String>();
        neighbors2weights = n2w;
        for (int uid: n2w.keySet()){
            this.rcvdFromNbr.put(uid, false);
            this.inTree.put(uid, false);
        }
        this.owner = owner;
    }
    
    public int handleMsg(String m){
      String msgType=m.split(" ")[0];
      if(msgType.equals("broadcast")){
        BroadcastMsg bc_msg = BroadcastMsg.toBroadcastMsg(m);
        if(bc_msg.leader==leader){
          if(!rcvdBC){
            parent = bc_msg.sender;
            rcvdBC = true;
          }
          else{
            rejecting.put(bc_msg.sender, true);
          }
        }
        else{
          sendConvergeTo.put(bc_msg.sender, true);
        }
      }
      
      GHSMessage ghsmsg = GHSMessage.toGHSMsg(m);
        
      // message handling logic goes here
      synchronized(this){
        if ((roundNo <= ghsmsg.round) && (!rcvdFromNbr.get(ghsmsg.senderUID))){ 
        }
        else if (roundNo < ghsmsg.round)
            buffer.add(m);
      }
      return -1;
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
