package dc_project2;
import java.util.HashSet;

// broadcast
class NewLeaderMsg extends Message{
  int level;
  int newLeader;
  HashSet<Integer> newNbrs;

  public NewLeaderMsg(int sender, int newLeader, HashSet<Integer> newNbrs){
    super(sender);
    this.newLeader = newLeader;
    this.newNbrs = newNbrs;
  }
}