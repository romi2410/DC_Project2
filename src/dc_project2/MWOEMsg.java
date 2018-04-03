package dc_project2;
import java.util.StringJoiner;
import java.lang.reflect.Field;

class MWOEMsg implements java.io.Serializable {
    int level;
    int leader1;
    int leader2;
    int leafnode;
    int extnode;
    double weight;
    int sender;
    
    public MWOEMsg(int level, int leader1, int leader2, int leafnode, int extnode, double weight, int sender){
        this.level = level;
        this.leader1 = leader1;
        this.leader2 = leader2;
        this.leafnode = leafnode;
        this.extnode = extnode;
        this.weight = weight;
        this.sender = sender;
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