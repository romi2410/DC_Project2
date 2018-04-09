package dc_project2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.StringJoiner;

abstract class Message implements java.io.Serializable{
    int sender;
    
    public Message(int sender){
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
    
    public String serialize() throws IOException{
      ByteArrayOutputStream bo = new ByteArrayOutputStream();
      ObjectOutputStream so = new ObjectOutputStream(bo);
      so.writeObject(this);
      so.flush();
      return so.toString();
    }
}