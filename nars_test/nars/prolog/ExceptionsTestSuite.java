package nars.prolog;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({	BasicLibraryExceptionsTestCase.class, 
				BuiltInExceptionsTestCase.class, 
				DCGLibraryExceptionsTestCase.class,
				IOLibraryExceptionsTestCase.class, 
				ISOLibraryExceptionsTestCase.class, 
				JavaLibraryExceptionsTestCase.class, 
				JavaThrowCatchTestCase.class,
				ThrowCatchTestCase.class
})
public class ExceptionsTestSuite {}
