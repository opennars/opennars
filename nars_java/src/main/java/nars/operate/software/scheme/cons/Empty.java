package nars.operate.software.scheme.cons;

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

    @Override
    public Object car() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Cons<Object> cdr() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCar(Object car) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCdr(Cons<Object> cdr) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void append(Cons<Object> tail) {
        throw new UnsupportedOperationException();
    }

}
