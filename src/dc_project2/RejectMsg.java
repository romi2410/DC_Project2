package dc_project2;

class RejectMsg implements java.io.Serializable {
    int level, sender;
    
    public RejectMsg(int level, int sender){
        this.level = level;
        this.sender = sender;
    }
}