package dc_project2;

abstract class Process{
  int uid;
  String hostname;
  int port;
  boolean terminated = false;
  abstract public void handleMsg(Message m);
}