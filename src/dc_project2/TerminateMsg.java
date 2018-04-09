package dc_project2;
public class TerminateMsg extends Message{
  int level;
  public TerminateMsg(int level, int sender){
    super(sender);
    this.level = level;
  }
}