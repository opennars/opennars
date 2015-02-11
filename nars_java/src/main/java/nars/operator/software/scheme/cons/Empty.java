package nars.operator.software.scheme.cons;

import com.google.common.collect.Iterators;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

class Empty extends Cons<Object> {
    public Empty() {
        super(null, null);
    }

    @Override public Iterator<Object> iterator() {
        return Iterators.emptyIterator();
    }

    @Override public void forEach(Consumer action) {
    }

    @Override public Spliterator<Object> spliterator() {
        return Spliterators.emptySpliterator();
    }

    public Object car() {
        throw new UnsupportedOperationException();
    }

    public Cons<Object> cdr() {
        throw new UnsupportedOperationException();
    }

    public void setCar(Object car) {
        throw new UnsupportedOperationException();
    }

    public void setCdr(Cons<Object> cdr) {
        throw new UnsupportedOperationException();

    }

    public boolean isEmpty() {
        return true;
    }

    public void append(Cons<Object> tail) {
        throw new UnsupportedOperationException();
    }

}
