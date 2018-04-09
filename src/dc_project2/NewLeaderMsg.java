package dc_project2;
import java.util.StringJoiner;
import java.lang.reflect.Field;

// broadcast
class NewLeaderMsg extends Message implements java.io.Serializable {
    int level;
    int newLeader;
    int oldLeader1, oldLeader2;
    int sender;
    
    public NewLeaderMsg(int level, int newLeader, int oldLeader1, int oldLeader2, int sender){
        super(level, sender);
        this.newLeader = newLeader;
        this.oldLeader1 = oldLeader1;
        this.oldLeader2 = oldLeader2;
    }

    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SearchMsg");
        for(Field field: this.getClass().getDeclaredFields())
          sj.add(field.toString());
        return sj.toString();
    }
}