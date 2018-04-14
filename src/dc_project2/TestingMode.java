package dc_project2;

/*  When TestingMode.isOn(),
      set host of all nodes to localhost
      and print additional logging messages
*/
public class TestingMode {
  
  private static boolean  test = false;
  
  public static void      turnOn()        { test = true;
                                            print("Running in test (host will be localhost)");  }
  public static boolean   isOn()          { return      test;                                   }
  
  public static void      print(String s) { if(isOn())  System.out.println(s);                  }
  public static int       threadCount()   { return      Thread.activeCount();                   }
  public static void      startPrintThread(Object t){
    (new Thread() {
      @Override
      public void run() {
        while(true){
          Wait.thirtySeconds();
          TestingMode.print(t.toString());
        }
      }
    }).start();
  }
}
