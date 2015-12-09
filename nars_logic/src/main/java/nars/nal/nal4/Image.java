package nars.nal.nal4;

import com.gs.collections.api.block.function.primitive.ObjectIntToObjectFunction;
import nars.Op;
import nars.Symbols;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import nars.term.compound.CompoundN;
import nars.util.utf8.ByteBuf;

import java.io.IOException;
import java.util.Arrays;

import static nars.Symbols.*;

/**
 * @author me
 */


public abstract class Image<T extends Term> extends CompoundN<T> {

    public static Term makeInt(Term... argList) {
        return make(argList, (a, r) -> new ImageInt(a, (short)r));
    }
    public static Term makeExt(Term... argList) {
        return make(argList, (a, r) -> new ImageExt(a, (short) r));
    }

        /** Image index ("imdex") symbol */
        public static final Atom Index = Atom.the(String.valueOf(IMAGE_PLACE_HOLDER));

    /**
     * "Imdex": subterm index of relation in the component list
     */
    public final short relationIndex;


    protected Image(T[] components, int relationIndex) {
        super(relationIndex+1 /* non-zero */,
                components);

        this.relationIndex = ((short) relationIndex);
    }


    @Override
    public final boolean equals(Object o) {
        if (o == this) return true;
        if (super.equals(o)) {
            return ((Image)o).relationIndex == relationIndex;
        }
        return false;
    }

    @Override
    public final int compareTo(Object o) {
        if (o == this) return 0;
        int d = super.compareTo(o);
        if (d == 0) {
            return Integer.compare(((Image)o).relationIndex, relationIndex);
        }
        return d;
    }

    //    /**
//     * apply the relation index as the additional structure code to differnetiate
//     * images with different relations
//     */
//    @Override
//    public final int structure2() {
//        return relationIndex+1;
//    }

    @Override public final boolean isCommutative() {
        return false;
    }

    //TODO replace with a special Term type
    static boolean isPlaceHolder(final Term t) {
//        if (t instanceof Compound) return false;
//        byte[] n = t.bytes();
//        if (n.length != 1) return false;
        return t.equals(Index);
    }

    @Override
    public int bytesLength() {
        return super.bytesLength() + 1;
    }

    @Override
    public byte[] bytes() {

        ByteBuf b = ByteBuf.create(bytesLength());

        b.add((byte) op().ordinal()); //header

        b.add((byte)relationIndex); //relation index

        appendSubtermBytes(b);

        b.add(COMPOUND_TERM_CLOSERbyte); //closer

        return b.toBytes();
    }

    @Override public boolean matchCompoundEx(Compound y) {
        /** if they are images, they must have same relationIndex */
        return super.matchCompoundEx(y)
                && (relationIndex == ((Image) y).relationIndex);
    }


    @Override
    public void append(Appendable p, boolean pretty) throws IOException {

        final int len = this.size();

        p.append(COMPOUND_TERM_OPENER);
        p.append(this.op().str);

        final int relationIndex = this.relationIndex;
        int i;
        for (i = 0; i < len; i++) {
            Term tt = this.term(i);

            p.append(ARGUMENT_SEPARATOR);
            if (pretty) p.append(' ');

            if (i == relationIndex) {
                p.append(Symbols.IMAGE_PLACE_HOLDER);
                p.append(ARGUMENT_SEPARATOR);
                if (pretty) p.append(' ');
            }

            tt.append(p, pretty);
        }
        if (i == relationIndex) {
            p.append(ARGUMENT_SEPARATOR);
            if (pretty) p.append(' ');
            p.append(Symbols.IMAGE_PLACE_HOLDER);
        }

        p.append(COMPOUND_TERM_CLOSER);

    }

    public Term relation() {
        return term(relationIndex);
    }


    /**
     * Try to make a new ImageInt/ImageExt.
     * @return the Term generated from the arguments
     * @param argList The list of term
     */
    public static Term make(Term[] argList, ObjectIntToObjectFunction<Term[], Term> build) {
        int l = argList.length;
        if (l < 2) {
            return argList[0];
        }

        //Term relation = argList[0];

        Term[] argument = new Term[l];
        int index = 0, n = 0;
        for (int j = 0; j < l; j++) {
            if (isPlaceHolder(argList[j])) {
                index = j;
                if (n == l-1)
                    break;
            } else {
                argument[n++] =  argList[j];
            }
        }
        if (n == l - 1) {
            argument = Arrays.copyOf(argument, n);
        } else if (n == l) {
            index = l;
        }

        return build.valueOf(argument, index);
    }


    /**
     *
     * @param ext - true if ext, false if int
     * @param terms - terms to form the Image, extracting 0 or 1 index placeholders that override defaultIndex
     * @return
     */
    public static Image build(Op o, Term[] res) {


        int index = 0, j = 0;
        for (Term x : res) {
            if (x.equals(Image.Index)) {
                index = j;
            }
            j++;
        }

        if (index == -1) {
            index = 0;
        } else {
            int serN = res.length-1;
            Term[] ser = new Term[serN];
            System.arraycopy(res, 0, ser, 0, index);
            System.arraycopy(res, index+1, ser, index, (serN - index));
            res = ser;
        }

        boolean ext = (o == Op.IMAGE_EXT);
        return ext ? new ImageExt(res, index) : new ImageInt(res, index);
    }

    public static boolean hasPlaceHolder(Term[] r) {
        for (Term x : r) {
            if (isPlaceHolder(x)) return true;
        }
        return false;
    }



//   /**
//     * default method to make the oldName of an image term from given fields
//     *
//     * @param op the term operate
//     * @param arg the list of term
//     * @param relationIndex the location of the place holder
//     * @return the oldName of the term
//     */
//    protected static CharSequence makeImageName(final Op op, final Term[] arg, final int relationIndex) {
//        throw new RuntimeException("should not be used, utf8 instead");
////        final int sizeEstimate = 24 * arg.length + 2;
////
////        StringBuilder name = new StringBuilder(sizeEstimate)
////            .append(COMPOUND_TERM_OPENER.ch)
////            .append(op)
////            .append(Symbols.ARGUMENT_SEPARATOR)
////            .append(arg[relationIndex].toString());
////
////        for (int i = 0; i < arg.length; i++) {
////            name.append(Symbols.ARGUMENT_SEPARATOR);
////            if (i == relationIndex) {
////                name.append(Symbols.IMAGE_PLACE_HOLDER);
////            } else {
////                name.append(arg[i].toString());
////            }
////        }
////        name.append(COMPOUND_TERM_CLOSER.ch);
////        return name.toString();
//    }


//    /**
//     * Get the other term in the Image
//     *
//     * @return The term related
//     */
//    public Term getTheOtherComponent() {
//        if (term.length != 2) {
//            return null;
//        }
//        Term r = (relationIndex == 0) ? term[1] : term[0];
//        return r;
//    }



    //    @Override
//    public byte[] bytes() {
//
//        final int len = this.size();
//
//        //calculate total size
//        int bytes = 2 + 2 + 2;
//        for (int i = 0; i < len; i++) {
//            Term tt = this.term(i);
//            bytes += tt.bytes().length;
//            if (i != 0) bytes++; //comma
//        }
//
//        ByteBuf b = ByteBuf.create(bytes)
//                .add((byte) COMPOUND_TERM_OPENER)
//                .add(this.op().bytes)
//                .add((byte) ARGUMENT_SEPARATOR)
//                .add(this.relation().bytes());
//
//
//        final int relationIndex = this.relationIndex;
//        for (int i = 0; i < len; i++) {
//            Term tt = this.term(i);
//            b.add((byte) ARGUMENT_SEPARATOR);
//            if (i == relationIndex) {
//                b.add((byte) Symbols.IMAGE_PLACE_HOLDER);
//            } else {
//                b.add(tt.bytes());
//            }
//        }
//        b.add((byte) COMPOUND_TERM_CLOSER);
//
//        return b.toBytes();
//
//    }

}

