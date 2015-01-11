package nars.johkra;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import nars.johkra.Prolog;

import static org.junit.Assert.assertEquals;

/**
 * User: Johannes Krampf <johkra@gmail.com>
 * Date: 27.02.11
 */
public class QueryTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUp() throws Exception {
        System.setOut(new PrintStream(outContent));
    }

    private void verifyOutput(String commands, String exceptedOutput) throws Exception {
        byte[] bytes = commands.getBytes("utf-8");
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        Prolog.procFile(input, "");
        assertEquals(exceptedOutput, outContent.toString());
    }

    @Test
    public void testSimpleQuery() throws Exception {
        verifyOutput("boy(bill)\n" +
                "boy(bill)?",
                "Yes\n");
    }

    @Test
    public void testVariableQuery() throws Exception {
        verifyOutput("boy(X)?", "{X=bill}\n");
    }

    @Test
    public void testIndirectQuery() throws Exception {
        verifyOutput("mother(alice,bill)\n" +
                "child(J,K) :- mother(K,J)\n" +
                "son(X,Y) :- child(X,Y),boy(X)\n" +
                "son(X,alice)?", "{X=bill}\n");
    }

    @Test
    public void testMember() throws Exception {
        verifyOutput("member(X,[X|T])\n" +
                "member(X,[H|T]) :- member(X,T)\n" +
                "member(a,[a,b,c])?\n" +
                "member(b,[a,b,c])?\n" +
                "member(c,[a,b,c])?", "Yes\nYes\nYes\n");
    }

    @Test
    public void testAppend() throws Exception {
        verifyOutput("append([],L,L)\n" +
                "append([X|A],B,[X|C]) :- append(A,B,C)\n" +
                "append([a,b],[c,d],X)?", "{X=[a,b,c,d]}\n");
    }

    @Test
    public void testLength() throws Exception {
        verifyOutput("length([],0)\n" +
                "length([H|T],N) :- length(T,Nt), N is Nt+1\n" +
                "length([],X)?\n" +
                "length([1],X)?\n" +
                "length([1,2,3],X)?", "{X=0}\n{X=1}\n{X=3}\n");
    }

    @Test
    public void testWithoutCut() throws Exception {
        verifyOutput("childOf(X,Y) :- parent(Y,X)\n" +
                "parent(chris,jon)\n" +
                "parent(maryann,jon)\n" +
                "childOf(A,B)?","{A=jon, B=chris}\n" +
                "{A=jon, B=maryann}\n");
    }

    @Test
    public void testFail() throws Exception {
        verifyOutput("childOfCut(X,Y) :- parent(Y,X),cut\n" +
                "childOfCut(A,B)?",
                "{A=jon, B=chris}\n");
    }

    @After
    public void tearDown() throws Exception {
        System.setOut(null);
    }
}
