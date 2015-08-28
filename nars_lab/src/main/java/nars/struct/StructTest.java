package nars.struct;

import javolution.io.Struct;

/**
 * Created by me on 8/28/15.
 */
public class StructTest {

    public enum Gender { MALE, FEMALE };
    public static class Date extends Struct {
        public final Unsigned16 year = new Unsigned16();
        public final Unsigned8 month = new Unsigned8();
        public final Unsigned8 day   = new Unsigned8();
    }
    public static class Student extends Struct {
        public final Enum32<Gender>       gender = new Enum32<Gender>(Gender.values());
        public final UTF8String name   = new UTF8String(64);
        public final Date                 birth  = inner(new Date());
        public final Float32[]            grades = array(new Float32[10]);
        public final Reference32<Student> next   =  new Reference32<Student>();
    }}
