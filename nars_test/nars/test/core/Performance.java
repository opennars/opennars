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

package nars.test.core;

/**
 *
 * @author me
 */
public abstract class Performance {
    public final int repeats;
    private final String name;
    private long totalTime;
    private long totalMemory;

    public Performance(String name, int repeats, int warmups) {
        this.repeats = repeats;        
        this.name = name;
        
        init();
        
        totalTime = 0;
        totalMemory = 0;

        for (int r = 0; r < repeats+warmups; r++) {

            System.gc();

            long start = System.nanoTime();
            long freeMemStart = Runtime.getRuntime().freeMemory();
            
            run(warmups != 0);

            if (warmups == 0) {
                totalTime += System.nanoTime() - start;
                totalMemory += freeMemStart - Runtime.getRuntime().freeMemory();
            }
            else
                warmups--;
        }
    }    
 
    public Performance print() {
        System.out.print(name + ": " + ((double)totalTime)/((double)repeats)/1000000.0 + "ms per iteration, ");
        System.out.println(((double)totalMemory)/((double)repeats)/1024.0 + " kb per iteration");
        return this;
    }
    public Performance printCSV() {
        System.out.print(name + "," + ((double)totalTime)/((double)repeats)/1000000.0 +",");
        System.out.print(((double)totalMemory)/((double)repeats)/1024.0+",");
        return this;
    }    
            
    abstract public void init();
    abstract public void run(boolean warmup);
    
    
}
