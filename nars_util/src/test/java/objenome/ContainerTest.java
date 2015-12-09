package objenome;

import objenome.MultitainerTest.Part;
import objenome.MultitainerTest.Part0;
import objenome.solution.dependency.Scope;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** TODO convert this into unit tests */
public class ContainerTest {

//    public static void main(String[] args) {
//
//        case1();
//        case2();
//        case3();
//        case4();
//        case5();
//        case6();
//        case7();
//        case8();
//        case9();
//        case10();
//        case11();
//    }

    public static class Connection {

    }

    public interface UserDAO {

        String getUsername(int id);
    }

    public static class JdbcUserDAO implements UserDAO {

        private Connection conn;

        public void setConnection(Connection conn) {
            this.conn = conn;
        }

        @Override
        public String getUsername(int id) {

            // connection will be injected by the container...
            if (conn == null) {
                throw new IllegalStateException("conn is null!");
            }

            // usable the connection to get the username...
            return "saoj";
        }
    }

    public interface AccountDAO {

        double getBalance(int id);
    }

    public static class JdbcAccountDAO implements AccountDAO {

        private final Connection conn;

        public JdbcAccountDAO(Connection conn) {
            this.conn = conn;
        }

        @Override
        public double getBalance(int id) {

            assert conn != null;

            // usable the connection to get the balance...
            return 1000000.0D;
        }
    }

    @Test public void case9() {

        AbstractContainer c = new Container();

        c.use("connection", Connection.class); // in real life this will be a connection pool factory...
        // all beans that need a connection in the constructor or setter will receive one...
        
        c.usable("accountDAO", JdbcAccountDAO.class);
        c.usable("userDAO", JdbcUserDAO.class);

        AccountDAO accountDAO = c.get("accountDAO");
        UserDAO userDAO = c.get("userDAO");

        assertEquals(1000000.0D, accountDAO.getBalance(25), 0.01);
        assertEquals("saoj", userDAO.getUsername(45));
    }

    @Test public void case1() {

        AbstractContainer c = new Container();

        c.usable("myString1", String.class);

        assertEquals("default constructor new String() was used", "", c.get("myString1"));

        c.usable("myString2", String.class).addInitValue("saoj");

        String s2 = c.get("myString2");
        assertEquals("==> constructor new String(\"saoj\") initialized via addInitValue", "saoj", s2);
        

        c.usable("myDate1", Date.class).addPropertyValue("hours", 15) // setHours(15)
                .addPropertyValue("minutes", 10) // setMinutes(10)
                .addPropertyValue("seconds", 45); // setSeconds(45)

        Date myDate1 = c.get("myDate1");

        assertTrue(myDate1.toString().contains("15:10:45"));
    }

    @Test public void case5() {

        AbstractContainer c = new Container();

        c.usable("connection", Connection.class); // in real life this will be a connection pool factory...

        c.usable("accountDAO", JdbcAccountDAO.class).constructorUse("connection");

        AccountDAO accountDAO = c.get("accountDAO");

        assertEquals(1000000, accountDAO.getBalance(25), 0.01);
    }

    @Test public void case7() {

        AbstractContainer c = new Container();

        c.use("connection", Connection.class); // in real life this will be a connection pool factory...
        // all beans that need a connection in the constructor will get one...

        c.usable("accountDAO", JdbcAccountDAO.class);

        AccountDAO accountDAO = c.get("accountDAO");

        assertEquals(1000000, accountDAO.getBalance(25), 0.01);

    }

    @Test public void case6() {

        AbstractContainer c = new Container();

        c.usable("connection", Connection.class); // in real life this will be a connection pool factory...

        c.usable("userDAO", JdbcUserDAO.class).addPropertyDependency("connection");

        UserDAO userDAO = c.get("userDAO");

        assertEquals("saoj", userDAO.getUsername(54));
    }

    @Test public void case8() {

        AbstractContainer c = new Container();

        c.use("connection", Connection.class); // in real life this will be a connection pool factory...

        c.usable("userDAO", JdbcUserDAO.class);
        

        UserDAO userDAO = c.get("userDAO");

        assertEquals("saoj", userDAO.getUsername(54));

    }

    @Test public void case2() {

        AbstractContainer c = new Container();

        c.usable("myString", Scope.SINGLETON, String.class).addInitValue("saoj");

        String s1 = c.get("myString");

        String s2 = c.get("myString");

        assertTrue(s1 == s2); // ==> true ==> same instance

        assertEquals(s1, s2); // ==> true => of course
    }

    @Test public void case3() {

        AbstractContainer c = new Container();

        c.usable("userDAO", JdbcUserDAO.class);

        c.use("connection", Connection.class); // in real life this would be a connection pool
        // or the hibernate SessionFactory

        // "conn" = the name of the property
        // Connection.class = the type of the property
        // "connection" = the source from where the dependency will come from
        

        UserDAO userDAO = c.get("userDAO");

        // the container detects that userDAO has a dependency: name = "conn" and type = "Connection.class"
        // where does it go to get the dependency to insert?
        // In itself: it does a AbstractContainer.get("connection") => "connection" => the source
        assertEquals("connection is not null as expected...", "saoj", userDAO.getUsername(11));
    }

    public static class SomeService {

        private UserDAO userDAO;

        public void setUserDAO(UserDAO userDAO) {
            this.userDAO = userDAO;
        }

        public boolean doSomething() {
            //System.out.println(userDAO.getUsername(11));
            return true;
        }
    }

    @Test public void case4() {

        AbstractContainer c = new Container();

        c.usable("userDAO", JdbcUserDAO.class);

        c.use("connection", Connection.class);

        SomeService service = new SomeService();

        // populate (apply) all properties of SomeService with
        // beans from the container
        assertTrue(c.apply(service).doSomething()); 
    }

    public static class ExampleService {

        private final UserDAO userDAO;

        public ExampleService(UserDAO userDAO) {
            this.userDAO = userDAO;
        }

        public String doSomething() {            
            return userDAO.getUsername(11);
        }
    }
    
    public static class ParameterX {
        public ParameterX() { }        
    }
    
    public static class ServiceNeedingDAOandParameter {

        private final UserDAO userDAO;
        private final ParameterX x;

        public ServiceNeedingDAOandParameter(UserDAO userDAO, ParameterX x) {
            this.userDAO = userDAO;
            this.x = x;
        }

        public ParameterX function() {
            userDAO.getUsername(11);
            //System.out.println(userDAO.getUsername(11) + " " + x);
            return x;
        }
    }    

    @Test public void case10() {

        AbstractContainer c = new Container();

        c.usable("userDAO", JdbcUserDAO.class);        

        c.use("connection", Connection.class);

        ExampleService service = c.get(ExampleService.class);

        assertEquals("saoj", service.doSomething()); // ==> "saoj"
    }

    @Test public void case11() {

        Container c = new Container();

        c.usable(JdbcUserDAO.class);        
        c.usable(ParameterX.class);
        
        c.use( Connection.class); //wires to setter

        ServiceNeedingDAOandParameter service = c.get(ServiceNeedingDAOandParameter.class);

        Assert.assertNotNull(service.function());
    }

    /** tests what happens when a key is replaced */
    @Test public void testAmbiguity() {
        
        Container c = new Container();
        c.usable(ServiceNeedingDAOandParameter.class, MultitainerTest.Part0.class);
        assertEquals(0, c.get(MultitainerTest.Machine.class).function());
        c.usable(ServiceNeedingDAOandParameter.class, MultitainerTest.Part1.class);
        assertEquals("overrides the first builder", 1, c.get(MultitainerTest.Machine.class).function());
        
    }
    
    /** tests what happens when a key is replaced */
    @Test public void testInstantiateKey() {
        Container c = new Container();
        c.use(Part.class, Part0.class);
        Part p = c.get(Part.class);
        assertEquals(0, p.function());
    }    
    
/** tests what happens when a key is replaced */
    @Test public void testSingleton() {
        Container c = new Container();
        Part x = c.the("part", new Part0());
        Part y = c.the("part", new Part0());
        assertTrue(x==y);
        Part z = c.the("part2", new Part0());
        assertTrue(x!=z);
        Part w = c.the(new Part0());
        assertTrue(w!=z);
        Part v = c.the(Part0.class);
        assertTrue(v==w);
    }
    
    

}
