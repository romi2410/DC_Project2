package dc_project2;
import java.util.StringJoiner;
import java.lang.reflect.Field;

// Broadcast
class SearchMsg extends Message implements java.io.Serializable {
    int leader;
    
    public SearchMsg(int level, int leader, int sender){
      super(level, sender);
      this.leader = leader;
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