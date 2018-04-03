package dc_project2;
import java.util.StringJoiner;
import java.lang.reflect.Field;

class SearchMsg implements java.io.Serializable {
    int level;
    int leader;
    int sender;
    
    public SearchMsg(int level, int leader, int sender){
        this.level = level;
        this.leader = leader;
        this.sender = sender;
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