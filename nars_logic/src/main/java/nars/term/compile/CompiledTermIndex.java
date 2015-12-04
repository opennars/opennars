package nars.term.compile;

import nars.term.Atom;
import nars.term.Atomic;
import nars.term.Term;
import nars.term.Termed;
import nars.term.transform.CompoundTransform;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.StubMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static net.bytebuddy.implementation.FixedValue.value;
import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * Created by me on 11/13/15.
 */
public class CompiledTermIndex extends ByteBuddy implements TermIndex {

    public final Map<Term, Term> terms = new HashMap();

    @Override
    public Termed get(Term t) {
        return terms.computeIfAbsent(t, this::compile);
    }

    public Term compile(Term t) {
        if (t instanceof Atom) {
            //TODO make the type final
            DynamicType.Unloaded<Atomic> uc = subclass(Atomic.class)
                    .method(named("bytes")).intercept(value(t.bytes()))
                    .method(named("getByteLen")).intercept(value(t.getByteLen()))
                    .method(named("hashCode")).intercept(value(t.hashCode()))
                    .method(named("setBytes")).intercept(StubMethod.INSTANCE)
                    .method(named("op")).intercept(value(t.op()))
                    .method(named("structure")).intercept(value(t.structure()))
                    //.method(named("toString")).intercept(SuperMethodCall.INSTANCE)
                    .make();


            /*try {
                uc.saveIn(new File("/tmp/a.class"));
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            System.out.println(uc);
            DynamicType.Loaded<Atomic> ux = uc.load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER);
            System.out.println(ux + " " + ux.getBytes().length + " bytes");
            Class c = ux.getLoaded();
            System.out.println(c);
            Term v;
            try {
                v = (Term) c.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                return t;
            }
            return v;

        }
        return t;
    }

    @Override
    public final void forEachTerm(Consumer<Termed> c) {
        terms.forEach((k, v) -> c.accept(v));
    }

    @Override
    public CompoundTransform getCompoundTransformer() {
        return null;
    }

    public static void main(String[] args) {
        CompiledTermIndex i = new CompiledTermIndex() {
            @Override
            public Termed get(Term t) {
                Termed u = super.get(t);
                System.out.println(t);
                System.out.println(u);
                System.out.println(u.hashCode());
                System.out.println(u.equals(t));
                return u;
            }
        };
        i.get(Atom.the("xyzxyzxyz"));
    }
}
