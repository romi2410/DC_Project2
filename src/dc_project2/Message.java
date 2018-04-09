package dc_project2;

abstract class Message{
    int level, sender;
    
    public Message(int level, int sender){
        this.level = level;
        this.sender = sender;
    }
}