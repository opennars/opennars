/*
package ca.nengo.test.lemon;

import com.github.parboiled1.grappa.backport.EventBasedParseRunner;
import com.github.parboiled1.grappa.backport.ParseRunnerListener;
import com.github.parboiled1.grappa.backport.events.PreMatchEvent;
import com.github.parboiled1.grappa.backport.tracer.TracingListener;
import org.parboiled.Node;
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.ParsingResult;
import static org.parboiled.common.Predicates.*;
import static org.parboiled.support.Filters.*;

import java.io.IOException;
import java.nio.file.Paths;


public class TracingLang extends Lang {
    @Override
    public Match text2match(String text)
    {

        EventBasedParseRunner runner = new EventBasedParseRunner<Object>(p.Input());
        TracingParseRunner tpr = new TracingParseRunner(p.Input());
        //tpr.withFilter(and(rulesBelow(p.Input()), )



        ParseRunnerListener listener = new ParseRunnerListener<Object>()
        {
            @Override
            public void beforeMatch(final PreMatchEvent<Object> event)
            {
                System.out.println(event);
            }
        };

        runner.registerListener(listener);

        //also:
        try {
            runner.registerListener(new TracingListener<Object>(Paths.get("/tmp/narsrstsrgwfgstrssrt65tsr6ts56.zip"), true));
        }
        catch (IOException e)
        {}

        ParsingResult r = runner.run(text);

        p.printDebugResultInfo(r);

        Node root = r.getParseTree();
        Match w = new ListMatch((Node)root.getChildren().get(1));
        return w;
    }
    public static void main(String[] args) {
        String input;
        //input = "<<(*,$a,$b,$c) --> Nadd> ==> <(*,$c,$a) --> NbiggerOrEqual>>.";
        //input = "<{light} --> [on]>.";
        //input = "(--,<goal --> reached>).";
        //input = "<(*,{tom},{sky}) --> likes>.";
        input = "<neutralization --> reaction>. <neutralization --> reaction>?";

        Lang l = new TracingLang();
        l.text2match(input);
    }


}
*/
