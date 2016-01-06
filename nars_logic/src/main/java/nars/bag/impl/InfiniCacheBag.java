//package nars.bag.impl;
//
//import com.google.common.collect.Iterators;
//import nars.Global;
//import nars.Memory;
//import nars.concept.Concept;
//import nars.concept.util.ConceptBuilder;
//import nars.concept.util.ConceptExternalizer;
//import nars.term.Term;
//import org.infinispan.Cache;
//
//import java.io.*;
//import java.util.Iterator;
//import java.util.Set;
//
//
///**
// * CacheBag backed by Infinispan, supporting distributed memory sharing
// */
////public class InfiniCacheBag<K, V extends Itemized<K>> extends MapCacheBag<K, V, Cache<K,V>> {
//public class InfiniCacheBag extends AbstractCacheBag<Term, Concept> {
//
//    private transient final Cache<Term, byte[]> cache;
//
//  //  private boolean asyncPut = false;
//
//
////    public static <K, V extends Itemized<K>> InfiniCacheBag<K,V> make(InfiniPeer p) {
////
////    }
//
////    public static InfiniCacheBag local(String userID, String channel) {
////        return new InfiniCacheBag(InfiniPeer.clusterLocal(userID).the(channel));
////    }
////    public static InfiniCacheBag file(String channel, String diskPath, int maxEntries) {
////        return new InfiniCacheBag(InfiniPeer.file(diskPath, maxEntries).the(channel));
////    }
////
//
//    private transient final Set<Concept> pendingWrite = Global.newHashSet(1);
//    private transient ConceptBuilder builder;
//    private transient ConceptExternalizer externalizer;
//
////    @Listener(observation = Listener.Observation.PRE)
////    class InfiniBagListener {
////
////        @CacheStopped
////        public void cacheStopped(CacheStoppedEvent e) {
////            System.out.println("stopping: " + e);
////            flush();
////        }
////    }
//
//    protected void flush() {
//        pendingWrite.forEach(this::write);
//        pendingWrite.clear();
//    }
//
//    @Override
//    public void start(Memory memory) {
//        super.start(memory);
//
//        this.builder = memory.the(ConceptBuilder.class);
//        this.externalizer = new ConceptExternalizer(builder);
//
//        memory.eventConceptProcess.on(cp -> {
//            pendingWrite.add(cp.getConcept());
//        });
//        //TODO is this necessary? can we avoid this with a more specific ConceptChanged event
//        memory.eventConceptActivated.on(c -> {
//            pendingWrite.add(c);
//        });
//        memory.eventFrameStart.on(c -> {
//            flush();
//        });
//    }
//
//
//    public Iterator<Concept> iterator() {
//        return Iterators.transform(cache.values().iterator(),
//                (v) -> {
//                    try {
//                        //TODO this is slow unless repeated
//                        return read(v);
//                    } catch (Exception e) {
//                        System.err.println(e);
//                        //e.printStackTrace();
//                    }
//                    return null;
//                }
//        );
//    }
//
//
//    public InfiniCacheBag( Cache<Term,byte[]> c) {
//        super();
//
//        this.cache = c;
//
//
//        System.out.println(this + " " +
//                c.size() + " " + c.getAdvancedCache().getDataContainer() + " " + c.getAdvancedCache().getDistributionManager());
//                //c.getAdvancedCache().getStats().getTotalNumberOfEntries() + " entries");
//
//        //c.getAdvancedCache().getCacheManager().addListener(new InfiniBagListener());
//    }
//
//
//    @Override
//    public Concept put(Concept concept) {
//        pendingWrite.add(concept);
//        return concept;
//    }
//
//    public Concept write(Concept v) {
////        if (asyncPut) {
////            data.putAsync(v.name(), v);
////
////            //TODO compute(..) with item merging
////
////            return null;
////        }
////        else {
//            try {
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                ObjectOutputStream o = new ObjectOutputStream(baos);
//                externalizer.writeObject(o, v);
//                //o.flush();
//                byte[] by = baos.toByteArray();
//                cache.put(v.name(), by);
//            }
//            catch (Exception e) {
//                System.err.println(e + " while trying to write " + v);
//                return null;
//            }
////        }
//        return v;
//    }
//
//    @Override
//    public int size() {
//        return cache.size();
//    }
//
//
//    @Override
//    public String toString() {
//        return getClass().getSimpleName() + ":" + cache.toString();
//    }
//
//    @Override
//    public void clear() {
//        //throw new RuntimeException("unable to clear() shared concept bag");
//    }
//
//    @Override
//    public Concept get(Term key) {
//
//        byte[] by = cache.get(key);
//        if (by == null) return null;
//
//        try {
//            Concept bc = read(by);
//            return bc;
//        } catch (Exception e) {
//            System.err.println(e);
//        }
//
//        return null;
//    }
//
//    public Concept read(byte[] by) throws IOException, ClassNotFoundException {
//        return externalizer.readObject(new ObjectInputStream(new ByteArrayInputStream(by)));
//    }
//
//    @Override
//    public Concept remove(Term key) {
//        byte[] b = cache.remove(key);
//        if (b != null) {
//            try {
//                return read(b);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
//
//
// }
