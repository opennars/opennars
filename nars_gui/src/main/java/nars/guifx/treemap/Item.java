package nars.guifx.treemap;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Tadas Subonis <tadas.subonis@gmail.com>
 */
public interface Item extends Comparable<Item> {

    Object getId();

    double getSize();

    String getLabel();

    boolean isContainer();

    SortedSet<Item> getItems();

    static DefaultItem get(Object o, double size) {
        return new DefaultItem(o, size);
    }
    static DefaultItem get(Object o, double size, Object firstChild, double firstChildSize) {
        DefaultItem i = new DefaultItem(o, size);
        i.add(firstChild,firstChildSize);
        return i;
    }

    class DefaultItem implements Item {
        private double size;
        private final Object id;

        public DefaultItem(Object o, double size) {
            id = o;
            this.size = size;
        }

        @Override
        public Object getId() {
            return id;
        }

        @Override
        public double getSize() {
            return size;
        }

        @Override
        public String getLabel() {
            return id.toString();
        }

        final TreeSet<Item> children = new TreeSet();

        public void add(Object childID, double childsize) {
            children.add(Item.get(childID, childsize));
            size += childsize;
        }

        @Override
        public boolean isContainer() {
            return !children.isEmpty();
        }

        @Override
        public SortedSet<Item> getItems() {
            return children;
        }

        @Override
        public boolean equals(Object o) {
            return compareTo((Item)o)==0;
        }

        @Override
        public int compareTo(Item o) {
            return Double.compare(getSize(), o.getSize());
        }
    }
}
