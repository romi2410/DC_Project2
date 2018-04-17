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
  ObjectOutputStream outputStream;

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
      outputStream = new ObjectOutputStream(s.getOutputStream());
      (new Thread() {
        @Override
        public void run() {
          while(!terminated){
            send();
            Wait.threeSeconds();
          }
        }
      }).start();
      successfullyConnected = true;
    } catch (UnknownHostException e){ e.printStackTrace();
    } catch (IOException e)         { e.printStackTrace();
    }
  }
  
  public void loadNewMsg(Message newMsg){
    newMsg.sender = ownerUID;
    msg = newMsg;
    send();
    TestingMode.print(ownerUID + " sent " + newMsg.toString() + " to " + rcvrPort, ownerUID);
  }
  private void send(){
    if(msg != null) 
      try{  outputStream.writeObject(msg);
            if(msg.is(TerminateMsg.class))  terminate();
      }catch(IOException e) { e.printStackTrace(); }
  }
  
}