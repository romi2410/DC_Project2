package dc_project2;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.function.Predicate;



public class Sender{
  Message msg;
  int ownerUID;
  boolean successfullyConnected = false;
  public static Predicate<Sender> successfullyConnected(){ return sender->sender.successfullyConnected; }
  
  Sender(String rcvrHostname, int rcvrPort, int senderUID){
    this.ownerUID = senderUID;
    while(!successfullyConnected) try {
      Socket s = new Socket(rcvrHostname, rcvrPort);
      ObjectOutputStream outputStream = new ObjectOutputStream(s.getOutputStream());
      (new Thread() {
        @Override
        public void run() {
          while(true){
            try{  if(msg != null) { outputStream.writeObject(msg);  }
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
    newMsg.sender = ownerUID;
    msg = newMsg;
  }
}