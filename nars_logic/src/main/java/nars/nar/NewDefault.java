package nars.nar;

import nars.NAR;
import nars.Param;
import nars.nal.*;
import nars.nal.nal8.OpReaction;
import nars.op.app.STMEventInference;
import nars.op.mental.Abbreviation;
import nars.op.mental.Counting;
import nars.op.mental.FullInternalExperience;
import nars.op.mental.InternalExperience;
import nars.process.concept.FilterEqualSubtermsAndSetPremiseBelief;
import org.infinispan.commons.marshall.jboss.GenericJBossMarshaller;
import spangraph.TemporaryCache;

import java.io.IOException;
import java.net.URISyntaxException;

import static nars.op.mental.InternalExperience.InternalExperienceMode.Full;
import static nars.op.mental.InternalExperience.InternalExperienceMode.Minimal;

//LOCKED CODE!!!, ASK PATRICK FOR CHANGE, I WILL REVERT ALL CHANGES NO MATTER WHAT IF I AM NOT CONVINCED THAT
//THE CHANGE WILL BREAK NOTHING

/**
 * Temporary class which uses the new rule engine for ruletables
 * <p>
 * WARNING this Seed is not immutable yet because it extends Param,
 * which is supposed to be per-instance/mutable. So do not attempt
 * to create multiple NAR with the same Default seed model *
 */
public class NewDefault extends Default {

    public NewDefault() {
        this(1024, 1, 3);
    }

    public NewDefault(int maxConcepts, int conceptsFirePerCycle, int termLinksPerCycle) {
        super(maxConcepts, conceptsFirePerCycle, termLinksPerCycle);
    }

    @Override
    public Param getParam() {
        Param p = super.getParam();
        //deprecated: all reasoning components should be added to the DI index automatically
        the(Deriver.class, der);
        return p;
    }


    /**
     * default set of rules, statically available
     */
    public static DerivationRules standard;

    public static final String key = "derivation_rules:standard";

    static void loadCachedRules() {
        standard = TemporaryCache.computeIfAbsent(
                key, new GenericJBossMarshaller(),
                () -> {
                    try {
//                        standard = new DerivationRules();

                        return new DerivationRules();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                        return null;
                    }
                }
//                //TODO compare hash/checksum of the input file
//                //to what is stored in cached file
//                (x) -> {
//                    //this disables entirely and just creates a new one each time:
//                    return  ...
//                }
        );
    }

    static void loadRules() {
        try {
            standard = new DerivationRules();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    static {
        loadRules();
    }

//    static {
//        try {
//            Cache<Object, Object> cache = (InfiniPeer.tmp().getCache());
//
//            cache.entrySet().forEach(c -> {
//                System.out.println(c);
//            });
//
//            standard = (DerivationRules) cache.get(key);
//
//            if (standard == null || !standard.isValid()) {
//                try {
//                    System.out.print("Recompiling derivation rules..");
//
//                    standard = new DerivationRules();
//
////                    //GenericJBossMarshaller
////
//                    ByteBuffer b = new JavaSerializationMarshaller().objectToBuffer(standard.get(0));
//                    System.out.println(b.getLength());
//                    System.out.println(b);
//
//                    cache.putForExternalRead(key, standard);
//                    System.out.println("saved.");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    System.exit(1);
//                }
//
//
//            }
//
//
//            //standard = new DerivationRules();
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//    }

    public static final Deriver der;

    static {
        Deriver r;

        try {
            r = new SimpleDeriver(standard);
        } catch (Exception e) {
            r = null;
            e.printStackTrace();
            System.exit(1);
        }

        der = r;
    }

    @Override
    public void init(NAR n) {

        n.setCyclesPerFrame(cyclesPerFrame);


        if (maxNALLevel >= 7) {
            n.on(STMEventInference.class);


            if (maxNALLevel >= 8) {

                for (OpReaction o : defaultOperators)
                    n.on(o);
                for (OpReaction o : exampleOperators)
                    n.on(o);


                //n.on(Anticipate.class);      // expect an event

                if (internalExperience == Minimal) {
                    n.on(InternalExperience.class, Abbreviation.class);
                } else if (internalExperience == Full) {
                    n.on(FullInternalExperience.class);
                    n.on(Counting.class);
                }
            }
        }

        //n.on(new RuntimeNARSettings());

    }


    @Override
    public PremiseProcessor getPremiseProcessor(Param p) {

        return new PremiseProcessor(

                new LogicStage[]{
                        new FilterEqualSubtermsAndSetPremiseBelief(),
                        //new QueryVariableExhaustiveResults(),
                        der
                        //---------------------------------------------
                },

                getDerivationFilters()

        );
    }


}
