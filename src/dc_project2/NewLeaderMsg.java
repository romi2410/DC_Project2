package dc_project2;
import java.util.StringJoiner;
import java.lang.reflect.Field;

// broadcast
class NewLeaderMsg extends Message{
    int level;
    int newLeader;
    int oldLeader1, oldLeader2;
    int sender;
    
    public NewLeaderMsg(int level, int newLeader, int oldLeader1, int oldLeader2, int sender){
        super(level, sender);
        this.newLeader = newLeader;
        this.oldLeader1 = oldLeader1;
        this.oldLeader2 = oldLeader2;
    }
}