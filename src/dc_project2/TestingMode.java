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
  public static int       threadCount()   { return      Thread.activeCount();                   }
  public static void      print(String s)           { Printer.print(s);                         }
  public static void      print(String s, int uid)  { Printer.print(s, uid);                    }
}
