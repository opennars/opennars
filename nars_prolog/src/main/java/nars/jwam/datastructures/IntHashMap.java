package nars.jwam.datastructures;

/*
 * This map is largely copy/paste from the built-in hashmap. 
 * The key is always an int. This saves 12 bytes per key.
 */
public class IntHashMap<V> {

    static final int MAXIMUM_CAPACITY = 1 << 30, NEXT = 0, KEY = 1, INDEXVALUE = 2, FIRST = 1;
    public static final int INT = 0, INTAR = 1, OBJ = 2;
    float loadFactor = 0.75f;
    int initialCapacity = 8;
    int threshold, size;
    public Object[] objtable; // for storing Object values
    public int[][] artable;
    int[] table; // for storing the hashes, index = indexFor(hash,table.length), value = entry index
    int[] entries; // for storing entry information
    int valuetype = 0;

    // iteration constants (ic)
    int icTableInd = 0, icEntry = 0, icNext = 0;

    public IntHashMap(int valuetype) {
        this.valuetype = valuetype;
        start();
    }

    public IntHashMap(int valuetype, int initialC) {
        initialCapacity = initialC;
        this.valuetype = valuetype;
        start();
    }

    public void start() {
        threshold = (int) (initialCapacity * loadFactor);
        size = 0;
        if (valuetype == OBJ) {
            objtable = new Object[initialCapacity];
        } else if (valuetype == INTAR) {
            artable = new int[initialCapacity][];
        }
        table = new int[initialCapacity];
        entries = new int[initialCapacity * 3 + 1];
        resetData(table, entries);
    }

    public void resetData(int[] table, int[] entries) {
        for (int i = 0; i < table.length; i++) {
            table[i] = -1; // set the table references to -1
        }
        for (int i = 0; i < entries.length; i += 3) {
            entries[i] = Integer.MIN_VALUE; // set next value to min_value, so the get function can "see" that it is not instantiated
        }
        entries[entries.length - FIRST] = Integer.MIN_VALUE;
    }

    static int indexFor(int h, int length) {
        return (length - 1) & h;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public V getObj(int key) {
        if (table.length > 0) {
            int e = table[indexFor(key, table.length)];
            while (e >= 0) {// while its instantiated
                if (entries[e + KEY] == key) {
                    return (V) objtable[entries[e + INDEXVALUE]]; // if key matches return the value
                }
                e = entries[e]; // otherwise move on
            }
        }
        return null;
    }

    public int getInt(int key) {
        if (table.length > 0) {
            int e = table[indexFor(key, table.length)];
            while (e >= 0) {// while its instantiated
                if (entries[e + KEY] == key) {
                    return entries[e + INDEXVALUE]; // if key matches return the value
                }
                e = entries[e]; // otherwise move on
            }
        }
        return Integer.MIN_VALUE; // for lack of a null value
    }

    public boolean containsKey(int key) { // bit larger instead of getObj/getInt, since with those functions you would either have unnecessary typecasting or cannot have Integer.MIN_VALUE values
        int e = table[indexFor(key, table.length)];
        while (e >= 0) {// while its instantiated
            if (entries[e + KEY] == key) {
                return true; // if key matches return true
            }
            e = entries[e]; // otherwise move on
        }
        return false;
    }

    public void putObj(int key, V value) {
        int tableindex = indexFor(key, table.length);
        int e = table[tableindex];
        while (e >= 0) {// while its instantiated
            if (entries[e + KEY] == key) {// if key matches override the value
                objtable[entries[e + INDEXVALUE]] = value;
                return; // return the old value
            }
            e = entries[e]; // otherwise move on
        }
        objtable[newEntry(key, tableindex, 0)] = value; // put the object in place
        if (size++ >= threshold) {
            resize(2 * table.length);
        }
    }

    public int putInt(int key, int value) {
        int tableindex = indexFor(key, table.length);
        int e = table[tableindex];
        while (e >= 0) {// while its instantiated
            if (entries[e + KEY] == key) {// if key matches override the value
                int oldValue = entries[e + INDEXVALUE];
                entries[e + INDEXVALUE] = value;
                return oldValue; // return the old value
            }
            e = entries[e]; // otherwise move on
        }
        newEntry(key, tableindex, value); // put the object in place
        if (size++ >= threshold) {
            resize(2 * table.length);
        }
        return Integer.MIN_VALUE;
    }

    public int newEntry(int key, int tableindex, int value) {
        boolean fillEmpty = entries[entries.length - FIRST] == Integer.MIN_VALUE; // check if there are empty spaces inside the array
        int entryindex = fillEmpty ? size * 3 : entries[entries.length - FIRST]; // get the entry index
        entries[entries.length - FIRST] = fillEmpty ? Integer.MIN_VALUE : entries[entryindex + 1]; // update the first linked list node for free spaces
        entries[entryindex + KEY] = key; // set the entry values
        entries[entryindex + INDEXVALUE] = (valuetype == INT) ? value : entryindex / 3; // either set the intvalue in the array or get the objtable index
        if (table[tableindex] >= 0) // there is a next
        {
            entries[entryindex] = table[tableindex]; // put this entry on top of the list/
        } else {
            entries[entryindex] = -1; // not MIN_VALUE since that means that this entry is uninstantiated
        }
        table[tableindex] = entryindex; // set this entry as liststart in the hashtable
        return entries[entryindex + INDEXVALUE];// return the objarray spacenr. or value
    }

    void resize(int newCapacity) {
        int[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }
        int[] newTable = new int[newCapacity];
        Object[] newObjTable = (valuetype != OBJ) ? null : new Object[newCapacity];
        int[][] newArTable = (valuetype != INTAR) ? null : new int[newCapacity][];
        int[] newEntries = new int[newCapacity * 3 + 1];
        resetData(newTable, newEntries);
        transfer(newTable, newObjTable, newArTable, newEntries);
        table = newTable;
        objtable = newObjTable;
        entries = newEntries;
        threshold = (int) (newCapacity * loadFactor);
    }

    void transfer(int[] newTable, Object[] newObjTable, int[][] newArTable, int[] newEntries) {
        int entryCounter = 0;
        int e = 0;
        int newIndex = 0;
        for (int j = 0; j < table.length; j++) {
            e = table[j];
            while (e >= 0) {
                newIndex = entryCounter * 3; // make a new entry
                newEntries[newIndex + KEY] = entries[e + KEY];
                newEntries[newIndex + INDEXVALUE] = (valuetype == INT) ? entries[e + INDEXVALUE] : entryCounter;
                newEntries[newIndex] = -1; // at first no part of a list
                if (valuetype == OBJ) {
                    Object o = objtable[entries[e + INDEXVALUE]];
                    newObjTable[entryCounter] = o;
                    objtable[entries[e + INDEXVALUE]] = null;
                } else if (valuetype == INTAR) {
                    int[] o = artable[entries[e + INDEXVALUE]];
                    newArTable[entryCounter] = o;
                    artable[entries[e + INDEXVALUE]] = null;
                }
                int tableIndex = indexFor(entries[e + KEY], newTable.length);
                if (newTable[tableIndex] >= 0) // there is already an entry in the new table
                {
                    newEntries[newIndex] = newTable[tableIndex]; // then put his entry on top of the list
                }
                newTable[tableIndex] = newIndex;// set this entry as start of the list at the new table
                entryCounter++;
                e = entries[e]; // continue with the next from the old list
            }
        }
    }

    public void removeObj(int key) {
        int e = removeEntryForKey(key);
        if (e >= 0) {
            objtable[e] = null; // remove from objtable
        }
    }

    public int removeInt(int key) {
        return removeEntryForKey(key);
    }

    final int removeEntryForKey(int key) {
        int i = indexFor(key, table.length);
        int prev = table[i];
        int e = table[i];
        while (e >= 0) {
            int next = entries[e];
            if (entries[e + KEY] == key) { // found the entry
                size--;
                int value = entries[e + INDEXVALUE];
                if (prev == e) {
                    table[i] = next; // update the linked list for the bucket
                } else {
                    entries[prev] = next;
                }
                entries[e + 1] = entries[entries.length - FIRST];// update the free entries list
                entries[e] = Integer.MIN_VALUE;
                entries[entries.length - FIRST] = e;
                return value;
            }
            prev = e;
            e = next;
        }
        return -1;
    }

    public void clear() {
        resetData(table, entries);
        if (valuetype == OBJ) {
            for (int i = 0; i < objtable.length; i++) {
                objtable[i] = null;
            }
        }
        if (valuetype == INTAR) {
            for (int i = 0; i < artable.length; i++) {
                artable[i] = null;
            }
        }
        size = 0;
    }

    public boolean containsValue(Object value) {
        for (int i = 0; i < objtable.length; i++) {
            if (value.equals(objtable[i])) {
                return true;
            }
        }
        return false;
    }

    // iterate as: for(int i = map.firstKey(); map.hasNext(); i = map.nextKey())
    public int firstKey() {
        for (icTableInd = 0; table[icTableInd] < 0; icTableInd++); // get the first table entry
        icEntry = table[icTableInd];
        icNext = -2;
        return entries[icEntry + KEY];
    }

    public int nextKey() {
        boolean firsthit = false;
        while (icTableInd < table.length) {
            if (icEntry >= 0) {
                if (firsthit) {
                    return entries[icEntry + KEY];
                }
                if (entries[icEntry] >= 0 && !firsthit) {
                    icNext = entries[icEntry];
                    icEntry = icNext;
                    return entries[icEntry + KEY];
                }
            }
            icTableInd++;
            firsthit = true;
            if (icTableInd < table.length) {
                icEntry = table[icTableInd];
            }
        }
        icEntry = -1;
        return -1;
    }

    public boolean hasNext() {
        if (icNext == -2) { // for the first call of a for loop
            icNext = -1;
            return true;
        }
        return icEntry >= 0;
    }

    @Override
    public String toString() {
        String r = "";
        for (int i = 0; i < entries.length - 1; i += 3) {
            if (entries[i] > Integer.MIN_VALUE) {
                r += entries[i + KEY] + " = ";
                r += (valuetype == INT) ? entries[i + INDEXVALUE] : (valuetype == OBJ ? objtable[entries[i + INDEXVALUE]].toString() : artable[entries[i + INDEXVALUE]]);
                r += "\r\n";
            }
        }
        for (int i = 0; i < entries.length; i++) {
            System.out.print(entries[i] + " ");
        }
        System.out.println("\r\n");
        return r.length() > 2 ? r.substring(0, r.length() - 2) : r;
    }
}
