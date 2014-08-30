package alice.util;

import java.util.*;

public class OneWayList<E> {
    
    private E head;
    private OneWayList<E> tail;
    
    public OneWayList(E head, OneWayList<E> tail){
        this.head = head;
        this.tail = tail;
    }

    public static <T> OneWayList<T> transform(List<T> list){
        if(list.isEmpty()) return null;
        return new OneWayList<T>(list.remove(0),transform(list));
    }
    
    /**
     * Transforms given list into a OneWayList without any modification
     * to it
     * 
     * Method introduced during revision by Paolo Contessi
     *
     * @param list  Input list to be transformed
     * @return      An equivalent OneWayList
     */
    public static <T> OneWayList<T> transform2(List<T> list){
        OneWayList<T> result = null;
        OneWayList<T> p = null;

        for(T obj : list){
            OneWayList<T> l = new OneWayList<T>(obj, null);

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
    
    public void setHead(E head) {
        this.head = head;
    }


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
    
    public OneWayList<E> get(int index){
        if(tail == null) throw new NoSuchElementException();
        if(index <= 0) return this;
        return tail.get(index-1);
    }

    public String toString() {
        String elem;
        if(head==null) elem = "null";
            else elem = head.toString();
        if(tail==null) return "["+elem+"]";
        return "["+tail.toString(elem)+"]";
    }
    
    private String toString(String elems){
        String elem;
        if(head==null) elem = "null";
            else elem = head.toString();
        if(tail==null) return elems+","+elem;
        return elems+","+tail.toString(elem);
    }
    
}