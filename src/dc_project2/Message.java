package dc_project2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.StringJoiner;

abstract class Message implements java.io.Serializable{
  int sender;
  public Message(int sender){ this.sender = sender; }

  @Override
  public String toString(){
    StringJoiner sj = new StringJoiner("_");
    sj.add("[");
    sj.add(this.getClass().getName());
    sj.add(Message.fieldValues(this));
    sj.add("]");
    return sj.toString();
  }
  public static String fieldValues(Object o){
    StringJoiner sj = new StringJoiner("_");
    for(Field field: o.getClass().getDeclaredFields())
      try{
        sj.add(field.getName() + ":" + String.valueOf(field.get(o)));
      }catch(IllegalAccessException e){System.out.println(e);}
    return sj.toString();
  }
}