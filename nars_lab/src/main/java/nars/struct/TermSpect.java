package nars.struct;

import nars.Op;
import nars.term.Term;
import nars.term.transform.TermVisitor;
import nars.util.data.id.Identifier;

import java.nio.ByteBuffer;

/** represents one particular Term "view" of a TermCept, at a time.
 *  a view is parameterized by an Op type
 */
public class TermSpect extends TermCept implements Term /* Concept */ {

    private Op op = null;

    public TermSpect() {
        super();
    }

    public TermSpect(TermCept t, Op o) {
        this();
        the(t, o);
    }

    public void believe(final float f, final float c) {
        believe(f, c, op);
    }

    @Override
    public Op operator() {
        return null;
    }

    @Override
    public int volume() {
        return 0;
    }

    @Override
    public int complexity() {
        return 0;
    }

    @Override
    public void recurseTerms(TermVisitor v, Term parent) {

    }

    @Override
    public void recurseSubtermsContainingVariables(TermVisitor v, Term parent) {

    }

    @Override
    public int containedTemporalRelations() {
        return 0;
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean isNormalized() {
        return false;
    }

    @Override
    public boolean containsTerm(Term target) {
        return false;
    }

    @Override
    public boolean containsTermRecursivelyOrEquals(Term target) {
        return false;
    }

    @Override
    public Term clone() {
        return null;
    }

    @Override
    public Term cloneDeep() {
        return null;
    }

    @Override
    public boolean hasVar() {
        return false;
    }

    @Override
    public int varIndep() {
        return 0;
    }

    @Override
    public int varDep() {
        return 0;
    }

    @Override
    public int varQuery() {
        return 0;
    }

    @Override
    public int getTotalVariables() {
        return 0;
    }

    @Override
    public long structureHash() {
        return 0;
    }

    @Override
    public int structure() {
        return 0;
    }

    @Override
    public boolean impossibleStructure(int possibleSubtermStructure) {
        return false;
    }

    @Override
    public boolean impossibleSubTermVolume(int otherTermVolume) {
        return false;
    }

    public TermSpect the(ByteBuffer b, int offset, Op o) {
        set(b, offset);
        the(o);
        return this;
    }

    public TermSpect the(TermCept t, Op o) {
        set(t.getByteBuffer(), t.outerOffset);
        this.op = o;
        return this;
    }

    public TermSpect the(TermCore core, int a, Op o) {
        set(core, a);
        the(o);
        return this;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    @Override
    public Identifier name() {
        return null;
    }

    public TermSpect the(Op op) {
        this.op = op;
        return this;
    }
}
