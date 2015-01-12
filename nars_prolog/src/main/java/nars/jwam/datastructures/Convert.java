package nars.jwam.datastructures;

import nars.jwam.WAM;

import java.util.ArrayList;
import java.util.List;

import static nars.jwam.datastructures.WAMToString.deref;

/**
 * This class contains functionalities to convert WAM data to Java data.
 *
 */
public class Convert {

    public static Object object(WAM wam, int[] source, int address) {
        int tag = WAM.cell_tag(source[address]);
        switch (tag) {
            case WAM.NUM:
                return numToDouble(wam, source, address);
            case WAM.STR:
                return predicateToArrayList(wam, source, address);
            case WAM.LIS:
                return listToArrayList(wam, source, address);
            case WAM.CON:
                return conToString(wam, source, address);
        }
        return null;        
    }
    public static Object termToObject(WAM wam, int[] source, int address, boolean no_bindings) {        
        if (!no_bindings) {
            address = deref(address, source);
        }
        return Convert.object(wam, source, address);
    }    
    public static Object cellToObject(WAM wam, int[] source, int heap_index) {
        int address = WAM.external_deref(source, heap_index);
        return object(wam, source, address);
    }

    public static int numToInt(WAM wam, int[] source, int heap_index) {
        return (int) numToDouble(wam, source, heap_index);
    }

    public static double numToDouble(WAM wam, int[] source, int heap_index) {
        int tag = WAM.cell_tag(source[heap_index]);
        int value = WAM.cell_value(source[heap_index]);
        if (tag != WAM.NUM) {
            throw new IllegalArgumentException("numToInt/numToDouble encountered non NUM cell, encountered tag = " + tag);
        }
        return wam.numbers().getDouble(value);
    }

    public static String conToString(WAM wam, int[] source, int heap_index) {
        int tag = WAM.cell_tag(source[heap_index]);
        int value = WAM.cell_value(source[heap_index]);
        if (tag != WAM.CON) {
            throw new IllegalArgumentException("conToString encountered non CON cell, encountered tag = " + tag);
        }
        return wam.strings().getWithoutArity(value << 7);
    }

    public static ArrayList<Object> predicateToArrayList(WAM wam, int[] source, int heap_index) {
        int tag = WAM.cell_tag(source[heap_index]); // For checking whether tag is STR
        int value = WAM.cell_value(source[heap_index]); // Address of the f/n cell
        if (tag != WAM.STR) {
            throw new IllegalArgumentException("predicateToArrayList encountered non STR cell, encountered tag = " + tag);
        }
        int a = value; // Start at the F/N cell
        value = WAM.cell_value(source[a]); // It holds the name and the argument count
        int args = WAM.numArgs(value); // Get the argument count

        Predicate<Object> result = new Predicate<Object>(args);
        
        result.add(wam.strings().getWithoutArity(value)); // First object is the functor
        for (int i = 1; i <= args; i++) // Add the arguments
        {
            result.add(cellToObject(wam, source, a + i));
        }
        return result;
    }

    public static List<Object> listToArrayList(WAM wam, int[] source, int heap_index) {
        int tag = WAM.cell_tag(source[heap_index]); // For checking whether tag is LIS
        int value = WAM.cell_value(source[heap_index]); // Address of the first element OR if [] it will be the []/0 identifier
        if (isEmptyList(wam, tag, value)) {
            return new ArrayList<Object>();
            //return Collections.EMPTY_LIST;
        }
        if (tag != WAM.LIS) {
            throw new IllegalArgumentException("listToArrayList encountered non LIS cell, encountered tag = " + tag);
        }
        List<Object> result = new ArrayList<Object>();
        int first = value;
        int second;
        int tag_second;
        do { // While going through a list
            result.add(cellToObject(wam, source, first)); // Add the first item of the pair
            second = WAM.external_deref(source, first + 1); // Get the address of the second item
            tag_second = WAM.cell_tag(source[second]); // Get its tag
            first = WAM.cell_value(source[second]); // Next first index
        } while (tag_second == WAM.LIS);
        return result;
    }

    public static void javaToWAMGeneral(WAM w, Object o, IntArrayList result) {
        if (o instanceof String) {
            result.add(javaToWAMString(w, (String) o));
        } else if (o instanceof Character) {
            result.add(javaToWAMString(w, (String) o));
        } else if (o instanceof Number) {
            result.add(javaToWAMNumber(w, (Double) ((Number) o).doubleValue()));
        } else if (o instanceof List<?>) {
            javaToWAMList(w, (List<Object>) o, result);
        } else if (o instanceof Convert.Predicate<?>) {
            javaToWAMPredicate(w, (Predicate<Object>) o, result);
        }
    }

    public static int javaToWAMNumber(WAM w, double d) {
        return WAM.newCell(WAM.NUM, w.numbers().new_number("" + d));
    }

    public static int javaToWAMString(WAM w, String s) {
        return WAM.newCell(WAM.CON, w.strings().add(s, 0) >>> 7);
    }

    public static void javaToWAMList(WAM w, List<Object> list, IntArrayList result) {
        IntArrayList items = new IntArrayList(); // Cells of the list items
        for (int i = list.size() - 1; i >= 0; i--) { // First add all the cells to the result
            javaToWAMGeneral(w, list.get(i), result);
            items.add(result.removeLast()); // Save the cell of the child
        }
        if (items.isEmpty()) {
            result.add(javaToWAMString(w, "[]"));
        } else {
            int last = result.size();
            result.add(items.get(0));
            result.add(javaToWAMString(w, "[]")); // The last cell contains emtpy tail
            for (int i = 1; i < items.size(); i++) {
                result.add(items.get(i)); // Add item
                result.add(WAM.newCell(WAM.LIS, last)); // Add list cell to previous 
                last = result.size() - 2;
            }
            result.add(WAM.newCell(WAM.LIS, last)); // Add list cell to point to the start of the list
        }
    }

    public static void javaToWAMPredicate(WAM w, Predicate<Object> pred, IntArrayList result) {
        String name = pred.remove(0).toString(); // Get the functor name
        IntArrayList args = new IntArrayList(); // For storing arguments
        for (int a = 0; a < pred.size(); a++) {	// For each argument
            javaToWAMGeneral(w, pred.get(a), result); // Convert it to WAM heap
            args.add(result.removeLast()); // Add last int to the arg array
        }
        int start = result.size(); // pointer to p/n cell
        result.add(WAM.newCell(WAM.PN, w.strings().add(name, args.size()))); // make p/n cell
        result.addAll(args); // Add the arguments
        result.add(WAM.newCell(WAM.STR, start)); // Add the STR cell that points to the structure
    }

    private static boolean isEmptyList(WAM wam, int tag, int value) {
        return tag == WAM.CON && wam.strings().get(value << 7).equals("[]/0");
    }

    /** TODO STringBuilder's */
    public static String javaConversionToString(Object o) {
        if (o == null) {
            return "null";
        }
        if (o instanceof String || o instanceof Double) {
            return o.toString();
        } else if (o instanceof Predicate) {
            Predicate<Object> p = (Predicate<Object>) o;
            String r = p.get(0).toString() + "(";
            for (int i = 1; i < p.size(); i++) {
                r += javaConversionToString(p.get(i));
                if (i < p.size() - 1) {
                    r += ",";
                }
            }
            return r + ")";
        } else if (o instanceof List) {
            String r = "[";
            ArrayList<Object> p = (ArrayList) o;
            for (int i = 0; i < p.size(); i++) {
                r += javaConversionToString(p.get(i));
                if (i < p.size() - 1) {
                    r += ",";
                }
            }
            return r + "]";
        }
        return "error: type not recognized";
    }

    // These two can be used to put data in. The predicate will take the first object as the name.
    public static final class Predicate<E> extends ArrayList<E> {

        public Predicate() {
            super();
        }

        public Predicate(int initialCapacity) {
            super(initialCapacity);
        }
        
    }

    //public static final class List<E> extends ArrayList<E> {    }
}
