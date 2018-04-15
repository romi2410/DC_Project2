package dc_project2;
import java.util.HashSet;

// broadcast
class NewLeaderMsg extends Message{
  int level;
  int newLeader;
  int oldLeader1, oldLeader2;
  int sender;
  //HashSet<Integer> component;
  //Path path;
  HashSet<Integer> newNbrs;

  public NewLeaderMsg(int sender, int newLeader, MWOEMsg m, HashSet<Integer> newNbrs){
    super(sender);
    this.newLeader = newLeader;
    this.oldLeader1 = m.compLeader;
    this.oldLeader2 = m.externalLeader;
//    this.component = component;
//    this.path = m.path;
    this.newNbrs = newNbrs;
  }
}