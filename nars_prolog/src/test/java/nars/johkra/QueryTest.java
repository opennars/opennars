package nars.johkra;

import nars.WAMPrologTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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

    private void testJohkra(String commands, String expectedOutput) throws Exception {
        byte[] bytes = commands.getBytes("utf-8");
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        JoProlog.procFile(input, "");
        assertEquals(expectedOutput, outContent.toString());
    }

    private void testJohkra(String theory, String query, String expectedOutput) throws Exception {
        String o = theory + '\n' + query + "?\n";
        testJohkra(o, expectedOutput + '\n');

        if (!theory.endsWith(".")) theory = theory + ".";
        WAMPrologTest.test(theory, query, expectedOutput);
    }

    @Test
    public void testSimpleQuery() throws Exception {
        testJohkra("boy(bill)",
                "boy(bill)",
                "Yes");
    }

    @Test
    public void testVariableQuery() throws Exception {
        testJohkra("boy(X)?", "{X=bill}\n");
    }

    @Test
    public void testIndirectQuery() throws Exception {
        testJohkra("mother(alice,bill)\n" +
                "child(J,K) :- mother(K,J)\n" +
                "son(X,Y) :- child(X,Y),boy(X)\n" +
                "son(X,alice)?", "{X=bill}\n");
    }

    @Test
    public void testMember() throws Exception {
        testJohkra("member(X,[X|T])\n" +
                "member(X,[H|T]) :- member(X,T)\n" +
                "member(a,[a,b,c])?\n" +
                "member(b,[a,b,c])?\n" +
                "member(c,[a,b,c])?", "Yes\nYes\nYes\n");
    }

    @Test
    public void testAppend() throws Exception {
        testJohkra("append([],L,L)\n" +
                "append([X|A],B,[X|C]) :- append(A,B,C)\n" +
                "append([a,b],[c,d],X)?", "{X=[a,b,c,d]}\n");
    }

    @Test
    public void testLength() throws Exception {
        testJohkra("length([],0)\n" +
                "length([H|T],N) :- length(T,Nt), N is Nt+1\n" +
                "length([],X)?\n" +
                "length([1],X)?\n" +
                "length([1,2,3],X)?", "{X=0}\n{X=1}\n{X=3}\n");
    }

    @Test
    public void testWithoutCut() throws Exception {
        testJohkra("childOf(X,Y) :- parent(Y,X)\n" +
                "parent(chris,jon)\n" +
                "parent(maryann,jon)\n" +
                "childOf(A,B)?", "{A=jon, B=chris}\n" +
                "{A=jon, B=maryann}\n");
    }

    @Test
    public void testFail() throws Exception {
        testJohkra("childOfCut(X,Y) :- parent(Y,X),cut\n" +
                        "childOfCut(A,B)?",
                "{A=jon, B=chris}\n");
    }

    @After
    public void tearDown() throws Exception {
        System.setOut(null);
    }
}
