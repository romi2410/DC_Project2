package dc_project2;

public final class Wait{
  private Wait(){}
  public  static void aSec()            { wait(1);  }
  public  static void tenSeconds()      { wait(10); }
  private static void wait(int seconds) {
    try                           { Thread.sleep(seconds*1000); } 
    catch(InterruptedException e) { System.out.println(e);      }
  }
}