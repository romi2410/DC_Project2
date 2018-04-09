package dc_project2;
import java.util.StringJoiner;
import java.lang.reflect.Field;

// Convergecast
public class MWOEMsg extends Message implements Comparable<MWOEMsg>{
    int leader1;
    int leader2;
    int leafnode;
    int externalNode;
    double weight;
    
    public MWOEMsg(int level, int leader1, int leader2, int leafnode, int extnode, double weight, int sender){
      super(level, sender);
      this.leader1 = leader1;
      this.leader2 = leader2;
      this.leafnode = leafnode;
      this.externalNode = extnode;
      this.weight = weight;
    }
    
    public int compareTo(MWOEMsg m){
      int weightComparison = Double.compare(this.weight, m.weight);
      if(weightComparison != 0)
        return weightComparison;
      else{
        int bigLeaderComparison = Integer.compare(
                Math.max(this.leader1, this.leader2), 
                Math.max(m.leader1, m.leader2)
        );
        if(bigLeaderComparison != 0)
          return bigLeaderComparison;
        else{
          int smallLeaderComparison = Integer.compare(
                  Math.min(this.leader1, this.leader2), 
                  Math.min(m.leader1, m.leader2)
          );
          return smallLeaderComparison;
        }
      }
    }
}