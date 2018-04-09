package dc_project2;

// Broadcast
class SearchMsg extends Message{
    int leader;
    
    public SearchMsg(int level, int leader, int sender){
      super(level, sender);
      this.leader = leader;
    }
}