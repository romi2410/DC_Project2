package dc_project2;

abstract class Process{
  int uid;
  String hostname;
  int port;
  abstract public void handleMsg(Message m);
}