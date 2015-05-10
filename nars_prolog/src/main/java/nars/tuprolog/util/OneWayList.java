package nars.tuprolog.util;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class OneWayList<E> {
    
    final private E head;
    private OneWayList<E> tail;
    
    public OneWayList(E head, OneWayList<E> tail){
        this.head = head;
        this.tail = tail;
    }

    public static <T> OneWayList<T> transform(List<T> list){
        if(list.isEmpty()) return null;
        return new OneWayList<>(list.remove(0),transform(list));
    }

    public static <T> OneWayList<T> transform2(Iterable<T> list){
        return transform2(list.iterator());
    }
    /**
     * Transforms given list into a OneWayList without any modification
     * to it
     * 
     * Method introduced during revision by Paolo Contessi
     *
     * @param list  Perceive list to be transformed
     * @return      An equivalent OneWayList
     */
    public static <T> OneWayList<T> transform2(Iterator<T> list){
        OneWayList<T> result = null;
        OneWayList<T> p = null;

        if (list == null) {
            return null;// Collections.EMPTY_LIST;
        }

        T obj;
        while (list.hasNext()) {
            obj = list.next();

            OneWayList<T> l = new OneWayList<>(obj, null);

            if(result == null){
                result = p = l;
            } else {
                p.tail = l;
                p = l;
            }
        }

        return result;
    }
    
    public E getHead() {
        return head;
    }
    
    /*    public void setHead(E head) {
        this.head = head;
    }*/


    public OneWayList<E> getTail() {
        return tail;
    }
    
    public void setTail(OneWayList<E> tail) {
        this.tail = tail;
    }

    public void addLast(OneWayList<E> newTail){
        if(tail == null){
            tail = newTail;
            return;
        }
        tail.addLast(newTail);
    }
    
    public OneWayList<E> get(final int index){
        if(tail == null) throw new NoSuchElementException();
        if(index <= 0) return this;
        return tail.get(index-1);
    }

    public String toString() {
        String elem;
        if(head==null) elem = "null";
            else elem = head.toString();
        if(tail==null) return '[' +elem+ ']';
        return '[' +tail.toString(elem)+ ']';
    }
    
    private String toString(String elems){
        String elem;
        if(head==null) elem = "null";
            else elem = head.toString();
        if(tail==null) return elems+ ',' +elem;
        return elems+ ',' +tail.toString(elem);
    }
    
}