package dc_project2;
import java.lang.reflect.Field;
import java.util.StringJoiner;
import java.util.function.Predicate;

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
    try{
      for(Field field: o.getClass().getSuperclass().getDeclaredFields()) 
        sj.add(field.getName() + ":" + String.valueOf(field.get(o)));
      for(Field field: o.getClass().getDeclaredFields())
        sj.add(field.getName() + ":" + String.valueOf(field.get(o)));
    }catch(IllegalAccessException e){System.out.println(e);}
    return sj.toString();
  }
  //public static Predicate<Message> isConvergeCast(){ return m -> m.is(MWOEMsg.class) || m.is(RejectMsg.class);  }
  public boolean is(Class c){ return this.getClass() == c;  }
}