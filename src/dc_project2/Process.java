package dc_project2;

abstract class Process{
  int uid;
  abstract public void handleMsg(Message m);
}