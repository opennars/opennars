package nars.op.software.scheme;

import java.util.List;

public enum Util {
    ;

    public static <T> T first(List<T> list) {
        return list.get(0);
    }

    public static <T> List<T> rest(List<T> list) {
        return list.subList(1, list.size());
    }
}
