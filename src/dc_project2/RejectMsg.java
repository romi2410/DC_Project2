package dc_project2;

class RejectMsg extends Message implements java.io.Serializable {
    int level, sender;
    
    public RejectMsg(int level, int sender){
      super(level, sender);
    }
}