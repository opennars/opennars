package nars.prolog;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({	BuiltInTestCase.class,
				PrologTestCase.class, 
				IntTestCase.class, 
				IOLibraryTestCase.class, 
				DoubleTestCase.class, 
				SolveInfoTestCase.class,
				StateRuleSelectionTestCase.class, 
				StructIteratorTestCase.class, 
				StructTestCase.class, 
				TermIteratorTestCase.class,
				TheoryTestCase.class, 
				TheoryManagerTestCase.class, 
				LibraryTestCase.class, 
				JavaLibraryTestCase.class, 
				ParserTestCase.class,
				SpyEventTestCase.class, 
				VarTestCase.class, 
				TestVarIsEqual.class, 
				JavaDynamicClassLoaderTestCase.class,
				ISOIOLibraryTestCase.class,
				SocketLibTestCase.class,
				ThreadLibraryTestCase.class
})
public class TuPrologTestSuite {}
