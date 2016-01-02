package objenome.op.cas.util;

import com.google.common.collect.Lists;
import objenome.op.cas.Expr;
import objenome.op.cas.Num;
import objenome.op.cas.Product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public final class ArrayLists {
    
//    public static void main(String[] args) {
//        ArrayList<Object> toSplit = new ArrayList<Object>();
//        toSplit.add("good");
//        toSplit.add("bald");
//        toSplit.add("bad");
//        toSplit.add("bad");
//        toSplit.add("not");
//        toSplit.add("not");
//        toSplit.add("bad");
//        toSplit.add("good");
//        toSplit.add("not");
//        toSplit.add("bad");
//        Object[] splitOn = {"good", "bad"};
//        ArrayList<Object> objectFound = new ArrayList<Object>();
//
//        System.out.println(toSplit);
//        System.out.println("splitOn: " + Arrays.toString(splitOn));
//        System.out.println(" 0: " + split(toSplit, splitOn, 0, objectFound) + "\n    " + objectFound);
//        System.out.println("-1: " + split(toSplit, splitOn, -1, objectFound) + "\n    " + objectFound);
//        System.out.println(" 1: " + split(toSplit, splitOn, 1, objectFound) + "\n    " + objectFound);
//        System.out.println(" 2: " + split(toSplit, splitOn, 2, objectFound) + "\n    " + objectFound);
//    }
    
    public static <T extends ArrayList> ArrayList<List<T>> split(ArrayList<T> arrayList, T[] splitOn) {
        return split(arrayList, splitOn, 0);
    }
    
    public static <T extends ArrayList> ArrayList<List<T>> split(ArrayList<T> arrayList, T[] splitOn, int direction) {
        return split(arrayList, splitOn, direction, null);
    }
    
    public static <T extends ArrayList> ArrayList<List<T>> split(ArrayList<T> arrayList, T[] splitOn, int direction, ArrayList objectFound) {
        if (!containsIn(arrayList, splitOn)) {
            return null;
        }
        
        arrayList = Lists.newArrayList(arrayList);
        ArrayList<List<T>> splitted = new ArrayList();
        objectFound.clear();

        //noinspection IfStatementWithTooManyBranches
        if (direction == 0) {
            while (containsIn(arrayList, splitOn)) {
                List<? extends T> subList = arrayList.subList(0, indexIn(arrayList, splitOn, objectFound));
                List<T> tmp = new ArrayList<>(subList);
                if (!tmp.isEmpty()) {
                    splitted.add(tmp);
                }
                
                subList.clear();
                arrayList.remove(0);
            }
            if (!arrayList.isEmpty()) {
                splitted.add(arrayList);
            }
        }
        else if (direction == -1) {
            List<? extends T> subList = arrayList.subList(0, indexIn(arrayList, splitOn, objectFound));
            List<T> tmp = new ArrayList(subList);
            
            splitted.add(tmp);
            
            subList.clear();
            arrayList.remove(0);
            
            splitted.add(arrayList);
        }
        else if (direction == 1) {
            List<T> subList = arrayList.subList(lastIndexIn(arrayList, splitOn, objectFound) + 1, arrayList.size());
            List<T> tmp = new ArrayList<>(subList);
            
            splitted.add(tmp);
            
            subList.clear();
            arrayList.remove(arrayList.size() - 1);
            
            splitted.add(0, arrayList);
        }
        else if (direction == 2) {
            while (containsIn(arrayList, splitOn)) {
                List<? extends T> subList = arrayList.subList(0, indexInMultFound(arrayList, splitOn, objectFound));
                List<T> tmp = new ArrayList<>(subList);
                if (!tmp.isEmpty()) {
                    splitted.add(tmp);
                } else {
                    objectFound.remove(objectFound.size() - 1);
                }
                
                subList.clear();
                arrayList.remove(0);
            }
            if (!arrayList.isEmpty()) {
                splitted.add(arrayList);
            } else {
                objectFound.remove(objectFound.size() - 1);
            }
        }
        
        return splitted;
    }

    public static <T> int indexIn(List<? extends T> arrayList, T[] objects) {
	    return indexIn(arrayList, objects, null);
    }
    
    public static <T> int indexIn(List<? extends T> arrayList, T[] objects, List<T> objectFound) {
        int index = -1;
        
        for (T object : objects) {
            int indexOf = arrayList.indexOf(object);
            if (indexOf != -1 && (index == -1 || indexOf < index)) {
                index = indexOf;
                if (objectFound != null) {
                    if (objectFound.isEmpty()) {
                        objectFound.add(arrayList.get(indexOf));
                    } else {
                        objectFound.set(0, arrayList.get(indexOf));
                    }
                }
            }
        }
        
        return index;
    }
    
    public static <T> int indexInMultFound(List<? extends T> arrayList, T[] objects, List<T> objectFound) {
        int initObjectsFound = objectFound.size();
        int index = -1;
        
        for (T object : objects) {
            int indexOf = arrayList.indexOf(object);
            if (indexOf != -1 && (index == -1 || indexOf < index)) {
                index = indexOf;
                if (objectFound != null) {
                    if (objectFound.size() == initObjectsFound) {
                        objectFound.add(arrayList.get(indexOf));
                    } else {
                        objectFound.set(initObjectsFound, arrayList.get(indexOf));
                    }
                }
            }
        }
        
        return index;
    }

    public static <T extends ArrayList> int lastIndexIn(List<T> arrayList, T[] objects) {
	    return lastIndexIn(arrayList, objects, null);
    }
    
    public static <T extends ArrayList> int lastIndexIn(List<T> arrayList, T[] objects, List<T> objectFound) {
        int index = -1;
        
        for (T object : objects) {
            int lastIndexOf = arrayList.lastIndexOf(object);
            if (lastIndexOf > index) {
                index = lastIndexOf;
                if (objectFound != null) {
                    if (objectFound.isEmpty()) {
                        objectFound.add(arrayList.get(lastIndexOf));
                    } else {
                        objectFound.set(0, arrayList.get(lastIndexOf));
                    }
                }
            }
        }
        
        return index;
    }
    
    public static <T> boolean containsIn(Collection<? extends T> arrayList, T[] objects) {
        for (T object : objects) {
            if (arrayList.contains(object)) return true;
        }
        return false;
    }
    
    public static ArrayList<Expr> copyAll(List<Expr> exprs, HashMap<Expr, Expr> subs) {
        ArrayList<Expr> copies = new ArrayList(exprs.size());
        copies.addAll(exprs.stream().map(expr -> expr.copy(subs)).collect(Collectors.toList()));
        return copies;
    }
    
    public static Expr productArrToExpr(ArrayList<Expr> exprs) {
        return productArrToExpr(exprs, true);
    }
    
    public static Expr productArrToExpr(ArrayList<Expr> exprs, boolean simplify) {
        if (exprs.isEmpty()) return Num.make(1);
        if (exprs.size() == 1) return exprs.get(0);
        return Product.make(exprs, simplify);
    }
    
    public static <T> ArrayList<T> castAll(Iterable arrList, Class<T> toClass) {
        ArrayList<T> newArrList = new ArrayList<>();
        for (Object o : arrList) {
            newArrList.add((T) o);
        }
        return newArrList;
    }
    
    public static String dumpAll(Iterable<Expr> exprs) {
        ArrayList<String> strs = new ArrayList<>();
        for (Expr expr: exprs) {
            strs.add(expr.toString());
        }
        return strs.toString();
    }
    
    public static boolean elemExprsEqual(List<? extends Expr> al1, List<? extends Expr> al2) {
        if (al1.size() != al2.size()) return false;
        for (int i = 0; i < al1.size(); i++) {
            if (!al1.get(i).equalsExpr(al2.get(i))) return false;
        }
        return true;
    }
    
}
