package dc_project2;

public class NullMsg extends Message {
  private NullMsg(int sender)         { super(sender);                                            }
  public static NullMsg getInstance() { return NullMsgHolder.INSTANCE;                            }
  private static class NullMsgHolder  { private static final NullMsg INSTANCE = new NullMsg(-2);  }
}
