package dc_project2;

class GHSMessage{
    int round;
    int leaderUID;
    int senderUID;

    public GHSMessage(int rnd, int uid){
        round = rnd;
        senderUID = uid;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(round).append(" ");
        sb.append(senderUID).append(" ");
        return sb.toString();
    }

    public static GHSMessage toGHSMsg(String rcvd_msg){
        String[] parsed_msg = rcvd_msg.split("\\s+");
        return new GHSMessage(Integer.parseInt(parsed_msg[1]),
                                Integer.parseInt(parsed_msg[2]));
    }
}