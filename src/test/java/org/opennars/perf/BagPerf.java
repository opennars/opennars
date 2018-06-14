/**
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
package org.opennars.perf;

import com.google.common.collect.Lists;
import org.opennars.entity.BudgetValue;
import org.opennars.entity.Item;
import org.opennars.main.Nar;
import org.opennars.main.Nar.PortableDouble;
import org.opennars.storage.Bag;
import org.opennars.storage.LevelBag;
import org.opennars.storage.Memory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opennars.main.Parameters;

/**
 *
 * @author me
 */
public class BagPerf {
    
    private static Parameters narParameters;
    final int repeats = 8;
    final int warmups = 1;
    static PortableDouble forgetRate;

    static {
        try {
            forgetRate = (new Nar().param).conceptForgetDurations;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    int randomAccesses;
    final double insertRatio = 0.9;
    

    
    public float totalPriority, totalMass, totalMinItemsPerLevel, totalMaxItemsPerLevel;

    public void testBag(final boolean List, final int levels, final int capacity, final PortableDouble forgetRate) {
        
        totalPriority = 0;
        totalMass = 0;
        totalMaxItemsPerLevel = totalMinItemsPerLevel = 0;
        
        final Performance p = new Performance((List ? "DequeArray" : "LinkedList")+","+levels+","+ capacity, repeats, warmups) {

            @Override public void init() { }

            @Override
            public void run(final boolean warmup) {
                Nar nar = null;
                try {
                    nar = new Nar();
                } catch (IOException ex) {
                    Logger.getLogger(BagPerf.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(BagPerf.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(BagPerf.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(BagPerf.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(BagPerf.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(BagPerf.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    Logger.getLogger(BagPerf.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(BagPerf.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    Logger.getLogger(BagPerf.class.getName()).log(Level.SEVERE, null, ex);
                }
                final LevelBag<NullItem,CharSequence> b = new LevelBag(levels, capacity, nar.narParameters) {

//                    @Override
//                    protected ArrayDeque<NullItem> newLevel() {
//                        //if (List)
//                            return super.newLevel();
//                        //return new LinkedList<>();
//                    }
                    
                };
                randomBagIO(b, randomAccesses, insertRatio);
                
                if (!warmup) {                    
                    totalPriority += b.getAveragePriority();
                    totalMass += b.getMass();                    
                    totalMinItemsPerLevel += b.getMinItemsPerLevel();
                    totalMaxItemsPerLevel += b.getMaxItemsPerLevel();
                }
            }
            
        }.printCSV(true);
        
        
        //items per level min
        //items per lvel max
        //avg prioirty
        //avg norm mass
        //System.out.print((totalMinItemsPerLevel/p.repeats) + ",");
        System.out.print((totalMaxItemsPerLevel/p.repeats) + ",");
        System.out.print(totalPriority/p.repeats + ",");
        System.out.print(totalMass/repeats/levels + ",");
        System.out.println();
    }
            
    public static int itemID = 0;
    
    /** Empty Item implementation useful for testing */
    public static class NullItem extends Item.StringKeyItem {
        public final String key;
    
        public NullItem() {
            this(Memory.randomNumber.nextFloat() * (1.0f - narParameters.TRUTH_EPSILON));
        }

        public NullItem(final float priority) {
            super(new BudgetValue(priority, priority, priority, narParameters));
            this.key = "" + (itemID++);
        }

        @Override
        public CharSequence name() {
            return key;
        }
        
    }
    
    public static void randomBagIO(final Bag<NullItem,CharSequence> b, final int accesses, final double insertProportion) {
        for (int i = 0; i < accesses; i++) {
            if (Memory.randomNumber.nextFloat() > insertProportion) {
                //remove
                b.takeNext();
            }
            else {
                //insert
                b.putIn(new NullItem());
            }            
        }
    }
    public static void iterate(final Bag<NullItem,CharSequence> b) {
        final Iterator<NullItem> i = b.iterator();
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
        Bag<E,K> newBag();
    }
    
    //final boolean first, final int levels, final int levelCapacity, 
    public static double getTime(final String label, final BagBuilder b, final int iterations, final int randomAccesses, final float insertRatio, final int repeats, final int warmups) {
        
        Memory.resetStatic();
        
        final Performance p = new Performance(label, repeats, warmups) {

            @Override public void init() { }

            @Override
            public void run(final boolean warmup) {
                
                final Bag bag = b.newBag();
                
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
    
    public static Map<Bag,Double> compare(final int iterations, final int randomAccesses, final float insertRatio, final int repeats, final int warmups, final Bag... B) {
        
        final Map<Bag,Double> t = new LinkedHashMap();
        
        for (final Bag X : B) {
            X.clear();
            
            t.put(X, getTime(X.toString(), () -> X, iterations, randomAccesses, insertRatio, repeats, warmups));
            
        }
        return t;
        
    }
    
    public static void printCSVLine(final PrintStream out, final String... s) {
        printCSVLine(out, Lists.newArrayList(s));        
    }
    
    public static void printCSVLine(final PrintStream out, final List<String> o) {
        final StringJoiner line = new StringJoiner(", ", "", "");
        for (final String x : o)
            line.add(x);        
        out.println(line.toString());
    }
    
    public static void main(final String[] args) throws Exception {
        narParameters = new Nar().narParameters;
        final int itemsPerLevel = 10;
        final int repeats = 10;
        final int warmups = 1;

        final int iterationsPerItem = 0;
        final int accessesPerItem = 8;
        
        boolean printedHeader = false;
        
        for (float insertRatio = 0.1f; insertRatio <= 1.0f; insertRatio += 0.1f) {
            for (int levels = 1; levels <= 10; levels += 1) {
                
                final int items = levels*itemsPerLevel;
                final int iterations = iterationsPerItem * items;
                final int randomAccesses = accessesPerItem * items;
                        
                final Bag[] bags = new LevelBag[1];
                bags[0] = new LevelBag(levels, items, narParameters);                       
    
                
                final Map<Bag, Double> t = BagPerf.compare(
                    iterations, randomAccesses, insertRatio, repeats, warmups,
                    bags
                );
                
                /*System.out.print(Arrays.toString(new Object[] { "(x" + repeats + ")", "items", items, "inserts:removals", insertRatio, "accesses", randomAccesses, "nexts", iterations }));                
                System.out.print("  ");
                for (Map.Entry<Bag, Double> e : t.entrySet()) {
                    System.out.print(e.getKey() + "," + e.getValue() + ",  ");
                }*/
                
                if (!printedHeader) {
                    
                    final List<String> ls = Lists.newArrayList("items", "io_ratio", "accesses", "nexts");
                    for (final Map.Entry<Bag, Double> e : t.entrySet())
                        ls.add(e.getKey().toString());
                                        
                    printCSVLine(System.out, ls  );
                    printedHeader = true;
                }

                {
                    final List<String> ls = Lists.newArrayList(items+"", insertRatio+"", randomAccesses+"", iterations+"");
                    for (final Map.Entry<Bag, Double> e : t.entrySet())
                        ls.add(e.getValue().toString());
                                        
                    printCSVLine(System.out, ls  );
                }
                
                
            }
        }

        
    }
    
}
