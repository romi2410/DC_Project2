package dc_project2;
import java.util.StringJoiner;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;

// broadcast
class NewLeaderMsg extends Message{
    int level;
    int newLeader;
    int oldLeader1, oldLeader2;
    int sender;
    Deque<Integer> path = new ArrayDeque<Integer>();
    
    public NewLeaderMsg(int newLeader, int oldLeader1, int oldLeader2, int sender, Deque<Integer> path){
        super(sender);
        this.newLeader = newLeader;
        this.oldLeader1 = oldLeader1;
        this.oldLeader2 = oldLeader2;
        this.path = path;
    }
    
    public NewLeaderMsg(int sender, int newLeader, MWOEMsg m){
        super(sender);
        this.newLeader = newLeader;
        this.oldLeader1 = m.compLeader;
        this.oldLeader2 = m.externalLeader;
        this.path = m.path;
    }
}