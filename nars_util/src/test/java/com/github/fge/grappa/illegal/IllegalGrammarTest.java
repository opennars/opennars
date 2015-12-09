package com.github.fge.grappa.illegal;

import com.github.fge.grappa.Grappa;
import com.github.fge.grappa.exceptions.InvalidGrammarException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

import static com.github.fge.grappa.util.CustomAssertions.shouldHaveThrown;
import static org.assertj.core.api.Assertions.assertThat;

@ParametersAreNonnullByDefault
@Test
public abstract class IllegalGrammarTest
{
    private final Class<? extends IllegalGrammarParser> parserClass;
    private final String errorMessage;

    private IllegalGrammarParser parser;

    protected IllegalGrammarTest(
        Class<? extends IllegalGrammarParser> parserClass,
        String errorMessage)
    {
        this.parserClass = Objects.requireNonNull(parserClass);
        this.errorMessage = Objects.requireNonNull(errorMessage);
    }

    @BeforeMethod
    public void init()
    {
        parser = Grappa.createParser(parserClass);
    }
    @Test
    public final void illegalGrammarIsDetected()
    {
        try {
            parser.illegal();
            shouldHaveThrown(InvalidGrammarException.class);
        } catch (InvalidGrammarException e) {
            assertThat(e).isExactlyInstanceOf(InvalidGrammarException.class)
                .hasMessage(errorMessage);
        }
    }

    @Test
    public final void legalGrammarDoesNotThrowAnException()
    {
        parser.legal();
    }
}
