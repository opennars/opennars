package nars.op.software.scheme.cons;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Cons<T> implements Iterable<T> {
    private T car;
    private Cons<T> cdr;

    public Cons(T car, Cons<T> cdr) {
        this.car = car;
        this.cdr = cdr;
    }

    public static <T> Cons<T> cons(T car, Cons<T> cdr) {
        return new Cons<>(car, cdr);
    }

    @SafeVarargs
    public static <T> Cons<T> copyOf(T... items) {
        return copyOf(Arrays.asList(items));
    }

    public static <T> Cons<T> copyOf(Iterable<T> items) {
        Cons<T> result = empty();
        for (T t : items) {
            if (result == empty()) {
                result = cons(t, empty());
            } else {
                result.append(cons(t, empty()));
            }
        }
        return result;
    }

    public T car() {
        return car;
    }

    public Cons<T> cdr() {
        return cdr;
    }

    public void setCar(T car) {
        this.car = car;
    }

    public void setCdr(Cons<T> cdr) {
        this.cdr = cdr;
    }

    public T cadr() {
        return cdr().car();
    }

    public boolean isEmpty() {
        return false;
    }

    public long size() {
        return stream().count();
    }

    private static final Cons<Object> EMPTY = new Empty();

    @SuppressWarnings("unchecked")
    public static <E> Cons<E> empty() {
        return (Cons<E>) EMPTY;
    }

    @Override public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Cons<T> cons = Cons.this;

            @Override public boolean hasNext() {
                return cons != empty();
            }

            @Override public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                T next = cons.car;
                cons = cons.cdr;

                return next;
            }
        };
    }

    @Override public void forEach(Consumer<? super T> action) {
        for (T e : this) {
            action.accept(e);
        }
    }

    @Override public Spliterator<T> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.NONNULL);
    }

    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public void append(Cons<T> tail) {
        if (cdr == empty()) {
            setCdr(tail);
        } else {
            cdr.append(tail);
        }
    }

    public List<T> toList() {
        return stream().collect(Collectors.toList());
    }

    public static <T> Collector<T, Cons, Cons/*, Cons<T>, Cons<T>*/> collector() {
        return Collector.of(
                () -> cons(null, empty()),
                (accumulator, e) -> {
                    if (accumulator.car() == null) {
                        accumulator.setCar(e);
                    } else {
                        accumulator.append(cons(e, empty()));
                    }
                },
                (a, b) -> {
                    a.append(b);
                    return a;
                },
                (accumulator) -> {
                    if (accumulator.car() == null) {
                        return empty();
                    } else {
                        return accumulator;
                    }
                });

    }

    public String toString() {
        return String.format("(%s)",
                stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(" ")));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Cons<T> cons = (Cons<T>) o;

        return car.equals(cons.car) && cdr.equals(cons.cdr);

    }

    @Override
    public int hashCode() {
        int result = car.hashCode();
        result = 31 * result + cdr.hashCode();
        return result;
    }
}
