//package nars.index;
//
//import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;
//import nars.Global;
//import nars.term.Term;
//import nars.term.TermContainer;
//import nars.term.Termed;
//import nars.term.Termlike;
//import nars.term.compound.Compound;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Map;
//import java.util.function.Consumer;
//
///**
// * Created by me on 12/7/15.
// */
//public class MapIndex2 extends AbstractMapIndex {
//
//    static final int initialGroupSize = 2;
//    public final Map<Termlike, IntObjectHashMap<Termed>> data;
//
//
//    public MapIndex2() {
//        this(Global.newHashMap());
//    }
//
//    public MapIndex2(Map<TermContainer, IntObjectHashMap<Termed>> data) {
//        super();
//        this.data = data;
//    }
//
//
//    @Override @NotNull
//    public Termed getIfAbsentIntern(Term x) {
//        Termed y = getTermIfPresent(x);
//        if (y == null) {
//            y = intern(x);
//            putTerm(y);
//        }
//        return y;
//    }
//
//    IntObjectHashMap<Termed> getGroup(Term tt) {
//        Termlike k;
//        if (tt instanceof Compound) {
//            k = ((Compound) tt).subterms();
//        } else {
//            k = tt;
//        }
//
//        return data.get(k);
//    }
//    IntObjectHashMap<Termed> getGroupIfAbsentCreate(Termed tt) {
//        IntObjectHashMap<Termed> z = getGroup(tt);
//        if (z == null) {
//            data.put(tt.term(), z = new IntObjectHashMap<>(initialGroupSize));
//        }
//        z.put(tt.opRel(), tt);
//        return z;
//    }
//
//    @Override
//    public final Termed getTermIfPresent(Termed t) {
//        Term tt = t.term();
//        IntObjectHashMap<Termed> z = getGroup(tt);
//        if (z!=null) return z.get(tt.opRel());
//        return null;
//    }
//
//    @Override
//    public void clear() {
//        data.clear();
//    }
//
//    @Override
//    public int subtermsCount() {
//        return data.size();
//    }
//
//    @Override
//    public Object remove(Term key) {
//        return data.remove(key);
//    }
//
//    @Override
//    public void putTerm(Termed termed) {
//        data.put(termed.term(), termed);
//    }
//
//    @Override
//    public int size() {
//        return data.size();
//    }
//
//
//    @Override public TermContainer getIfAbsentIntern(TermContainer s) {
//        TermContainer existing = getSubtermsIfPresent(s);
//        if (existing == null) {
//            s = internSubterms(s.terms());
//            putSubterms(s);
//            return s;
//        }
//        return existing;
//    }
//
//    protected void putSubterms(TermContainer subterms) {
//        this.subterms.put(subterms,subterms);
//    }
//
//    protected TermContainer getSubtermsIfPresent(TermContainer subterms) {
//        return this.subterms.get(subterms);
//    }
//
//
//    @Override
//    public final void forEach(Consumer<? super Termed> c) {
//        data.forEach((k, v) -> c.accept(v));
//    }
//}
