package objenome.util.bean;

import objenome.util.bean.anno.GenericBeanKeyMethod;
import objenome.util.bean.anno.GenericBeanKeyProvider;
import objenome.util.bean.anno.GenericBeanMethod;
import objenome.util.bean.anno.GenericBeanMethod.Type;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class GetGenericTest {

    // MyGenericAccessBean would be the super interface for all our project's beans
    public interface MyGenericAccessBean {

        @GenericBeanMethod(Type.GENERIC_GET)
        Object get(MyKeyEnum key);

        @GenericBeanMethod(Type.GENERIC_SET)
        void set(MyKeyEnum key, Object value);

        @GenericBeanMethod(Type.IS_SET)
        boolean notNull(MyKeyEnum key);

        @GenericBeanMethod(Type.KEYS)
        Set<MyKeyEnum> keys();

    }

    public enum MyKeyEnum {
        NAME, PRENAME, YEAR_OF_BIRTH, ADDRESS, STREET, ZIP, CITY
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    // annotate MyGenericAccessAnno to be the GenericBeanKey
    @GenericBeanKeyProvider
    public @interface MyGenericAccessAnno {

        @GenericBeanKeyMethod
        // this method annotation here is optional since the value method is default
        MyKeyEnum value();

    }

    public interface User extends MyGenericAccessBean {

        @MyGenericAccessAnno(MyKeyEnum.NAME)
        String getName();

        @MyGenericAccessAnno(MyKeyEnum.NAME)
        void setName(String name);

        @MyGenericAccessAnno(MyKeyEnum.PRENAME)
        String getPreName();

        @MyGenericAccessAnno(MyKeyEnum.PRENAME)
        void setPreName(String prename);

        @MyGenericAccessAnno(MyKeyEnum.YEAR_OF_BIRTH)
        int getYearOfBirth();

        @MyGenericAccessAnno(MyKeyEnum.YEAR_OF_BIRTH)
        void setYearOfBirth(int age);

        @MyGenericAccessAnno(MyKeyEnum.ADDRESS)
        Address getAddress();

        @MyGenericAccessAnno(MyKeyEnum.ADDRESS)
        void setAddress(Address address);

    }

    public interface Address extends MyGenericAccessBean {

        @MyGenericAccessAnno(MyKeyEnum.STREET)
        String getStreet();

        @MyGenericAccessAnno(MyKeyEnum.STREET)
        void setStreet(String street);

        @MyGenericAccessAnno(MyKeyEnum.ZIP)
        short getZip();

        @MyGenericAccessAnno(MyKeyEnum.ZIP)
        void setZip(short zip);

        @MyGenericAccessAnno(MyKeyEnum.CITY)
        String getCity();

        @MyGenericAccessAnno(MyKeyEnum.CITY)
        void setCity(String city);
    }

    @Test
    public void testGeneric() {
        User user = BeanProxyBuilder.on(User.class).build();
        // ----------------------------------------------------
        assertFalse(user.notNull(MyKeyEnum.NAME));
        assertFalse(user.notNull(MyKeyEnum.PRENAME));
        assertFalse(user.notNull(MyKeyEnum.YEAR_OF_BIRTH));
        assertFalse(user.notNull(MyKeyEnum.ADDRESS));
        // ----------------------------------------------------
        user.set(MyKeyEnum.NAME, "Fichtner"); //$NON-NLS-1$
        user.set(MyKeyEnum.PRENAME, "Peter"); //$NON-NLS-1$
        user.setYearOfBirth(1974); // set it directly for fun :-)

        Address address = BeanProxyBuilder.on(Address.class).build();
        address.setZip((short) 12345);
        address.setCity("Musterstadt"); //$NON-NLS-1$
        user.setAddress(address);

        // ----------------------------------------------------
        assertTrue(user.notNull(MyKeyEnum.NAME));
        assertTrue(user.notNull(MyKeyEnum.PRENAME));
        assertTrue(user.notNull(MyKeyEnum.YEAR_OF_BIRTH));
        assertTrue(user.notNull(MyKeyEnum.ADDRESS));
        // ----------------------------------------------------

        Map<MyKeyEnum, Object> mapWithoutAddress = copyToMap(user, new HashMap<>());
        // remove the address object (the bean Address) from the map
        mapWithoutAddress.remove(MyKeyEnum.ADDRESS);

        assertEquals(getExpected(), mapWithoutAddress);
    }

    /**
     * Copy all the values of the bean to the passed map and returns the map
     * 
     * @param bean the bean to read from
     * @param map the map to copy the data to
     * @return the passed in map
     */
    private Map<MyKeyEnum, Object> copyToMap(MyGenericAccessBean bean, Map<MyKeyEnum, Object> map) {
        for (MyKeyEnum key : bean.keys()) {
            map.put(key, bean.get(key));
        }
        return map;
    }

    private Map<MyKeyEnum, Object> getExpected() {
        Map<MyKeyEnum, Object> map = new HashMap<>();
        map.put(MyKeyEnum.NAME, "Fichtner"); //$NON-NLS-1$
        map.put(MyKeyEnum.PRENAME, "Peter"); //$NON-NLS-1$
        map.put(MyKeyEnum.YEAR_OF_BIRTH, 1974);
        return map;
    }

}
