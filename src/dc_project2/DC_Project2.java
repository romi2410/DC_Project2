package dc_project2;

import java.util.Scanner;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class DC_Project2 {

  public static void main(String[] args) throws IOException {
    
    if(args.length>=2 && args[1].equalsIgnoreCase("test"))
      TestingMode.turnOn();

    try
    {
      Scanner sc = new Scanner(new File(args[0]));

      int numNodes = readNumNodes(sc);
      HashMap<Integer, Node> nodes = readAndCreateNodes(sc, numNodes);
      Synchronizer s = initSynchronizer(nodes);
      while(serversStillStarting(nodes, numNodes, s)){}

      int numEdges = readAndCreateEdges(sc, nodes) + 2*numNodes;  //2*numNodes = # edges between nodes and synchronizer
      s.connectToNodes(new HashSet<>(nodes.values()));
      for(Node node: nodes.values())
          node.connectToSynchronizer(s.hostname, s.port);
      while(socketsStillStarting(nodes, numEdges, s)){}

      for(Node node: nodes.values())
          node.initGHS();
    }
    catch(IOException e)
    {
      System.out.println("File " + args[0] + " not found");
      System.exit(0);
    }

    while(true){
      System.out.println("Number of threads: " + Thread.activeCount());
      try{
        TimeUnit.SECONDS.sleep(2);
      } catch(InterruptedException e){
        System.out.println(e);
      }
    }
  }
  
  
  
  
    public static int readNumNodes(Scanner sc){
      int numNodes = 0;
      while(numNodes==0)
      {
        String line = sc.nextLine();
        if(!(line.startsWith("#") || line.trim().length() == 0))
          numNodes = Integer.parseInt(line.trim());
      }
      return numNodes;
    }
    
    public static Node parseLine_Node(String[] nodeParams){
      String uid = nodeParams[0];
      String hostname = nodeParams[1];
      String port = nodeParams[2];
      return new Node(Integer.parseInt(uid), hostname, Integer.parseInt(port));
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
    
    public static boolean serversStillStarting(HashMap<Integer, Node> nodes, int numNodes, Synchronizer sync){
      HashSet<Integer> started = new HashSet<>();
      for(Node n: nodes.values())
        if(n.server)
          started.add(n.uid);
      return (started.size() < numNodes) && sync.server;
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
    
    public static boolean socketsStillStarting(HashMap<Integer, Node> nodes, int numEdges, Synchronizer sync){
      int edgeCnt = 0;
      edgeCnt = 0;
      for(Node node: nodes.values())
        edgeCnt += node.numEdges;
      edgeCnt += sync.numEdges;
      return edgeCnt < numEdges;
    }
    
    // initializes the Synchronizer
      // with an arbitrary hostname from the config file
      // and one plus the largest port number from the config file
    public static Synchronizer initSynchronizer(HashMap<Integer, Node> nodes){
      String s_hostname = nodes.entrySet().iterator().next().getValue().hostname;
      int s_port = 0;                                                             
      for(Node node: nodes.values())
        s_port = Math.max(s_port, (node.port+1) % 65535);
      return new Synchronizer(nodes, s_hostname, s_port);
      //return new Synchronizer(nodes, "localhost", 10000);
    }
  
}
