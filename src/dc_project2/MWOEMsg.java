package dc_project2;
import java.util.StringJoiner;
import java.lang.reflect.Field;

// Convergecast
public class MWOEMsg extends Message implements java.io.Serializable{
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

    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(" ");
        sj.add("MWOEMsg");
        for(Field field: this.getClass().getDeclaredFields())
          sj.add(field.toString());
        return sj.toString();
    }
}