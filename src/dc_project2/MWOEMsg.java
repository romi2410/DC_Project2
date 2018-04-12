package dc_project2;
import java.util.Deque;
import java.util.ArrayDeque;

// Convergecast
public class MWOEMsg extends Message implements Comparable<MWOEMsg>{
    int compLeader;
    int externalLeader;
    int leafnode;
    int externalNode;
    double weight;
    Deque<Integer> path = new ArrayDeque<Integer>();
    
    public MWOEMsg(int compLeader, int externalLeader, int leafnode, int extnode, double weight, int sender){
      super(sender);
      this.compLeader = compLeader;
      this.externalLeader = externalLeader;
      this.leafnode = leafnode;
      this.externalNode = extnode;
      this.weight = weight;
      path.add(externalNode);
    }
    
    public void appendToPath(int sender){
      path.add(sender);
    }
    
    public int compareTo(MWOEMsg m){
      int weightComparison = Double.compare(this.weight, m.weight);
      if(weightComparison != 0)
        return weightComparison;
      else{
        int bigLeaderComparison = Integer.compare(
                Math.max(this.compLeader, this.externalLeader), 
                Math.max(m.compLeader, m.externalLeader)
        );
        if(bigLeaderComparison != 0)
          return bigLeaderComparison;
        else{
          int smallLeaderComparison = Integer.compare(
                  Math.min(this.compLeader, this.externalLeader), 
                  Math.min(m.compLeader, m.externalLeader)
          );
          return smallLeaderComparison;
        }
      }
    }
}