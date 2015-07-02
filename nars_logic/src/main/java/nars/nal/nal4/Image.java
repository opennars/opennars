package nars.nal.nal4;

import nars.Symbols;
import nars.nal.NALOperator;
import nars.term.Compound;
import nars.term.DefaultCompound;
import nars.term.Term;
import nars.util.utf8.ByteBuf;

import java.io.IOException;
import java.io.Writer;

import static nars.Symbols.ARGUMENT_SEPARATOR;
import static nars.nal.NALOperator.COMPOUND_TERM_CLOSER;
import static nars.nal.NALOperator.COMPOUND_TERM_OPENER;

/**
 *
 * @author me
 */


abstract public class Image extends DefaultCompound {
    /** The index of relation in the component list */
    transient public final short relationIndex;

    protected Image(Term[] components, short relationIndex) {
        super(components);
        
        this.relationIndex = relationIndex;
                
        init(components);
    }

    /** apply the relation index as the additional structure code to differnetiate
     * images with different relations
     */
    public int additionalStructureCode() { return relationIndex; }




    //TODO replace with a special Term type
    public static boolean isPlaceHolder(final Term t) {
        if (t instanceof Compound) return false;
        byte[] n = t.bytes();
        if (n.length != 1) return false;
        return n[0] == Symbols.IMAGE_PLACE_HOLDER;
    }    
    
   /**
     * default method to make the oldName of an image term from given fields
     *
     * @param op the term operate
     * @param arg the list of term
     * @param relationIndex the location of the place holder
     * @return the oldName of the term
     */
    protected static CharSequence makeImageName(final NALOperator op, final Term[] arg, final int relationIndex) {
        throw new RuntimeException("should not be used, utf8 instead");
//        final int sizeEstimate = 24 * arg.length + 2;
//
//        StringBuilder name = new StringBuilder(sizeEstimate)
//            .append(COMPOUND_TERM_OPENER.ch)
//            .append(op)
//            .append(Symbols.ARGUMENT_SEPARATOR)
//            .append(arg[relationIndex].toString());
//
//        for (int i = 0; i < arg.length; i++) {
//            name.append(Symbols.ARGUMENT_SEPARATOR);
//            if (i == relationIndex) {
//                name.append(Symbols.IMAGE_PLACE_HOLDER);
//            } else {
//                name.append(arg[i].toString());
//            }
//        }
//        name.append(COMPOUND_TERM_CLOSER.ch);
//        return name.toString();
    }



    /**
     * Get the relation term in the Image
     * @return The term representing a relation
     */
    public Term getRelation() {
        return term[relationIndex];
    }


    /**
     * Get the other term in the Image
     * @return The term related
     */
    public Term getTheOtherComponent() {
        if (term.length != 2) {
            return null;
        }
        Term r = (relationIndex == 0) ? term[1] : term[0];
        return r;
    }


        @Override
        public byte[] init() {

            final int len = this.length();

            //calculate total size
            int bytes = 2+2+2;
            for (int i = 0; i < len; i++) {
                Term tt = this.term(i);
                bytes += tt.name().bytes().length;
                if (i!=0) bytes++; //comma
            }

            ByteBuf b = ByteBuf.create(bytes)
                    .add((byte) COMPOUND_TERM_OPENER.ch)
                    .add(this.operator().bytes)
                    .add((byte) ARGUMENT_SEPARATOR)
                    .add(this.relation().bytes());


            final int relationIndex = this.relationIndex;
            for (int i = 0; i < len; i++) {
                Term tt = this.term(i);
                b.add((byte) ARGUMENT_SEPARATOR);
                if (i == relationIndex) {
                    b.add((byte) Symbols.IMAGE_PLACE_HOLDER);
                } else {
                    b.add(tt.bytes());
                }
            }
            b.add((byte) COMPOUND_TERM_CLOSER.ch);

            return b.toBytes();

        }

        @Override
        public void append(Writer p, boolean pretty) throws IOException {

            final int len = this.length();

            p.append(COMPOUND_TERM_OPENER.ch);
            p.append(this.operator().str);

            p.append(ARGUMENT_SEPARATOR);
            if (pretty)
                p.append(' ');

            this.relation().append(p, pretty);

            final int relationIndex = this.relationIndex;
            for (int i = 0; i < len; i++) {
                Term tt = this.term(i);

                p.append(ARGUMENT_SEPARATOR);

                if (pretty)
                    p.append(' ');

                if (i == relationIndex) {
                    p.append(Symbols.IMAGE_PLACE_HOLDER);
                } else {
                    tt.append(p, pretty);
                }
            }
            p.append(COMPOUND_TERM_CLOSER.ch);

        }

    public Term relation() {
        return term(relationIndex);
    }


}

