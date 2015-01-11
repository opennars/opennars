package nars.jwam;


import nars.jwam.datastructures.WAMToString;

public class Repl {

    public static void print(WAM w) {
        System.out.println(w.getRuleHeap().toString(w.strings(),w.numbers(),w.regStart()));         
        
    }
    
    public static class Prolog {

        final WAM w;
        
        public Prolog() {
            w = new WAM(8192, 80, 2000, 200, 100, 6);
            
        }
        
        public void setTheory(String theorySource) {
            w.getCompiler().reset();
            w.getCompiler().compile_string(theorySource);
            w.getCompiler().finalize_compilation();            
        }

        public String query(String query) {
            w.prepare_for_new_query();
            w.append_query_code(w.getCompiler().compile_query(query));
            w.execute();
            return WAMToString.queryResult(w);
        }
        
        
    }
    
    public static void main(String[] args) {
        
        
        //Reader inreader = new InputStreamReader(System.in);
        Prolog p = new Prolog();
        
        try {

            p.setTheory("x(a,b). \n  y(C,D) :- x(D,C).");
            String result = p.query("y(b,a)");
            assert(result.equals("true"));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public void test() {
        //WAM w = new WAM(5000, 40, 1000, 100, 50, 3);
        WAM w = new WAM(8192, 80, 2000, 200, 100, 6);
        w.getCompiler().compile_file("src/main/java/nlp-1.pl");
//		w.getCompiler().compile_file("test2.pl");
        w.getCompiler().finalize_compilation();
        
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
            w.append_query_code(q);
            w.execute();
            //w.prepare_for_new_query();
            //w.append_query_code(q2);
            //w.execute(); 
        }
        long d = System.currentTimeMillis() - l;
        System.out.println(((d) / amount) + " average " + d + "ms total");
        System.out.println(WAMToString.queryResult(w));
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
