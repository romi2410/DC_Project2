package dc_project2;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Predicate;

public final class BoolCollection{
  private BoolCollection(){}
  public static boolean allTrue(Collection c, Predicate p){ return c.stream().allMatch(p); }
  public static boolean allTrue(Collection c){ return !Arrays.asList(c).contains(false); }
  public static boolean allTrue(HashMap c){ return !Arrays.asList(c.values()).contains(false); }
}