package dc_project2;

import java.util.Scanner;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class DC_Project2 {

  static HashMap<Integer, Node> nodes;
  static Synchronizer synchronizer;
  static Scanner sc;
          
  public static void main(String[] args) throws IOException {
    
    if(args.length>=2 && args[1].equalsIgnoreCase("test"))
      TestingMode.turnOn();

    try
    {
      sc = new Scanner(new File(args[0]));
      int numNodes = startServers();
      startSenders(numNodes);
      for(Node node: nodes.values())
          node.initGHS();
    }
    catch(IOException e)
    {
      System.out.println("File " + args[0] + " not found");
      System.exit(0);
    }

    //while(true){
    System.out.println("Number of threads: " + Thread.activeCount());
    try{
      TimeUnit.SECONDS.sleep(2);
    } catch(InterruptedException e){
      System.out.println(e);
    }
    //}
  }
  
  public static int startServers(){
    int numNodes = readNumNodes();
    nodes = initNodes(numNodes);
    TestingMode.print("Starting nodes");
    synchronizer = initSynchronizer(nodes);
    TestingMode.print("Starting synchronizer");
    while(serversStillStarting(nodes, numNodes, synchronizer)){
      // nop
    }
    TestingMode.print("Started all servers");
    return numNodes;
  }
  public static void startSenders(int numNodes){
    TestingMode.print("Starting node->node senders");
    int numEdges = initEdges(nodes);
    TestingMode.print("Starting synchronizer->node senders");
    synchronizer.connectToNodes(new HashSet<Node>(nodes.values()));
    TestingMode.print("Starting node->synchronizer senders");
    for(Node node: nodes.values())
        node.connectToSynchronizer(synchronizer.hostname, synchronizer.port);
    while(socketsStillStarting(nodes, numEdges, synchronizer)){
      // nop
    }
    TestingMode.print("Started all senders");
  }
  
    public static int readNumNodes(){
      int numNodes = 0;
      while(numNodes==0)
      {
        String line = sc.nextLine();
        if(!(line.startsWith("#") || line.trim().length() == 0))
          numNodes = Integer.parseInt(line.trim());
      }
      return numNodes;
    }
    
    public static HashMap<Integer, Node> initNodes(int numNodes){
      HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();
      int nodesInitialized = 0;
      while(nodesInitialized < numNodes){
        String line = sc.nextLine();
        if(!(line.startsWith("#") || line.trim().length() == 0))
        {
          String[] params = line.trim().split("\\s+");
          int uid = Integer.parseInt(params[0]);
          String hostname = params[1];
          int port = Integer.parseInt(params[2]);
          nodes.put(uid, new Node(uid, hostname, port));
          nodesInitialized++;
        }
      }
      return nodes;
    }
    
    public static boolean serversStillStarting(HashMap<Integer, Node> nodes, int numNodes, Synchronizer sync){
      HashSet<Integer> started = new HashSet<>();
      for(Node n: nodes.values())
        if(n.server)
          started.add(n.uid);
      return (started.size() < numNodes) && sync.serverUp;
    }
    
    // returns number of edges
    public static int initEdges(HashMap<Integer, Node> nodes){
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
      return edgeCnt < numEdges && sync.sendersUp;
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
    }
  
}
