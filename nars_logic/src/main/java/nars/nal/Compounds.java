//package nars.nal;
//
//import nars.term.compile.TermIndex;
//
///**
// * static compound builder support
// */
//public interface Compounds extends TermIndex {
//
//
//    //
////    static byte[] newCompound1Key(Op op, Term singleTerm) {
////
////        byte opByte = (byte) op.ordinal();
////
////        byte[] termBytes = singleTerm.bytes();
////
////        return ByteBuf.create(1 + termBytes.length)
////                .add(opByte)
////                .add(termBytes)
////                .toBytes();
////    }
//
//
//    //    /**
////     * Try to make a Product from an ImageExt/ImageInt and a component. Called by the logic rules.
////     *
////     * @param image     The existing Image
////     * @param component The component to be added into the component list
////     * @param index     The index of the place-holder in the new Image -- optional parameter
////     * @return A compound generated or a term it reduced to
////     */
////    public static Term product(Compound<Term> image, Term component, int index) {
////        Term[] argument = image.termsCopy();
////        argument[index] = component;
////        return $.p(argument);
////    }
//
//    //    /**
////     * recursively flatten a embedded conjunction subterms if they are of a specific order
////     */
////    static Term[] flatten(Term[] args, Order order) {
////        //determine how many there are with same order
////
////        int expandedSize;
////        while ((expandedSize = getFlattenedLength(args, order)) != args.length) {
////            args = _flatten(args, order, expandedSize);
////        }
////        return args;
////    }
//
//    //    private static Term junction(Op o, Collection<Term> t) {
////        return junction(o, Terms.toArray(t));
////    }
//
//
//    //    static Term[] _flatten(Term[] args, Order order, int expandedSize) {
////        Term[] ret = new Term[expandedSize];
////        int k = 0;
////        for (Term a : args) {
////            if (a.op().isConjunctive(order)) {
////                //arraycopy?
////                for (Term t : ((Compound) a).terms()) {
////                    ret[k++] = t;
////                }
////            } else {
////                ret[k++] = a;
////            }
////        }
////
////        return ret;
////    }
////
////    static int getFlattenedLength(Term[] args, Order order) {
////        int sz = 0;
////        for (Term a : args) {
////            if (a.op().isConjunctive(order))
////                sz += a.size();
////            else
////                sz++;
////        }
////        return sz;
////    }
//
//
////    /**
////     * Try to make a new ImageInt/ImageExt.
////     * @return the Term generated from the arguments
////     * @param argList The list of term
////     */
////    static Term makeImage(Term[] argList, ObjectIntToObjectFunction<Term[], Term> build) {
////        int l = argList.length;
////        if (l < 2) {
////            return argList[0];
////        }
////
////        //Term relation = argList[0];
////
////        Term[] argument = new Term[l];
////        int index = 0, n = 0;
////        for (int j = 0; j < l; j++) {
////            //        if (t instanceof Compound) return false;
//////        byte[] n = t.bytes();
//////        if (n.length != 1) return false;
////            if (argList[j].equals(Op.Index)) {
////                index = j;
////                if (n == l-1)
////                    break;
////            } else {
////                argument[n++] =  argList[j];
////            }
////        }
////        if (n == l - 1) {
////            argument = Arrays.copyOf(argument, n);
////        } else if (n == l) {
////            index = l;
////        }
////
////        return build.valueOf(argument, index);
////    }
//
//
////   /**
////     * default method to make the oldName of an image term from given fields
////     *
////     * @param op the term operate
////     * @param arg the list of term
////     * @param relationIndex the location of the place holder
////     * @return the oldName of the term
////     */
////    protected static CharSequence makeImageName(final Op op, final Term[] arg, final int relationIndex) {
////        throw new RuntimeException("should not be used, utf8 instead");
//////        final int sizeEstimate = 24 * arg.length + 2;
//////
//////        StringBuilder name = new StringBuilder(sizeEstimate)
//////            .append(COMPOUND_TERM_OPENER.ch)
//////            .append(op)
//////            .append(Symbols.ARGUMENT_SEPARATOR)
//////            .append(arg[relationIndex].toString());
//////
//////        for (int i = 0; i < arg.length; i++) {
//////            name.append(Symbols.ARGUMENT_SEPARATOR);
//////            if (i == relationIndex) {
//////                name.append(Symbols.IMAGE_PLACE_HOLDER);
//////            } else {
//////                name.append(arg[i].toString());
//////            }
//////        }
//////        name.append(COMPOUND_TERM_CLOSER.ch);
//////        return name.toString();
////    }
//
//
////    /**
////     * Get the other term in the Image
////     *
////     * @return The term related
////     */
////    public Term getTheOtherComponent() {
////        if (term.length != 2) {
////            return null;
////        }
////        Term r = (relationIndex == 0) ? term[1] : term[0];
////        return r;
////    }
//
//
//    //    @Override
////    public byte[] bytes() {
////
////        final int len = this.size();
////
////        //calculate total size
////        int bytes = 2 + 2 + 2;
////        for (int i = 0; i < len; i++) {
////            Term tt = this.term(i);
////            bytes += tt.bytes().length;
////            if (i != 0) bytes++; //comma
////        }
////
////        ByteBuf b = ByteBuf.create(bytes)
////                .add((byte) COMPOUND_TERM_OPENER)
////                .add(this.op().bytes)
////                .add((byte) ARGUMENT_SEPARATOR)
////                .add(this.relation().bytes());
////
////
////        final int relationIndex = this.relationIndex;
////        for (int i = 0; i < len; i++) {
////            Term tt = this.term(i);
////            b.add((byte) ARGUMENT_SEPARATOR);
////            if (i == relationIndex) {
////                b.add((byte) Symbols.IMAGE_PLACE_HOLDER);
////            } else {
////                b.add(tt.bytes());
////            }
////        }
////        b.add((byte) COMPOUND_TERM_CLOSER);
////
////        return b.toBytes();
////
////    }
//
////    /**
////     * constructor with partial values, called by make
////     * @param arg The component list of the term
////     * @param index The index of relation in the component list
////     */
////    public ImageInt(Term[] arg, int index) {
////        super(arg, index);
////    }
////
////
////    /**
////     * Clone an object
////     * @return A new object, to be casted into an ImageInt
////     */
////    @Override
////    public ImageInt clone() {
////        return new ImageInt(terms.term, relationIndex);
////    }
////
////    @Override
////    public Term clone(Term[] replaced) {
////        if ((replaced.length != size())
////                || Image.hasPlaceHolder(replaced)) //TODO indexOfPlaceHolder
////            return Image.makeInt(replaced);
////
//////        if (replaced.length != size())
//////            //return null;
//////            throw new RuntimeException("Replaced terms not the same amount as existing terms (" + terms().length + "): " + Arrays.toString(replaced));
////
////
////        return new ImageInt(replaced, relationIndex);
////    }
////
////    /**
////     * Try to make an Image from a Product and a relation. Called by the logic rules.
////     * @param product The product
////     * @param relation The relation
////     * @param index The index of the place-holder
////     * @return A compound generated or a term it reduced to
////     */
////    public static Term make(Product product, Term relation, short index) {
////        if (relation instanceof Product) {
////            Product p2 = (Product) relation;
////            if ((product.size() == 2) && (p2.size() == 2)) {
////                if ((index == 0) && product.term(1).equals(p2.term(1))) {// (\,_,(*,a,b),b) is reduced to a
////                    return p2.term(0);
////                }
////                if ((index == 1) && product.term(0).equals(p2.term(0))) {// (\,(*,a,b),a,_) is reduced to b
////                    return p2.term(1);
////                }
////            }
////        }
////
////        Term[] argument = product.termsCopy(); //shallow clone necessary because the index argument is replaced
////        argument[index] = relation;
////        return make(argument, index);
////    }
////
////    /**
////     * Try to make an Image from an existing Image and a component. Called by the logic rules.
////     * @param oldImage The existing Image
////     * @param component The component to be added into the component list
////     * @param index The index of the place-holder in the new Image
////     * @return A compound generated or a term it reduced to
////     */
////    public static Term make(ImageInt oldImage, Term component, short index) {
////        Term[] argList = oldImage.termsCopy();
////        int oldIndex = oldImage.relationIndex;
////        Term relation = argList[oldIndex];
////        argList[oldIndex] = component;
////        argList[index] = relation;
////        return make(argList, index);
////    }
////
////    /**
////     * Try to make a new compound from a set of term. Called by the public make methods.
////     * @param argument The argument list
////     * @param index The index of the place-holder in the new Image
////     * @return the Term generated from the arguments
////     */
////    public static ImageInt make(Term[] argument, int index) {
////        return new ImageInt(argument, index);
////    }
////
////
////    /**
////     * Get the operate of the term.
////     * @return the operate of the term
////     */
////    @Override
////    public Op op() {
////        return Op.IMAGE_INT;
////    }
////    /**
////     * Clone an object
////     * @return A new object, to be casted into an ImageExt
////     */
////    @Override
////    public ImageExt clone() {
////        return new ImageExt(terms.term, relationIndex);
////    }
////    @Override
////    public Term clone(Term[] replaced) {
////        if ((replaced.length != size())
////                || Image.hasPlaceHolder(replaced)) //TODO indexOfPlaceHolder
////            return Image.makeExt(replaced);
////
//////        if (replaced.length != size())
//////            //return null;
//////            throw new RuntimeException("Replaced terms not the same amount as existing terms (" + terms().length + "): " + Arrays.toString(replaced));
////
////        return new ImageExt(replaced, relationIndex);
////    }
////
////
////
////    /**
////     * Try to make an Image from an existing Image and a component. Called by the logic rules.
////     * @param oldImage The existing Image
////     * @param component The component to be added into the component list
////     * @param index The index of the place-holder in the new Image
////     * @return A compound generated or a term it reduced to
////     */
////    public static Term make(ImageExt oldImage, Term component, short index) {
////        Term[] argList = oldImage.termsCopy();
////        int oldIndex = oldImage.relationIndex;
////        Term relation = argList[oldIndex];
////        argList[oldIndex] = component;
////        argList[index] = relation;
////        return new ImageExt(argList, index);
////    }
////
////
////
////    /**
////     * get the operate of the term.
////     * @return the operate of the term
////     */
////    @Override
////    public final Op op() {
////        return Op.IMAGE_EXT;
////    }
//
// }
