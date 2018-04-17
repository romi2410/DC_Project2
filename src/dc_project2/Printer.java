package dc_project2;

// synchronous printing + prevent duplicate print statements for a uid

import java.util.HashMap;

public class Printer {
  private static HashMap<Integer,String> lastPrinted = new HashMap<Integer,String>();

  private Printer(){}
  public static void print(String s) {  print(s, -2); } // -2 is like a dummy catch-all uid
  public static synchronized void print(String s, int uid) {
    if(!s.equals(lastPrinted.get(uid))){
      System.out.println(s);
      lastPrinted.put(uid, s);  lastPrinted.put(-2, s);
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
