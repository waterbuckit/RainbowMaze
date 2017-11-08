package mazesolver;

import java.util.function.Predicate;

public class Are {

    public static <T> Predicate<T> NOT(Predicate<T> p) {
        return p.negate();
    }

}
