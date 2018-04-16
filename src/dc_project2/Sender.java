package dc_project2;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.function.Predicate;



public class Sender{
  Message msg;
  int ownerUID, rcvrPort;
  String rcvrHostname;

  boolean successfullyConnected = false;
  public static Predicate<Sender> successfullyConnected(){ return sender->sender.successfullyConnected; }

  private boolean terminated = false;
  public static Predicate<Sender> terminated(){ return sender->sender.terminated; }
  public void terminate(){
    TestingMode.print(ownerUID + "'s sender to " + rcvrHostname + ":" + rcvrPort + " terminating");
    terminated=true;
  }
  
  Sender(String rcvrHostname, int rcvrPort, int senderUID){
    this.ownerUID = senderUID;  this.rcvrPort = rcvrPort; this.rcvrHostname = rcvrHostname;
    while(!successfullyConnected) try {
      Socket s = new Socket(rcvrHostname, rcvrPort);
      ObjectOutputStream outputStream = new ObjectOutputStream(s.getOutputStream());
      (new Thread() {
        @Override
        public void run() {
          while(!terminated){
            TestingMode.print(ownerUID + " not yet terminated");
            try{  if(msg != null) {
    if(msg.getClass() == NewLeaderAckMsg.class) TestingMode.print(ownerUID + " is sending NewLeaderAckMsg");
                    outputStream.writeObject(msg);
                    if(msg.getClass() == TerminateMsg.class)  terminate();
                  }
            }catch(IOException e) { e.printStackTrace();            }
            Wait.threeSeconds();
          }
        }
      }).start();
      successfullyConnected = true;
    } catch (UnknownHostException e){ e.printStackTrace();
    } catch (IOException e)         { e.printStackTrace();
    }
  }
  
  public void send(Message newMsg){
    if(newMsg.getClass() == NewLeaderMsg.class) TestingMode.print(ownerUID + " is sending NewLeaderMsg");
    newMsg.sender = ownerUID;
    msg = newMsg;
  }
  
}