package ca.nengo.test;

import com.github.parboiled1.grappa.backport.EventBasedParseRunner;
import com.github.parboiled1.grappa.backport.ParseRunnerListener;
import com.github.parboiled1.grappa.backport.events.PreMatchEvent;
import org.parboiled.Node;
import org.parboiled.support.ParsingResult;


/**
 * Created by you on 25.3.15.
 */
public class TracingLang extends Lang {
    @Override
    public Match text2match(String text)
    {
        ParseRunnerListener listener = new ParseRunnerListener<Object>()
        {
            @Override
            public void beforeMatch(final PreMatchEvent<Object> event)
            {
                System.out.println(event);
            }
        };
        EventBasedParseRunner runner = new EventBasedParseRunner<Object>(p.Input());
        runner.registerListener(listener);
        ParsingResult r = runner.run(text);

        p.printDebugResultInfo(r);

        Node root = r.getParseTree();
        Match w = new ListMatch((Node)root.getChildren().get(1));
        return w;
    }

}
