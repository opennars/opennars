package nars.nal.term;

import nars.util.data.id.Identifier;
import nars.util.data.id.UTF8Identifier;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by me on 6/2/15.
 */
abstract public class AbstractTerm implements Term {

    private Identifier id;

    public AbstractTerm() {

    }

    public AbstractTerm(String name) {
        this(new UTF8Identifier(name));
    }

    public AbstractTerm(Identifier x) {
        this.id = x;
    }

    /** removes the name, forcing the Term to regenerate it */
    public void invalidate() {
        setName(null);
    }

    public boolean hasName() { return this.id!=null; }

    protected void setName(Identifier i) {
        this.id = i;
        if (i!=null)
            i.set(this);
    }

    @Override
    public Identifier name() {
        if (id == null)
            throw new RuntimeException("null name");
        return id;
    }

    @Override
    public void identifierEquals(Identifier other) {
        this.id = other;
    }


    @Override public String toString() {
        return name().toString();
    }

    /**
     * Produce a hash code for the term
     *
     * @return An integer hash code
     */
    @Override
    public int hashCode() {
        return name().hashCode();
    }

    abstract public Term clone();
}
