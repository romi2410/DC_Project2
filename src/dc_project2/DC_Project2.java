package dc_project2;

import java.util.Scanner;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DC_Project2 {
  
  static boolean test = false;
  //static Logger logger = LoggerFactory.getLogger(DC_Project2.class);

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
            
            int numNodes = readNumNodes(sc);
            HashMap<Integer, Node> nodes = readAndCreateNodes(sc, numNodes);
            while(serversStillStarting(nodes, numNodes)){}
            
            int numEdges = readAndCreateEdges(sc, nodes);
            while(socketsStillStarting(nodes, numEdges)){}
            
            for(Node node: nodes.values())
                node.initGHS();
        }
        catch(IOException e)
        {
            System.out.println("File " + args[0] + " not found");
            System.exit(0);
        }
    }
  
  
  
  
    public static int readNumNodes(Scanner sc){
      int numNodes = 0;
      while(numNodes==0)
        {
            String line = sc.nextLine();
            if(!(line.startsWith("#") || line.trim().length() == 0))
            {
                numNodes = Integer.parseInt(line.trim());
            }
        }
      return numNodes;
    }
    
    public static Node parseLine_Node(String[] nodeParams){
        String uid = nodeParams[0];
        String hostname = nodeParams[1];
        String port = nodeParams[2];
        return new Node(Integer.parseInt(uid), hostname, Integer.parseInt(port), test);
    }
    
    public static HashMap<Integer, Node> readAndCreateNodes(Scanner sc, int numNodes){
        HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();
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
        return nodes;
    }
    
    public static boolean serversStillStarting(HashMap<Integer, Node> nodes, int numNodes){
      HashSet<Integer> started = new HashSet<>();
      for(Node n: nodes.values())
          if(n.server)
              started.add(n.uid);
      return started.size() < numNodes;
    }
    
    // returns number of edges
    public static int readAndCreateEdges(Scanner sc, HashMap<Integer, Node> nodes){
      int numEdges = 0;
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
      return numEdges;
    }
    
    public static boolean socketsStillStarting(HashMap<Integer, Node> nodes, int numEdges){
        int edgeCnt = 0;
        edgeCnt = 0;
        for(Node node: nodes.values())
            edgeCnt += node.numEdges;
        return edgeCnt < numEdges;
    }
  
}
