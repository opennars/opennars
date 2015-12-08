package nars.nal.nal6;

import nars.Narsese;
import nars.util.meter.ExhaustPremises;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by me on 10/4/15.
 */
@Ignore
public class PremiseExhaustion {


    /** (template) */
    final void template_ex() {

        ExhaustPremises.tryPremise(
            "task. %...",
            "belief. %..."
            //|-
        ).mustBelieve(1 /*cycles*/,
            "result.", 0.79f /* f */, 0.92f /* c */);

    }

    @Test
    public void variable_unification1_ex() {

        ExhaustPremises.tryPremise(
            "<<$x --> a> ==> <$x --> b>>.",
            "<<$y --> a> ==> <$y --> b>>. %0.00;0.70%"
            //|-
        ).mustBelieve(1 /*cycles*/,
            "<<$1 --> a> ==> <$1 --> b>>", 0.79f, 0.92f);

    }

    @Test
    public void second_level_variable_unification_ex() throws Narsese.NarseseException {

        ExhaustPremises.tryPremise(
                "(&&,<#1 --> lock>,<<$2 --> key> ==> <#1 --> (/,open,$2,_)>>). %1.00;0.90%",
                "<{key1} --> key>. %1.00;0.90%"
                //|-
        ).mustBelieve(150 /*cycles*/,
                "(&&,<#1 --> lock>,<#1 --> (/,open,{key1},_)>).",
                1.00f /* f */, 0.81f /* c */);


    }

}
