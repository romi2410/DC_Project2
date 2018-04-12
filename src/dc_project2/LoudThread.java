package dc_project2;

public class LoudThread extends Thread {

    public void runLoudly() {
      System.out.println("Starting thread");
      run();
    }
}