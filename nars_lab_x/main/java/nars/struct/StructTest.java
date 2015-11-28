package nars.struct;


/**
 * Created by me on 8/28/15.
 */
public class StructTest {

    public enum Gender { MALE, FEMALE };
    public static class Date extends Fuct {
        public final Unsigned16 year = new Unsigned16();
        public final Unsigned8 month = new Unsigned8();
        public final Unsigned8 day   = new Unsigned8();
    }
    public static class Student extends Fuct {
        public final Enum32<Gender>       gender = new Enum32<Gender>(Gender.values());
        public final UTF8String name   = new UTF8String(64);
        public final Date                 birth  = inner(new Date());
        public final Float32[]            grades = array(new Float32[10]);
        public final Reference32<Student> next   =  new Reference32<Student>();
    }

    public static void main(String[] args) {

        Student student = new Student();
        student.gender.set(Gender.MALE);
        student.name.set("John Doe"); // Null terminated (C compatible)
        int age = 2003 - student.birth.year.get();
        student.grades[2].set(12.5f);

        System.out.println(student);

        student = student.next.get();

        {
            Student a = new Student();
            a.gender.set(Gender.MALE);
            a.name.set("John Doe"); // Null terminated (C compatible)
            a.grades[2].set(12.5f);
        }

    }

}
