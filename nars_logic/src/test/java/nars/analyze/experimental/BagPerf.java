/*
 * Copyright (C) 2014 me
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nars.analyze.experimental;

import com.google.common.collect.Lists;
import nars.model.impl.Default;
import nars.NAR;
import nars.Global;
import nars.budget.Budget;
import nars.nal.Item;
import nars.bag.Bag;
import nars.bag.impl.CurveBag;
import nars.bag.impl.LevelBag;
import nars.util.sort.ArraySortedIndex;
import com.google.common.util.concurrent.AtomicDouble;
import nars.util.data.random.XORShiftRandom;

import java.io.PrintStream;
import java.util.*;

/**
 *
 * @author me
 */
public class BagPerf {
    
    int repeats = 8;
    int warmups = 1;
    final static AtomicDouble forgetRate = (new NAR(new Default()).param).conceptForgetDurations;
    int randomAccesses;
    double insertRatio = 0.9;
    final static Random rng = new XORShiftRandom();

    
    public float totalPriority, totalMass, totalMinItemsPerLevel, totalMaxItemsPerLevel;

    public void testBag(final boolean arraylist, final int levels, final int capacity, final AtomicDouble forgetRate) {
        
        totalPriority = 0;
        totalMass = 0;
        totalMaxItemsPerLevel = totalMinItemsPerLevel = 0;
        
        Performance p = new Performance((arraylist ? "DequeArray" : "LinkedList")+","+levels+","+ capacity, repeats, warmups) {

            @Override public void init() { }

            @Override
            public void run(boolean warmup) {
                LevelBag<NullItem,CharSequence> b = new LevelBag(levels, capacity) {

//                    @Override
//                    protected ArrayDeque<NullItem> newLevel() {
//                        //if (arraylist)                                                    
//                            return super.newLevel();
//                        //return new LinkedList<>();
//                    }
                    
                };
                randomBagIO(b, randomAccesses, insertRatio);
                
                if (!warmup) {                    
                    totalPriority += b.getPriorityMean();
                    totalMass += b.mass();
                    totalMinItemsPerLevel += b.getMinItemsPerLevel();
                    totalMaxItemsPerLevel += b.getMaxItemsPerLevel();
                }
            }
            
        }.printCSV(true);
        
        
        //items per level min
        //items per lvel max
        //avg prioirty
        //avg norm mass
        //System.out.printMeaning((totalMinItemsPerLevel/p.repeats) + ",");
        System.out.print((totalMaxItemsPerLevel/p.repeats) + ",");
        System.out.print(totalPriority/p.repeats + ",");
        System.out.print(totalMass/repeats/levels + ",");
        System.out.println();
    }
            
    public static int itemID = 0;
    
    /** Empty Item implementation useful for testing */
    public static class NullItem extends Item.StringKeyItem {
        public String key;
    
        public NullItem() {
            this(rng.nextFloat() * (1.0f - Global.TRUTH_EPSILON));
        }

        public NullItem(float priority, String key) {
            super(new Budget(priority, priority, priority));
            this.key = key;
        }

        public NullItem(float priority) {
            super(new Budget(priority, priority, priority));
            this.key = "" + (itemID++);
        }

        @Override
        public CharSequence name() {
            return key;
        }
        
    }
    
    public static void randomBagIO(Bag<CharSequence, NullItem> b, int accesses, double insertProportion) {
        for (int i = 0; i < accesses; i++) {
            if (rng.nextFloat() > insertProportion) {
                //remove
                b.pop();
            }
            else {
                //insert
                b.put(new NullItem());
            }            
        }
    }
    public static void iterate(Bag<CharSequence, NullItem> b) {
        Iterator<NullItem> i = b.iterator();
        int count = 0;
        while (i.hasNext()) {
            i.next();
            count++;
        }
        if (count != b.size()) {
            System.err.println("Error itrating " + b.getClass() + " " + b.size() + " != " + count);
        }
    }
    
    public interface BagBuilder<E extends Item<K>,K> {
        public Bag<K, E> newBag();
    }
    
    //final boolean first, final int levels, final int levelCapacity, 
    public static double getTime(String label, BagBuilder b, final int iterations, final int randomAccesses, final float insertRatio, int repeats, int warmups) {

        Performance p = new Performance(label, repeats, warmups) {

            @Override public void init() { }

            @Override
            public void run(boolean warmup) {                                
                
                Bag bag = b.newBag();
                
                randomBagIO(bag, randomAccesses, insertRatio);
                
                for (int i = 0; i < iterations; i++)
                    iterate(bag);

            }
            
        };//.printCSV(false);                
        //System.out.println();
        
        return p.getCycleTimeMS();
        
    }
    
    public BagPerf() {
        
        
        for (int capacity = 8; capacity < 40000; capacity*=capacity) {
            randomAccesses = capacity*64;
            for (int i = 5; i < 200; i+=5) {
                testBag(false, i, capacity, forgetRate);
                testBag(true, i, capacity, forgetRate);
            }
        }
        
    }
    
    public static Map<Bag,Double> compare(final int iterations, final int randomAccesses, final float insertRatio, int repeats, int warmups, final Bag... B) {
        
        Map<Bag,Double> t = new LinkedHashMap();
        
        for (Bag X : B) {
            X.clear();
            
            t.put(X, getTime(X.toString(), new BagBuilder() {
                @Override public Bag newBag() {  return X; }
            }, iterations, randomAccesses, insertRatio, repeats, warmups));
            
        }
        return t;
        
    }
    
    public static void printCSVLine(PrintStream out, String... s) {
        printCSVLine(out, Lists.newArrayList(s));        
    }
    
    public static void printCSVLine(PrintStream out, List<String> o) {
        StringJoiner line = new StringJoiner(", ", "", "");
        for (String x : o)
            line.add(x);        
        out.println(line.toString());
    }
    
    
    public static void main(String[] args) {
        
        int itemsPerLevel = 10;
        int repeats = 10;
        int warmups = 1;

        int iterationsPerItem = 0;
        int accessesPerItem = 8;
        
        CurveBag.FairPriorityProbabilityCurve curve = new CurveBag.FairPriorityProbabilityCurve();
        
        boolean printedHeader = false;
        
        for (float insertRatio = 0.1f; insertRatio <= 1.0f; insertRatio += 0.1f) {
            for (int levels = 1; levels <= 10; levels += 1) {
                
                final int items = levels*itemsPerLevel;
                final int iterations = iterationsPerItem * items;
                int randomAccesses = accessesPerItem * items;
                        
                Bag[] bags = new Bag[] { 

                    new CurveBag(rng, items, curve, true, new ArraySortedIndex(items)),
                    //new CurveBag(items, curve, true, new FractalSortedItemList<>()),                
                    new LevelBag(levels, items),
                        //new FairDelayBag(..., items)

                };
                
                Map<Bag, Double> t = BagPerf.compare(                    
                    iterations, randomAccesses, insertRatio, repeats, warmups,
                    bags
                );
                
                /*System.out.printMeaning(Arrays.toString(new Object[] { "(x" + repeats + ")", "items", items, "inserts:removals", insertRatio, "accesses", randomAccesses, "nexts", iterations }));
                System.out.printMeaning("  ");
                for (Map.Entry<Bag, Double> e : t.entrySet()) {
                    System.out.printMeaning(e.getKey() + "," + e.getValue() + ",  ");
                }*/
                
                if (!printedHeader) {
                    
                    List<String> ls = Lists.newArrayList("items", "io_ratio", "accesses", "nexts");
                    for (Map.Entry<Bag, Double> e : t.entrySet())
                        ls.add(e.getKey().toString());
                                        
                    printCSVLine(System.out, ls  );
                    printedHeader = true;
                }

                {
                    List<String> ls = Lists.newArrayList(items+"", insertRatio+"", randomAccesses+"", iterations+"");
                    for (Map.Entry<Bag, Double> e : t.entrySet())
                        ls.add(e.getValue().toString());
                                        
                    printCSVLine(System.out, ls  );
                }
                
                
            }
        }

        
    }
    
}
