package dc_project2;

import java.util.HashMap;
import java.util.Set;
import java.util.StringJoiner;

class Node extends Process{
  // node stuff
  GHS ghs;
  
  // neighbor stuff
  private HashMap<Integer, Double> weights  = new HashMap<>();
  private HashMap<Integer, Sender> senders = new HashMap<Integer, Sender>();
  public  Set<Integer>             neighbors()        { return senders.keySet();  }
  public  double                   getWeight(int nbr) { return weights.get(nbr);  }
  private Sender                   senderToSynchronizer;
  public ClientManager             server;

  // used by DC_Project2 for verification before initiating GHS
  boolean serverUp = false;
  public void haltUntilSendersUp(){
    while(!BoolCollection.allTrue(senders.values(), Sender.successfullyConnected()))
      { Wait.threeSeconds(); }
  }

  public Node(int u, String hn, int p) {
    uid = u;  port = p;
    hostname = (TestingMode.isOn()) ? "localhost" : hn;
    Node t = this;
    //(new ServerThread(t, port)).start();
    server = new ClientManager(this);
    serverUp = true;
    ghs = new GHS(this);
  }

  public void connectTo(Node nbr, double w){
    senders.put(nbr.uid, new Sender(nbr.server, uid));
    weights.put(nbr.uid, w);
  }
  public void connectToSynchronizer(Synchronizer sync){
    senderToSynchronizer = new Sender(sync.server, uid);
  }
  public void initGHS(){  ghs.newSearchPhase(); }

  public void sendTo(int rcvrUid, Message newMsg){
    newMsg.sender = uid;
    if(rcvrUid==-1) senderToSynchronizer.loadNewMsg(newMsg);
    else            senders.get(rcvrUid).loadNewMsg(newMsg);
  }

  public String toString(){
    return (new StringJoiner(" ")
            .add("Node ").add(String.valueOf(uid)).add(hostname).add(String.valueOf(port)))
            .toString();
  }
  
  public synchronized void handleMsg(Message msg){
    if(ghs!=null) ghs.handleMsg(msg);   //the only time ghs is null is at the start
  }
  
  public void terminate(){
    senders.values().forEach(sender -> sender.terminate());
    senderToSynchronizer.terminate();
    Wait.untilAllTrue(senders.values(), Sender.terminated());
    terminated = true;
  }
}