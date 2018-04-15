package dc_project2;

import java.util.Collection;
import java.util.function.Predicate;

public final class BooleanCollection{
  private BooleanCollection(){}
  public static boolean allTrue(Collection c, Predicate p){ return c.stream().allMatch(p); }
}