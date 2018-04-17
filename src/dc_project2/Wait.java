package dc_project2;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.Random;

public final class Wait{
  private Wait(){}
  public  static void aSec()            { wait(1);  }
  public  static void threeSeconds()    { wait(3);  }
  public  static void tenSeconds()      { wait(10); }
  public  static void thirtySeconds()   { wait(30); }
  public  static void random()          { wait((new Random()).nextInt(2)); }
  private static void wait(int seconds) {
    try                           { Thread.sleep(seconds*1000); } 
    catch(InterruptedException e) { System.out.println(e);      }
  }
  public  static void untilAllTrue(Collection<Boolean> c)     { while(!BoolCollection.allTrue(c)) { Wait.aSec(); }}
  public  static void untilAllTrue(HashMap hm)                { while(!BoolCollection.allTrue(hm.values())){ Wait.aSec(); }}
  public  static void untilAllTrue(Collection c, Predicate p) { while(!BoolCollection.allTrue(c, p)){ Wait.aSec(); }}

}