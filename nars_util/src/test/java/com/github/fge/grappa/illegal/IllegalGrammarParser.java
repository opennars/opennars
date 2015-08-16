package com.github.fge.grappa.illegal;

import com.github.fge.grappa.parsers.ListeningParser;
import com.github.fge.grappa.rules.Rule;

public abstract class IllegalGrammarParser
    extends ListeningParser<Object>
{
    abstract Rule illegal();

    abstract Rule legal();

    Rule empty()
    {
        return EMPTY;
    }

    Rule nonEmpty()
    {
        return ch('x');
    }
}
