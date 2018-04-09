package dc_project2;

import java.lang.reflect.Field;
import java.util.StringJoiner;

abstract class Message implements java.io.Serializable{
    int level, sender;
    
    public Message(int level, int sender){
        this.level = level;
        this.sender = sender;
    }

    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(" ");
        sj.add(this.getClass().toString());
        for(Field field: this.getClass().getDeclaredFields())
          sj.add(field.toString());
        return sj.toString();
    }
}