package nars.jwam.compiler;

import java.util.ArrayList;

public class ModPrimIntKeyHashMap {

    int entry_count = 0, next = 1, entry_space = 0;
    public int[] table = new int[0];

    private int indexFor(int h) {
        return (h >>> 7) & entry_space;
    }

    public void put(int key, int value) {
        //System.out.println(key+" -> "+value);
        if (entry_count > 0 && contains(key, value)) {
            return;
        }
        entry_count++;
        int old_entry_space = entry_space;
        if (entry_count > entry_space) {
            entry_space = entry_space * 2 + 1; // c.f. 15 goes to 31: 01111 => 11111, its the entry space that gets put in switch_on_X as an argument
        }
        next = entry_space + 1;
        int[] new_table = new int[entry_space + 1 + entry_count * 3];
        //System.out.println(entry_space+" "+entry_count+" "+new_table.length);
        for (int i = 0; i < new_table.length; i++) {
            new_table[i] = Integer.MIN_VALUE;
        }
        for (int i = 0; i < entry_count - 1; i++) {
            int j = i * 3 + old_entry_space + 1;
            add(new_table, table[j + 1], table[j + 2]);
        }
        add(new_table, key, value);
        table = new_table;
    }

    private void add(int[] table, int key, int value) {
        int e = indexFor(key);
        while (table[e] != Integer.MIN_VALUE) {
            e = table[e];
        }
        table[e] = next;
        table[next + 1] = key;
        table[next + 2] = value;
        next += 3;
    }

    private boolean contains(int key, int new_value) {
        int e = indexFor(key);
        if (table[e] == Integer.MIN_VALUE) {
            return false; // not even a chain at this index so does not exist
        }
        e = table[e]; // move to first entry of the chain
        do {
            if (table[e + 1] == key) {
                table[e + 2] = new_value;
                return true;
            } else {
                e = table[e];
            }
        } while (e != Integer.MIN_VALUE);
        return false;
    }

    public int get(int key) {
        int e = indexFor(key);
        while (e != Integer.MIN_VALUE) {
            if (table[e + 1] == key) {
                return table[e + 2];
            }
            e = table[e];
        }
        return -1;
    }

    public ArrayList<Integer> toArrayList() {
        ArrayList<Integer> r = new ArrayList<Integer>();
        for (int i = 0; i < table.length; i++) {
            r.add(table[i]);
        }
        return r;
    }

    public void output() {
        for (int i = 0; i < table.length; i++) {
            System.out.println(table[i] + " ");
        }
        for (int i = 0; i < entry_count; i++) {
            int j = i * 3 + entry_space + 1;
            System.out.println(i + ": " + table[j + 1] + " = " + table[j + 2]);
        }
        System.out.println("$$$$$$$$");
    }
}
