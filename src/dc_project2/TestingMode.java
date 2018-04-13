package dc_project2;

/*  When TestingMode.isOn(),
      set host of all nodes to localhost
      and print additional logging messages
*/
public class TestingMode {
  static boolean        test = false;
  
  public static void    turnOn(){ test = true;  
                                  System.out.println("Running in test (host will be localhost)"); }
  public static boolean isOn()  { return test;  }
}
