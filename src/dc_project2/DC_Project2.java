package dc_project2;
import java.util.Scanner;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DC_Project2 {
  
  static boolean test = false;
  static HashMap<Integer, Integer> uids2ports = new HashMap<Integer, Integer>();
  static HashMap<Integer, String> uids2hosts = new HashMap<Integer, String>();
  static Logger logger = LoggerFactory.getLogger(DC_Project2.class);

  public static void main(String[] args) throws IOException {
        logger.info("Hello World");
        if(args.length>=2 && args[1].equalsIgnoreCase("test")){
          test = true;
          System.out.println("Running in test (host will be localhost)");
        }
        try{
            Scanner sc=new Scanner(new File(args[0]));
            HashMap<Integer, Node> nodes = parseLines(sc);
            
            for(Node node: nodes.values()){
              uids2ports.put(node.uid, node.port);
              uids2hosts.put(node.uid, node.hostname);
            }
            System.out.println("\n Mapping from UIDs to Ports:");
            nodes.values().forEach(node -> System.out.println(node.uid + " -> " + uids2hosts.get(node.uid) + ":" + uids2ports.get(node.uid)));
            System.out.println();
        }
        catch(IOException e){
            System.out.println("File " + args[0] + " not found");
            System.exit(0);
        }
  }
  
    public static HashMap<Integer, Node> parseLines(Scanner sc){
        HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();
        int numNodes = 0;
        while(numNodes==0){
          String line = sc.nextLine();
          if(!(line.startsWith("#") || line.trim().length()==0)){
            numNodes = Integer.parseInt(line.trim());
          }
        }
        // Create nodes
        for(int i=0; i<numNodes; i++){
          String line = sc.nextLine();
          if(!(line.startsWith("#") || line.trim().length()==0)){
            String[] params = line.trim().split("\\s+");
            Node node = parseLine_Node(params);
            nodes.put(node.uid, node);
          }
          else i--;
        }
        // Check all servers started
        HashSet<Integer> started = new HashSet<Integer>();
        while(started.size()<numNodes){
          for(Node n: nodes.values())
            if(n.server)
              started.add(n.uid);
        }
        // Create edges
        while(sc.hasNext()){
          String line = sc.nextLine();
          if(!(line.startsWith("#") || line.trim().length()==0)){
            String[] params = line.trim().split("\\s+");
            double w = Double.parseDouble(params[1]);
            String[] tuple = params[0].substring(1, params[0].length()-1).split(",");
            int n1 = Integer.parseInt(tuple[0]);
            int n2 = Integer.parseInt(tuple[1]);
            nodes.get(n1).connectTo(nodes.get(n2).hostname, nodes.get(n2).port, nodes.get(n2).uid, w);
            nodes.get(n2).connectTo(nodes.get(n1).hostname, nodes.get(n1).port, nodes.get(n2).uid, w);
          }
        }
        return nodes;
    }
    public static Node parseLine_Node(String[] nodeParams){
        String uid = nodeParams[0];
        String hostname = nodeParams[1];
        String port = nodeParams[2];
        return new Node(Integer.parseInt(uid), hostname, Integer.parseInt(port), test);
    }
  
}
