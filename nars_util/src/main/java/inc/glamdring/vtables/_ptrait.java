package inc.glamdring.vtables;


import java.lang.reflect.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;


/**
 * User: jim
 * Date: Sep 14, 2008
 * Time: 2:31:31 AM
 */
public enum _ptrait implements _vtable<_ptrait>, _proto<_ref> {
    $Object {{
        ref = new _ref<>();
    }}, $Access,
    $Arg,
    $Arity,
    $Array,
    $Big,
    $Binary,
    $Bind,
    $Broadcast,
    $Catchable,
    $Cdestructuring,
    $Compiled,
    $Complex,
    $Complex$Special,
    $Condition,
    $Declaration,
    $Expression,
    $Facade,
    $Factory,
    $File,
    $Form,
    $High,
    $In,
    $Info,
    $Input($In),
    $Interpreted,
    $Invalid,
    $IO,
    $Iterator,
    $Keyword,
    $Low,
    $LTiny,
    $Math,
    $Name,
    $Native,
    $Out,
    $Output($Out),
    $Print,
    $Quarternary,
    $Quintenary,
    $Random,
    $Read,
    $Socket,
    $Special,
    $Synonym,
    $System,
    $Ternary,
    $Test,
    $Text,
    $Throw,
    $Trampoline,
    $Translated,
    $Unary,
    $Unit,
    $User,
    $Utility,
    $Zero,

    $Sequence($Object) {{
        ref = new _ref<£Sequence>();
    }class £Sequence {
    }},
    $List($Sequence) {{
        ref = new _ref<£List>();
    }class £List {
    }},
    $Cons($List) {{
        ref = new _ref<£Cons>();
    }class £Cons {
    }},
    $Operator($Object) {{
        ref = new _ref<£Operator>();
    }class £Operator {
    }},
    $Function($Operator) {{
        ref = new _ref<£Function>();
    }class £Function {
    }},
    $Stream {{
        ref = new _ref<£Stream>();
    }class £Stream {
    }},

    $Number {{
        ref = new _ref<Number>();
    }},
    $Input$Stream,
    $Alien,
    $Any {{
        ref = new _ref<£Any>();
    }class £Any {
    }},
    $Array$List,
    $Array$Sequence,
    $Benchmark,
    $Big$Int$Bignum,
    $Bignum,
    $Binary$Function,
    $Binary$Stream,
    $Boolean {{
        ref = new _ref<Boolean>();
    }},
    $Broadcast$Stream($Stream) {{
        class £Broadcast$Stream {
        }
        ref = new _ref<£Broadcast$Stream>();
    }},
    $Byte,
    $Catchable$Throw,
    $Cdestructuring$Bind,
    $Character($Object) {{
        ref = new _ref<Character>();
    }},
    $Char($Character) {{
        ref = new _ref<Character>();
    }},
    $Character$Name,
    $Compatibility,
    $Compiled$Function($Function, $Operator) {{
        class £Compiled$Function {
        }
        ref = new _ref<£Compiled$Function>();
    }},
    $Complex$Special$Form,
    $Condition$Macro,
    $Cons$Facade,
    $Cons$High,
    $Cons$Low,
    $Cons$Pair($Cons) {{
        class £Cons$Pair {
        }
        ref = new _ref<£Cons$Pair>();
    }},
    $Double {{
        ref = new _ref<Double>();
    }},
    $Double$Float {{
        class £Double$Float {
        }
        ref = new _ref<£Double$Float>();
    }},
    $Dynamic,
    $Environment() {{
        ref = new _ref<£Environment>();
    }class £Environment {
    }},
    $Equality,
    $Eval,
    $Exception,
    $Filesys,
    $Fixnum,
    $Float {{
        ref = new _ref<Float>();
    }},
    $Foreign,
    $Format,
    $Function$Arg$List$Description,
    $Guid {{
        ref = new _ref<£Guid>();
    }class £Guid {
    }},
    $Hashtable$High,
    $Hashtable$Iterator {{
        class £Hashtable$Iterator {
        }
        ref = new _ref<£Hashtable$Iterator>();
    }},
    $Hashtable,
    $In$Out$Binary$Stream($Stream) {{
        class £In$Out$Binary$Stream {
        }
        ref = new _ref<£In$Out$Binary$Stream>();
    }},
    $In$Out$Text$Stream($Stream) {{
        class £In$Out$Text$Stream {
        }
        ref = new _ref<£In$Out$Text$Stream>();
    }},
    $Input$Binary$Stream($Input$Stream, $Object, $Stream) {{
        class £Input$Binary$Stream {
        }
        ref = new _ref<£Input$Binary$Stream>();
    }},
    $Input$Text$Stream($Input$Binary$Stream, $Input$Stream, $Object, $Stream) {{
        class £Input$Text$Stream {
        }
        ref = new _ref<£Input$Text$Stream>();
    }},
    $Integer($Number) {{
        ref = new _ref<Integer>();
    }},
    $Int($Integer) {{
        ref = new _ref<Integer>();
    }},
    $Integer$Bignum,
    $Interpreted$Function($Function),
    $Invalid$Expression$Exception,
    $Keyhash {{
        ref = new _ref<£Keyhash>();
    }class £Keyhash {
    }},
    $Keyhash$Iterator,
    $Lock {{
        ref = new _ref<Lock>();
    }},
    $Long($Integer) {{
        ref = new _ref<Long>();
    }},
    $Long$Bignum,
    $Macro {{
        ref = new _ref<£Macro>();
    }class £Macro {
    }},
    $Mapper,
    $Mapping,
    $Math$Utility,
    $Math$Plus {{
        ￥ = (Number[] a) -> a[0].doubleValue() + a[1].doubleValue();
    }},
    $Math$Minus {{
        ￥ = (Number[] a) -> a[0].doubleValue() - a[1].doubleValue();
    }},
    $Math$Sin {{
        ￥ = (Number a) -> Math.sin(a.doubleValue());
    }},
    $Nil($List) {{
        ref = new _ref<£Nil>();
    }class £Nil {
    }},
    $Output$Binary$Stream($Stream) {{
        class £Output$Binary$Stream {
        }
        ref = new _ref<£Output$Binary$Stream>();
    }},
    $Output$Stream,
    $Output$Text$Stream($Stream) {{
        class £Output$Text$Stream {
        }
        ref = new _ref<£Output$Text$Stream>();
    }},
    $Package {{
        ref = new _ref<£Package>();
    }class £Package {
    }},
    $Parser,
    $Print$Function,
    $Print$High,
    $Print$Low,
    $Print$Macro,
    $Process {{
        ref = new _ref<Process>();
    }},
    $Quarternary$Function,
    $Quintenary$Function,
    $Quote,
    $Random$Access$Stream,
    $Read$Write$Lock {{
        class £Read$Write$Lock {
        }
        ref = new _ref<£Read$Write$Lock>();
    }},
    $Reader,
    $Regex$Pattern {{
        class £Regex$Pattern {
        }
        ref = new _ref<£Regex$Pattern>();
    }},
    $Regex,
    $Resourcer,
    $Semaphore {{
        ref = new _ref<Semaphore>();
    }},
    $Socket$Stream($Object, $In$Out$Text$Stream, $Input$Binary$Stream, $Input$Stream, $Input$Text$Stream, $Output$Binary$Stream, $Output$Stream, $Output$Text$Stream, $Stream) {
        {
            class £Socket$Stream {
            }
            ref = new _ref<£Socket$Stream>();
        }
    },
    $Sort,
    $Special$Operator$Declaration,
    $Special$Operator,
    $Storage,
    $Stream$Factory,
    $Stream$Macro,
    $Stream$Nil$Exception,
    $Streams$High,
    $Streams$Low,
    $String($Sequence) {{
//        _ref = new ΩString();
    }},
    $Struct$Interpreted,
    $Struct$Native,
    $Struct($Object) {{
        ref = new _ref<£Struct>();
    }class £Struct {
    }},
    $Structure,
    $Sxhash,
    $Symbol$Keyword,
    $Symbol($Object) {{
        ref = new _ref<£Symbol>();
    }class £Symbol {
    }},
    $Synonym$Stream($Stream) {{
        class £Synonym$Stream {
        }
        ref = new _ref<£Synonym$Stream>();
    }},
    $System$Info,
    $T,
    $Tcp,
    $Ternary$Function,
    $Text$Stream,
    $Thread$Macro,
    $Thread,
    $Time,
    $Time$High,
    $Trampoline$File,
    $Translated$File,
    $Type,
    $Unary$Function,
    $Unit$Test,
    $Unit$Test_Cyc$LTiny,
    $User$IO,
    $Value,
    $Vector($Sequence) {{
        ref = new _ref<£Vector>();
    }class £Vector {
    }},
    $Zero$Arity$Function,;


    _ref ref;

    /** execute */
    Function<?,?> ￥;
    /*￥(X x) {
        return null;
    }*/

    private EnumSet<_ptrait> traits;
    private _ptrait[] as;

    /**
     * uses its own name as a composition of traits, as well as adding from that which is passed in.
     * <p/>
     * this is almost certainly unfit
     * for general consumption but
     * the metaphoir is desired as
     * a baseline.
     */
    _ptrait(_ptrait... as) {
        this.as = as;
    }


    public boolean is(_ptrait ptrait) {
        return false;
    }

    public EnumSet<_ptrait> getPrimaryTraits() {
        return null;
    }

    public int $as$extent$offset$int() {
        return 0;
    }

    public int $as$extent$length$int() {
        return 0;
    }

    public _ref<?> $() {
        return ref;
    }

    public _ref reify(_ptr void$) {
        return ref;  //todo: verify for a purpose
    }



    public static void main(String[] a) {

        final Class<_ptrait> ptraitClass = _ptrait.class;
        final Package aPackage = ptraitClass.getPackage();


        classMeta(ptraitClass);

        final Enum<?>[] constants = ptraitClass.getEnumConstants();

        for (
                Enum<?> constant
                : constants)

        {

            final Class<? extends Enum<?>> declaringClass = constant.getDeclaringClass();
            final int modifiers = declaringClass.getModifiers();
            final String s = Modifier.toString(modifiers);
//            System.err.println(" + " + s);


        }


    }

    private static void classMeta(Class ptraitClass) {
        if (null == ptraitClass) return;
        final TypeVariable[] typeVariables = ptraitClass.getTypeParameters();
        System.err.println("" + ptraitClass);
//        System.err.println("" + Arrays.toString(typeVariables));
        for (TypeVariable typeVariable : typeVariables) {
            System.err.println("" + typeVariable);
            final Type[] bounds = typeVariable.getBounds();

            for (Type bound : bounds) {
                System.err.println("" + bound);
                if (bound instanceof ParameterizedType)
                    System.err.println("" + bound );//+ "" + Arrays.toString(((ParameterizedType)
                            //(bound)).getActualTypeArguments()));
                else {
                    final Type type = ((Class) bound).getSuperclass();
                    classMeta((Class) type);
                }
            }
            final GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
            final TypeVariable<?>[] typeVariables1 = genericDeclaration.getTypeParameters();
            for (TypeVariable<?> variable : typeVariables1) {
                System.err.println("" + variable);
            }
        }

        for (Type type : ptraitClass.getInterfaces()) {
            System.err.println("" + type);
            if (type instanceof Class)
                classMeta((Class) type);
            else {
                final Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();

                for (Type actualTypeArgument : actualTypeArguments) {
                    System.err.println("" + actualTypeArgument);
                }
            }
        }

    }


    public EnumSet<_ptrait> getTraits() {
        if (traits == null) {
            traits = EnumSet.of(this);
            Collections.addAll(traits, as);

            System.err.println("" + String.valueOf(traits) + "");
            final String[] strings = name().split("$");
            for (String string : strings) {
                _ptrait ptrait = null;
                try {
                    ptrait = valueOf("$" + string);
                    traits.addAll(ptrait.getTraits());
                } catch (IllegalArgumentException e) {
                }
            }
        }
        System.err.println("" + String.valueOf(traits) + "");
        return this.traits;
    }
}



