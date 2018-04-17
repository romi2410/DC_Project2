package dc_project2;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Predicate;

public final class BoolCollection{
  private BoolCollection(){}
  public static boolean allTrue(Collection c, Predicate p){ return c.stream().allMatch(p); }
  public static boolean allTrue(Collection<Boolean> c){
    TestingMode.print(c.toString());
    for(boolean b : c)
      if(!b) return false;
    return true;
  }
  public static boolean allTrue(HashMap c){ return allTrue(c.values()); }
}