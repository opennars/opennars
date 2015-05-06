package example;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import objenome.goal.Between;

public class OptimizeRuntimeSpeedExample {

    final static int NumItems = 1000;
    final static int NumIterations = 10;
    
    public static class MapActivity {
        public final Mapper builder;
        
        public MapActivity(Mapper b) {
            this.builder = b;
        }
        public void act() {
            Map l = builder.map();
            for (int i = 0; i < NumItems; i++) {
                l.put(i, Math.random());
            }
            for (int i = 0; i < NumIterations; i++) {
                for (Object o  : l.keySet()) {
                    //should never happen, just simple comparison with no side-effect
                    if (o == null) break; 
                }                    
            }
            
        }

        @Override
        public String toString() {
            return builder.toString();
        }
        
    }
    
    public static interface Mapper {
        public Map map();
    }
    public static class TreeMapper implements Mapper {
        public Map map() {
            return new TreeMap();
        }

        @Override public String toString() { return getClass().toString(); }        
    }
    
    public static class HashMapper implements Mapper{
        final int initialSize;
        
        public HashMapper(@Between(min=0, max=1.0) double initialSize) {
            this.initialSize = (int)(initialSize * NumItems);
        }
        
        public Map map() {
            return new HashMap(initialSize);
        }        
        
        @Override public String toString() { return getClass() + "(" + initialSize + ")"; }
    }

    public static class ConcurrentHashMapper extends HashMapper {

        public ConcurrentHashMapper(int initialSize) {
            super(initialSize);
        }
        
        
        public Map map() {
            return new ConcurrentHashMap(initialSize);
        }        
    }


    //TODO update to the new API
    
//    public static void main(String[] args) {
//        Genetainer g = new Genetainer();
//        g.any(Mapper.class, of(TreeMapper.class, HashMapper.class, ConcurrentHashMapper.class));
//                
//        Objenome o = new OptimizeMultivariate(g, MapActivity.class, new Function<MapActivity, Double>() {
//            public Double apply(MapActivity s) {
//                  
//                Microbenchmark w = new Microbenchmark(100, 10) {
//                    
//                    @Override public void init() {                     
//                    }
//                    
//                    
//                    @Override
//                    public void run(boolean warmup) {
//                        s.act();
//                    }                    
//                };
//                double t = w.getTotalTime()/1e6;
//                double m = w.getTotalMemory()/1e6;
//                System.out.println(s + " " + t + " " + m);
//                return t + m/10f;
//            }
//        }).minimize().run();
//        
//        
//        System.out.println("best: " + o.getGeneList());
//        
//    }
    
}
