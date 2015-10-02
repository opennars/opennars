package nars.term.transform;

import com.gs.collections.api.block.function.primitive.BooleanFunction0;
import com.gs.collections.api.tuple.Twin;
import com.gs.collections.impl.map.mutable.primitive.ObjectBooleanHashMap;
import com.gs.collections.impl.tuple.Tuples;
import nars.Op;
import nars.term.Term;

import java.util.Map;
import java.util.Random;

/**
 * TODO not working yet, experimental
 * Caches input combinations
 */
class FindSubstCached extends FindSubst {


    ObjectBooleanHashMap<Twin<Term>> cache = new ObjectBooleanHashMap<>();
    private Term a = null;
    private Term b = null;
    private Twin<Term> ab;

    public FindSubstCached(Op type, Random random) {
        super(type, random);
    }

    public FindSubstCached(Op type, Map<Term, Term> map1, Map<Term, Term> map2, Random random) {
        super(type, map1, map2, random);
    }


    @Override
    public final void clear() {
        if (this._accesses == 0) {
            return;
        }

        super.clear();
        cache.clear();

        this._accesses = 0;
    }

    /** needs to be called if the terms change.. TODO make an API which prevents this by only allowing a set(x,y) and a next() method */
    public void reset() {
        clear();
        //cache.clear();
    }

    private long _accesses = 0;

    private void report() {
        if (new Random().nextInt() % 2 == 0) {
            System.out.println(this + " " + cache.size() + " from " + _accesses + " accesses");
//            Procedure<? super ObjectBooleanPair<Twin<Term>>> c = (p) -> {
//                System.out.println("  " + p);
//            };

        }

    }

    @Override
    public boolean next(final Term a, final Term b, final int power) {

        if ((a != a) || (b != b))
            this.ab = null;

        this.a = a;
        this.b = b;


        //PERF ANALYZE: accesses vs. size
        {
            _accesses++;
        }


        // hypothesis:
        //  if a and b have > 1 variable and a commutative term, there
        //  is indeterminacy and a hashtable will not work like this
        //
        //  another idea is to use a bloom filter or something
        //  more conventional to probabalistically learn a 1st-level of
        //  reactivty

        final BooleanFunction0 c = () -> {
            return super.next(a, b, power);
        };


        if (!cacheable(a, b)) {
            return c.value();
        }
        else {
            Twin<Term> p = this.ab;
            if (p == null)
                this.ab = p = Tuples.twin(a, b);

//        //FOR TESTING
//        boolean expect = super.next(p.getOne(), p.getTwo(), power);

        boolean calculated = cache.getIfAbsentPut(ab, c);


//        //FOR TESTING
//        if (calculated!=expect) {
//            //throw new RuntimeException
//            System.out.println("conflict: " +
//                    a + ":" + b + " : " + p + " (" + p.hashCode() + ") : " + this            );
//        }
            return calculated;
        }


    }

    public static boolean cacheable(Term a, Term b) {
        if (((a.vars() + b.vars()) > 0)) return false;
        //if ((a.vars() > 1) || (b.vars() > 1)) return false;
        return true;
    }
}
