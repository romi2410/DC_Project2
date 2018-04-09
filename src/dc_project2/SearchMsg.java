package dc_project2;

// Broadcast
class SearchMsg extends Message{
    int leader;
    
    public SearchMsg(int leader, int sender){
      super(sender);
      this.leader = leader;
    }
}