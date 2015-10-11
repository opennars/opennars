package nars.util.space;

import java.util.function.Consumer;

/**
 * Created by me on 10/11/15.
 */


/******************************************************************************
 * Compilation:  javac QuadTree.java
 * Execution:    java QuadTree M N
 * <p>
 * Quad tree.
 ******************************************************************************/

public class QuadTree<Key extends Comparable<Key>, Value> {
    private Node root;

    // helper node data type
    private static class Node<Key, Value> {
        Key x, y;              // x- and y- coordinates
        Node NW, NE, SE, SW;   // four subtrees
        Value value;           // associated data

        Node(Key x, Key y, Value value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Node{" +
                    x + "," + y + "=" + value +
                    '}';
        }
    }


    /***********************************************************************
     * Insert (x, y) into appropriate quadrant
     ***************************************************************************/
    public void insert(Key x, Key y, Value value) {
        root = insert(root, value, x, y);
    }

    private Node insert(Node<Key, Value> h, Value value, Key x, Key y) {
        if (h == null) return new Node(x, y, value);
            //// if (eq(x, h.x) && eq(y, h.y)) h.value = value;  // duplicate
        else if (less(x, h.x) && less(y, h.y)) h.SW = insert(h.SW, value, x, y);
        else if (less(x, h.x) && !less(y, h.y)) h.NW = insert(h.NW, value, x, y);
        else if (!less(x, h.x) && less(y, h.y)) h.SE = insert(h.SE, value, x, y);
        else if (!less(x, h.x) && !less(y, h.y)) h.NE = insert(h.NE, value, x, y);
        return h;
    }


    /***********************************************************************
     * Range search.
     ***************************************************************************/

    public void query2D(Interval2D<Key> rect, Consumer<Node<Key,Value>> c) {
        query2D(root, rect, c);
    }

    public void query2D(Node<Key,Value> h, Interval2D<Key> rect, Consumer<Node<Key,Value>> c) {
        if (h == null) return;
        Key xmin = rect.minx;
        Key ymin = rect.miny;
        Key xmax = rect.maxx;
        Key ymax = rect.maxy;
        if (rect.contains(h.x, h.y))
            c.accept(h); //StdOut.println("    (" + h.x + ", " + h.y + ") " + h.value);

        if (less(xmin, h.x) && less(ymin, h.y)) query2D(h.SW, rect, c);
        if (less(xmin, h.x) && !less(ymax, h.y)) query2D(h.NW, rect, c);
        if (!less(xmax, h.x) && less(ymin, h.y)) query2D(h.SE, rect, c);
        if (!less(xmax, h.x) && !less(ymax, h.y)) query2D(h.NE, rect, c);
    }


    /***************************************************************************
     * helper comparison functions
     ***************************************************************************/

    private static  <Key extends Comparable> boolean less(Key k1, Key k2) {
        return k1.compareTo(k2) < 0;
    }

    private static  <Key extends Comparable> boolean lessOrEqual(Key k1, Key k2) {
        return k1.compareTo(k2) <= 0;
    }

    private static <Key extends Comparable> boolean eq(Key k1, Key k2) {
        return k1.compareTo(k2) == 0;
    }


    /***************************************************************************
     * test client
     ***************************************************************************/
    public static void main(String[] args) {
        int M = 400;
        int N = 400;

        QuadTree<Integer, String> st = new QuadTree<Integer, String>();

        // insert N random points in the unit square
        for (int i = 0; i < N; i++) {
            Integer x = (int) (100 * Math.random());
            Integer y = (int) (100 * Math.random());
            // StdOut.println("(" + x + ", " + y + ")");
            st.insert(x, y, "P" + i);
        }
        System.out.println("Done preprocessing " + N + " points");

        // do some range searches
        for (int i = 0; i < M; i++) {
            Integer xmin = (int) (100 * Math.random());
            Integer ymin = (int) (100 * Math.random());
            Integer xmax = xmin + (int) (10 * Math.random());
            Integer ymax = ymin + (int) (20 * Math.random());

            Interval2D<Integer> rect = new Interval2D<Integer>(xmin, xmax, ymin, ymax);
            System.out.println(rect + " : ");
            st.query2D(rect, (n) -> {
                System.out.println(n);
            });
        }
    }

    private static class Interval2D<Key extends Comparable> {
        public Key minx, maxx, miny, maxy;

        public Interval2D(Key xmin, Key xmax, Key ymin, Key ymax) {
            this.minx = xmin;
            this.maxx = xmax;
            this.miny = ymin;
            this.maxy = ymax;
        }

        @Override
        public String toString() {
            return "Interval2D{" +
                    "minx=" + minx +
                    ", maxx=" + maxx +
                    ", miny=" + miny +
                    ", maxy=" + maxy +
                    '}';
        }

        public boolean contains(Key x, Key y) {
            if (lessOrEqual(this.minx, x)) {
                if (lessOrEqual(x, this.maxx)) {
                    if (lessOrEqual(this.miny, y)) {
                        if (lessOrEqual(y, this.maxy)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }


}
