package dc_project2;

// Convergecast
public class MWOEMsg extends Message{
  int compLeader;
  int externalLeader;
  int leafnode;
  int externalNode;
  double weight;

  public MWOEMsg(int compLeader, int externalLeader, int leafnode, int extnode, double weight, int sender){
    super(sender);
    this.compLeader = compLeader;
    this.externalLeader = externalLeader;
    this.leafnode = leafnode;
    this.externalNode = extnode;
    this.weight = weight;
  }

  public static int newLeaderUID(MWOEMsg m1, MWOEMsg m2){
    assert Math.max(m1.externalNode, m1.leafnode) == Math.max(m2.externalNode, m2.leafnode);
    return Math.max(m1.externalNode, m1.leafnode);
  }
          
  public static MWOEMsg min(MWOEMsg a, MWOEMsg b){
    if(a.weight > b.weight)
      return b;
    else if(a.weight < b.weight)
      return a;
    else{
      int a_BigLeader = Math.max(a.compLeader, a.externalLeader);
      int b_BigLeader = Math.max(b.compLeader, b.externalLeader);
      if(a_BigLeader > b_BigLeader)
        return a;
      if(a_BigLeader < b_BigLeader)
        return b;
      else{
        int a_SmallLeader = Math.min(a.compLeader, a.externalLeader);
        int b_SmallLeader = Math.min(b.compLeader, b.externalLeader);
        if(a_SmallLeader > b_SmallLeader)
          return a;
        else if(a_SmallLeader < b_SmallLeader)
          return b;
        else{
//          TestingMode.print("Unexpected Scenario: comparing two equal MWOEMsgs," 
//                  + a.toString() + " and " + b.toString());
          return a;
        }
      }
    }
  }
}