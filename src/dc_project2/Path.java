package dc_project2;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class Path implements java.io.Serializable{
  private Deque<Integer> path = new ArrayDeque<Integer>();  // leaf-to-root; implemented as a Stack
  Path(int external_leaf){    path.add(external_leaf);  }
  public void add(int node){  path.add(node);           }
  public Iterator<Integer> iteratorLeaf2Root(){ return path.iterator();           }
  public Iterator<Integer> iteratorRoot2Leaf(){ return path.descendingIterator(); }
  
  public int findPrecedingNode(int nodeUID, int parent){
    // if node is in path, find the node that precedes it
      // (equivalent to successor in reversed path)
    Iterator<Integer> iter = iteratorLeaf2Root();
    while(iter.hasNext())
      if(iter.next().equals(nodeUID))
        return iter.next();
    
    // if node not in path, return node's original parent
    return parent;
  }
}