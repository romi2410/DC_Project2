package dc_project2;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.function.Predicate;



public class Sender{
  Message msg;
  int ownerUID;
  ClientManager rcvr;
  ObjectOutputStream outputStream;

  boolean successfullyConnected = false;
  public static Predicate<Sender> successfullyConnected(){ return sender->sender.successfullyConnected; }

  private boolean terminated = false;
  public static Predicate<Sender> terminated(){ return sender->sender.terminated; }
  public void terminate(){
    TestingMode.print(ownerUID + "'s sender to " + rcvr.owner.uid + " terminating");
    terminated=true;
  }
  
  Sender(ClientManager rcvr, int senderUID){
    this.ownerUID = senderUID;  this.rcvr = rcvr;
//    while(!successfullyConnected)
//      (new Thread() {
//        @Override
//        public void run() {
//          while(!terminated){
//            send();
//            Wait.threeSeconds();
//          }
//        }
//      }).start();
      successfullyConnected = true;
  }
  
  public void loadNewMsg(Message newMsg){
    newMsg.sender = ownerUID;
    msg = newMsg;
    send();
  }
  private void send(){
    if(msg != null){
      TestingMode.print(ownerUID + " sent " + msg.toString() + " to " + rcvr.owner.uid, ownerUID);
      rcvr.handleMsg(msg);
      if(msg.is(TerminateMsg.class))  terminate();
    }
  }
  
}