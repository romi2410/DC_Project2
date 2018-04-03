package dc_project2;

import java.util.Scanner;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DC_Project2 {
  
  static boolean test = false;
  static HashMap<Integer, Integer> uids2ports = new HashMap<Integer, Integer>();
  static HashMap<Integer, String> uids2hosts = new HashMap<Integer, String>();
  static Logger logger = LoggerFactory.getLogger(DC_Project2.class);

  public static void main(String[] args) throws IOException {
        //logger.info("Hello World");
        
        if(args.length>=2 && args[1].equalsIgnoreCase("test"))
        {
            test = true;
            System.out.println("Running in test (host will be localhost)");
        }
        
        try
        {
            Scanner sc = new Scanner(new File(args[0]));
            HashMap<Integer, Node> nodes = parseLines(sc);
            
            for(Node node: nodes.values())
            {
                uids2ports.put(node.uid, node.port);
                uids2hosts.put(node.uid, node.hostname);
            }
            
            System.out.println("\n Mapping from UIDs to Ports:");
            nodes.values().forEach(node -> System.out.println(node.uid + " -> " + uids2hosts.get(node.uid) + ":" + uids2ports.get(node.uid)));
            System.out.println();
        }
        catch(IOException e)
        {
            System.out.println("File " + args[0] + " not found");
            System.exit(0);
        }
    }
  
    public static HashMap<Integer, Node> parseLines(Scanner sc){
        
        HashMap<Integer, Node> nodes = new HashMap<>();
        HashSet<Integer> started = new HashSet<>();
        int numNodes = 0;
        int numEdges = 0;
        
        // Reading number of nodes from config file
        while(numNodes==0)
        {
            String line = sc.nextLine();
            if(!(line.startsWith("#") || line.trim().length() == 0))
            {
                numNodes = Integer.parseInt(line.trim());
            }
        }
        
        // Creating nodes
        for(int i=0; i < numNodes; i++)
        {
            String line = sc.nextLine();
            if(!(line.startsWith("#") || line.trim().length() == 0))
            {
                String[] params = line.trim().split("\\s+");
                Node node = parseLine_Node(params);
                nodes.put(node.uid, node);
            }
            else
                i--;
        }
        
        // Checking if all servers have started
        while(started.size() < numNodes)
        {
            for(Node n: nodes.values())
                if(n.server)
                    started.add(n.uid);
        }
        
        // Creating edges - sockets 
        while(sc.hasNext())
        {
            String line = sc.nextLine();
            
            if(!(line.startsWith("#") || line.trim().length() == 0))
            {
                String[] params = line.trim().split("\\s+");
                double w = Double.parseDouble(params[1]);
                String[] tuple = params[0].substring(1, params[0].length()-1).split(",");
                Node node1 = nodes.get(Integer.parseInt(tuple[0]));
                Node node2 = nodes.get(Integer.parseInt(tuple[1]));
                
                node1.connectTo(node2.hostname, node2.port, node2.uid, w);
                node2.connectTo(node1.hostname, node1.port, node1.uid, w);
                numEdges += 2;
            }
        }
        
        // Checking if all edges are created
        int edgeCnt = 0;
        while(edgeCnt < numEdges)
        {
            edgeCnt = 0;
            for(Node node: nodes.values())
                edgeCnt += node.numEdges;
        }
        
        // Initiate GHS
        for(Node node: nodes.values())
            node.initGHS();
        
        return nodes;
    }
    
    public static Node parseLine_Node(String[] nodeParams){
        String uid = nodeParams[0];
        String hostname = nodeParams[1];
        String port = nodeParams[2];
        return new Node(Integer.parseInt(uid), hostname, Integer.parseInt(port), test);
    }
  
}
