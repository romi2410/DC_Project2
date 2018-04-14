package dc_project2;

import java.util.HashMap;
import java.util.Set;

class Node{
  // node stuff
  int uid;
  int port;
  String hostname;
  GHS ghs;

  // neighbor stuff
  private HashMap<Integer, Double> neighbors2weights  = new HashMap<>();
  private HashMap<Integer, Sender> senders = new HashMap<Integer, Sender>();
  public  Set<Integer>             neighbors()        { return neighbors2weights.keySet(); }
  public  double                   getWeight(int nbr) { return neighbors2weights.get(nbr); }
  private Sender                   senderToSynchronizer;

  // used by DC_Project2 for verification before initiating GHS
  boolean serverUp = false;
  public void haltUntilSendersUp(){
    while(!BooleanCollection.allTrue(senders.values(), Sender.successfullyConnected()))
      { Wait.aSec(); }
  }

  public Node(int u, String hn, int p) {
    uid = u;  port = p;
    hostname = (TestingMode.isOn()) ? "localhost" : hn;
    serverUp = true;        
    (new ServerThread(this, port)).start();
    if(TestingMode.isOn()) startPrintThread();
  }

  public void connectTo(String nbrhostname, int nbrport, int nbrUID, double w){
    senders.put(nbrUID, new Sender(nbrhostname, nbrport, nbrUID));
    neighbors2weights.put(nbrUID, w);
  }
  public void connectToSynchronizer(String syncHostname, int syncPort){
    senderToSynchronizer = new Sender(syncHostname, syncPort, -1);
  }
  public void initGHS(){
    ghs = new GHS(this);
  }

  public void sendTo(int rcvrUid, Message newMsg){
    newMsg.sender = uid;
    if(rcvrUid==-1) senderToSynchronizer.send(newMsg);
    else            senders.get(rcvrUid).send(newMsg);
  }

  public String toString(){
      StringBuilder sb = new StringBuilder();

      sb.append(uid).append(" ");
      sb.append(hostname).append(" ");
      sb.append(port).append(" ");

      for(int neighbor: neighbors2weights.keySet())
          sb.append(neighbor).append("    ");

      return sb.toString();
  }
  private void startPrintThread(){
    Node t = this;
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