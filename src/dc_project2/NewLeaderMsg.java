package dc_project2;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;

// broadcast
class NewLeaderMsg extends Message{
  int level;
  int newLeader;
  int oldLeader1, oldLeader2;
  int sender;
  HashSet<Integer> component;
  //Deque<Integer> path = new ArrayDeque<Integer>();

  public NewLeaderMsg(int sender, int newLeader, MWOEMsg m, HashSet<Integer> component){
    super(sender);
    this.newLeader = newLeader;
    this.oldLeader1 = m.compLeader;
    this.oldLeader2 = m.externalLeader;
    this.component = component;
    //this.path = m.path;
  }
}