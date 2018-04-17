package dc_project2;

// stores the last output printed, so that duplicate outputs are not printed

import java.util.HashMap;

public class Printer {
  private static String lastPrinted = "";
  private static HashMap<Integer,String> lastPrintedFrom = new HashMap<Integer,String>();

  private Printer(){}
  public static void print(String s) {
    if(!s.equals(lastPrinted)){
      System.out.println(s);
      lastPrinted = s;
    }
  }
  public static void print(String s, int uid) {
    if(!s.equals(lastPrinted) && !s.equals(lastPrintedFrom.get(uid))){
      System.out.println(s);
      lastPrinted = s;
      lastPrintedFrom.put(uid, s);
    }
  }
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
