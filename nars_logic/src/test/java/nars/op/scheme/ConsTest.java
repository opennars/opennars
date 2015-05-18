package nars.op.scheme;

import com.google.common.collect.ImmutableList;
import nars.op.software.scheme.cons.Cons;
import org.junit.Test;

import static nars.op.software.scheme.cons.Cons.cons;
import static nars.op.software.scheme.cons.Cons.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ConsTest {
    @Test
    public void map() {
        Cons<Integer> list = cons(1, cons(2, empty()));
        list.stream().map(e ->
                e + 1).collect(Cons.collector());

        assertThat(list.stream().map(e -> e + 1).collect(Cons.collector()).toList(), is(ImmutableList.of(2, 3)));
    }



}