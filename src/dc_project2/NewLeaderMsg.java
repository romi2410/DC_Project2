package dc_project2;
import java.util.HashSet;

// broadcast
class NewLeaderMsg extends Message{
  int level;
  int newLeader;
//  int oldLeader1, oldLeader2;
  int sender;
  HashSet<Integer> newNbrs;

  public NewLeaderMsg(int sender, int newLeader, HashSet<Integer> newNbrs){
    super(sender);
    this.newLeader = newLeader;
//    this.oldLeader1 = m.compLeader;
//    this.oldLeader2 = m.externalLeader;
    this.newNbrs = newNbrs;
  }
}