package nars;


import nars.jwam.WAM;
import nars.jwam.WAMProlog;
import nars.jwam.WAMProlog.Answer;
import nars.jwam.WAMProlog.Query;
import nars.jwam.parser.ParseException;
import org.junit.Test;

import java.io.FileNotFoundException;

import static junit.framework.TestCase.*;

public class WAMPrologTest {

 
    @Test public void testQuery1() throws ParseException {
        
        WAMProlog p = WAMProlog.newSmall().setTheory("x(a,b).  y(C,D) :- x(D,C).");
        
        Query q = p.query("y(b,a)");
        Answer a = q.nextAnswer();        
        assertTrue(a.success);
        assertEquals("no query variables, so empty size", 0, a.size());        
        
        Query q2 = p.query("y(a,b)");
        assertFalse(q2.nextAnswer().success);

    }
    
    @Test public void testSyntaxError1() {
        try {
            WAMProlog p = WAMProlog.newSmall().setTheory("x(!!22.,sd3_a,b");
            assertTrue("did not catch the syntax error", false);
        }
        catch (ParseException e) {
            assertEquals("caught error", ParseException.class, e.getClass());
        }
    }
    
    @Test public void testQuery2() throws ParseException {           
        test("f(a). f(b). g(a). g(b). h(b). k(X) :- f(X), g(X), h(X).", 
                "k(Y).", 
                true, 
                "Y", "b");
    }
    /*@Test public void testQuery4() throws ParseException {
        
        test("A :- not(not(A)).", 
                "\\=(a,not(not(a)))", 
                true, 
                null, null);
    }*/
    @Test public void testQuery3() throws ParseException {           
        String t = "i(x,y). i(y,x).  s(A,B) :- i(A,B), i(B,A).";
        test(t, "s(x,y)", true, null, null);
        test(t, "s(y,x)", true, null, null);
        test(t, "s(x,w)", false, null, null);
    }

    public static void test(String theory, String query, String result) throws ParseException {
        WAMProlog p = WAMProlog.newSmall().setTheory(theory.trim());

        Query q = p.query(query.trim());

        Answer a = q.nextAnswer();

        assertTrue(a.success);

        if (!result.equals("Yes"))
            assertEquals(result, a.toJSON());

    }

    public static void test(String theory, String query, boolean expected, String variable, String value) throws ParseException {
        
        WAMProlog p = WAMProlog.newSmall().setTheory(theory);

        Query q = p.query(query);
                
        Answer a = q.nextAnswer();       
        
        if (variable == null) {
           
            System.err.println(a.toString());
            assertEquals( expected, a.success);
            return;
        }
        
        assertEquals("Answer[" + q.toString() + "|" + expected + "|{" + variable + "=" + value + "}]", a.toString());
        
        
    }
    

    
    
    @Deprecated void original_test_code() throws ParseException, FileNotFoundException {
        //WAM w = new WAM(5000, 40, 1000, 100, 50, 3);
        WAM w = new WAM(8192, 80, 2000, 200, 100, 6);
        w.getCompiler().compile_file("src/main/java/nlp-1.pl");
//		w.getCompiler().compile_file("test2.pl");
        w.getCompiler().commit();
        
        int[] q = w.getCompiler().compile_query("einstein(Houses, FishOwner)");
        //	int[] q = w.getCompiler().compile_query("asserta(p(bla,bla)),assert(p(blu,bla)),p(A,B)");
//		int[] q2 = w.getCompiler().compile_query("assert(p(b,c)),p(b,C),p(BLU,bla),p(blu,BLA)");
//		int[] q = w.getCompiler().compile_query("p(X)");
//		System.out.println(w.getSrcManager().toString(w.getStringContainer(),w.getNums(),w.regStart())); 
//		try {
//			System.in.read();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
        double amount = 1;
        long l = System.currentTimeMillis();
        for (int i = 0; i < amount; i++) {
            w.prepare_for_new_query();
            w.setQuery(q);
            w.execute();
            //w.prepare_for_new_query();
            //w.append_query_code(q2);
            //w.execute(); 
        }
        long d = System.currentTimeMillis() - l;
        System.out.println(((d) / amount) + " average " + d + "ms total");
        //System.out.println(WAMProlog.queryResult(w));
//		System.out.println(WAMToString.oneLineHeap(w.getStorage(), 0, w.getH(), w.getStringContainer(), w.getNums()));
//		ArrayList<Object> conv = DataConverter.predicateToArrayList(w, w.getStorage(), w.deref(w.getA1()));
//		for(Object o : conv){
//			System.out.println(o);
//		}
//		System.out.println(DataConverter.javaConversionToString(conv));
//		//List<Object> list = new DataConverter.List<Object>();
//		Predicate<Object> list = new DataConverter.Predicate<Object>();
//		list.add("a");
//		list.add("b");
//		list.add("c");
//		list.add((Integer)3);
//		list.add((Double)2.4);
//		PrimIntArrayList converted = new PrimIntArrayList();
//		DataConverter.javaToWAMGeneral(w, list, converted);
//		System.out.println(converted.size());
//		System.out.println(WAMToString.oneLineHeap(converted.intdata, 0, converted.size(), w.getStringContainer(), w.getNums()));
//		System.out.println(WAMToString.termToString(converted.intdata, converted.size()-1, w.getStringContainer(), w.getNums(), false));
// 
//		
//		l = System.currentTimeMillis();
//		for(int i = 0; i < amount; i++){
//			w.prepare_for_new_query();
//			w.append_query_code(q);
//			w.execute(); 
//		}
//		d = System.currentTimeMillis()-l;
//		System.out.println(((d)/amount) +" average "+d+"ms total");
//		System.out.println(WAMToString.queryResult(w)); 
//		

    }
}
