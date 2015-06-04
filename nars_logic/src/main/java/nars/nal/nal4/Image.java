package nars.nal.nal4;

import nars.Symbols;
import nars.nal.NALOperator;
import nars.nal.term.Compound;
import nars.nal.term.DefaultCompound;
import nars.nal.term.Term;
import nars.util.data.id.DynamicUTF8Identifier;
import nars.util.data.id.UTF8Identifier;
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

    @Override
    public UTF8Identifier newName() {
        return new ImageUTF8Identifier(this);
    }


    //    @Override
//    public boolean equals2(final CompoundTerm other) {
//        return relationIndex == ((Image)other).relationIndex;
//    }


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
        return (relationIndex == 0) ? term[1] : term[0];
    }

    public final static class ImageUTF8Identifier extends DynamicUTF8Identifier {
        private final Image compound;

        public ImageUTF8Identifier(Image c) {
            this.compound = c;
        }

        @Override
        public byte[] newName() {

            final int len = compound.length();

            //calculate total size
            int bytes = 2;
            for (int i = 0; i < len; i++) {
                Term tt = compound.term(i);
                bytes += tt.name().bytes().length;
                if (i!=0) bytes++; //comma
            }

            ByteBuf b = ByteBuf.create(bytes)
                    .append((byte) COMPOUND_TERM_OPENER.ch)
                    .append(compound.operator().bytes)
                    .append((byte) ARGUMENT_SEPARATOR)
                    .append(compound.relation().bytes());


            final int relationIndex = compound.relationIndex;
            for (int i = 0; i < len; i++) {
                Term tt = compound.term(i);
                b.append((byte) ARGUMENT_SEPARATOR);
                if (i == relationIndex) {
                    b.append((byte) Symbols.IMAGE_PLACE_HOLDER);
                } else {
                    b.append(tt.bytes());
                }
            }
            b.append((byte) COMPOUND_TERM_CLOSER.ch);

            return b.toBytes();

        }

        @Override
        public void write(Writer p, boolean pretty) throws IOException {

            final int len = compound.length();

            p.write(COMPOUND_TERM_OPENER.ch);
            p.write(compound.operator().str);

            p.write(ARGUMENT_SEPARATOR);
            if (pretty)
                p.write(' ');

            compound.relation().write(p, pretty);

            final int relationIndex = compound.relationIndex;
            for (int i = 0; i < len; i++) {
                Term tt = compound.term(i);

                p.write(ARGUMENT_SEPARATOR);

                if (pretty)
                    p.write(' ');

                if (i == relationIndex) {
                    p.write(Symbols.IMAGE_PLACE_HOLDER);
                } else {
                    tt.write(p, pretty);
                }
            }
            p.write(COMPOUND_TERM_CLOSER.ch);

        }
    }

    public Term relation() {
        return term(relationIndex);
    }


}

