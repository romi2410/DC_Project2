package dc_project2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.function.Predicate;



public class Sender{
  String serializedMsg;
  int ownerUID;
  boolean successfullyConnected = false;
  public static Predicate<Sender> successfullyConnected(){ return sender->sender.successfullyConnected; }
  
  Sender(String rcvrHostname, int rcvrPort, int senderUID){
    this.ownerUID = senderUID;
    while(!successfullyConnected) try {
      Socket s = new Socket(rcvrHostname, rcvrPort);
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
      (new Thread() {
          @Override
          public void run() {
            while(true){
              try{
                if(serializedMsg != null && serializedMsg.trim().length()>0){
                  out.write(serializedMsg);
                  TestingMode.print(ownerUID + " sent " + serializedMsg + " to " + rcvrPort);
                }
                out.newLine();
                out.flush();
              }catch(IOException e){ e.printStackTrace(); }
              Wait.aSec();
            }
          }
      }).start();
      successfullyConnected = true;
    } catch (UnknownHostException e){ e.printStackTrace();
    } catch (IOException e)         { e.printStackTrace();
    }
  }
  
  public void send(Message msg){
    msg.sender = ownerUID;
    try{
      serializedMsg = msg.serialize();
      TestingMode.print(ownerUID + " is loaded to send " + msg.toString());
    } catch (IOException e) {
      System.out.println(e);
      System.out.println("Attempt to serialize " + msg.toString() + " failed");
    }
  }
}