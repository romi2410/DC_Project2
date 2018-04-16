package dc_project2;

import java.util.Collection;
import java.util.HashMap;

public final class Wait{
  private Wait(){}
  public  static void aSec()            { wait(1);  }
  public  static void threeSeconds()    { wait(3);  }
  public  static void tenSeconds()      { wait(10); }
  public  static void thirtySeconds()   { wait(30); }
  private static void wait(int seconds) {
    try                           { Thread.sleep(seconds*1000); } 
    catch(InterruptedException e) { System.out.println(e);      }
  }
  public  static void untilAllTrue(Collection<Boolean> c) { while(!BoolCollection.allTrue(c)) { Wait.aSec(); }}
  public  static void untilAllTrue(HashMap hm)            { while(!BoolCollection.allTrue(hm)){ Wait.aSec(); }}

}