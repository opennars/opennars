package nars.jprolog.lang;
/**
 * The <code>Arithmetic</code> class contains a method
 * for evaluating arithmetic expressions.<br>
 * This class is mainly used by the builtin predicate <code>is/2</code>.
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class Arithmetic {
    static SymbolTerm SYM_RANDOM       = SymbolTerm.makeSymbol("random");
    static SymbolTerm SYM_PI           = SymbolTerm.makeSymbol("pi");
    static SymbolTerm SYM_E            = SymbolTerm.makeSymbol("e");
    static SymbolTerm SYM_ADD_1        = SymbolTerm.makeSymbol("+", 1);
    static SymbolTerm SYM_NEGATE_1     = SymbolTerm.makeSymbol("-", 1);
    static SymbolTerm SYM_ADD_2        = SymbolTerm.makeSymbol("+", 2);
    static SymbolTerm SYM_SUBTRACT_2   = SymbolTerm.makeSymbol("-", 2);
    static SymbolTerm SYM_MULTIPLY_2   = SymbolTerm.makeSymbol("*", 2);
    static SymbolTerm SYM_DIVIDE_2     = SymbolTerm.makeSymbol("/", 2);
    static SymbolTerm SYM_INTDIVIDE_2  = SymbolTerm.makeSymbol("//", 2);
    static SymbolTerm SYM_MOD_2        = SymbolTerm.makeSymbol("mod", 2);
    static SymbolTerm SYM_SHIFTLEFT_2  = SymbolTerm.makeSymbol("<<", 2);
    static SymbolTerm SYM_SHIFTRIGHT_2 = SymbolTerm.makeSymbol(">>", 2);
    static SymbolTerm SYM_NOT_1        = SymbolTerm.makeSymbol("\\", 1);
    static SymbolTerm SYM_AND_2        = SymbolTerm.makeSymbol("/\\", 2);
    static SymbolTerm SYM_OR_2         = SymbolTerm.makeSymbol("\\/", 2);
    static SymbolTerm SYM_XOR_2        = SymbolTerm.makeSymbol("#", 2);
    static SymbolTerm SYM_POW_2        = SymbolTerm.makeSymbol("**", 2);
    static SymbolTerm SYM_ABS_1        = SymbolTerm.makeSymbol("abs", 1);
    static SymbolTerm SYM_ACOS_1       = SymbolTerm.makeSymbol("acos", 1);
    static SymbolTerm SYM_ASIN_1       = SymbolTerm.makeSymbol("asin", 1);
    static SymbolTerm SYM_ATAN_1       = SymbolTerm.makeSymbol("atan", 1);
    static SymbolTerm SYM_CEIL_1       = SymbolTerm.makeSymbol("ceiling", 1);
    static SymbolTerm SYM_COS_1        = SymbolTerm.makeSymbol("cos", 1);
    static SymbolTerm SYM_DEGREES_1    = SymbolTerm.makeSymbol("degrees", 1);
    static SymbolTerm SYM_EXP_1        = SymbolTerm.makeSymbol("exp", 1);
    static SymbolTerm SYM_FLOOR_1      = SymbolTerm.makeSymbol("floor", 1);
    static SymbolTerm SYM_LOG_1        = SymbolTerm.makeSymbol("log", 1);
    static SymbolTerm SYM_MAX_2        = SymbolTerm.makeSymbol("max", 2);
    static SymbolTerm SYM_MIN_2        = SymbolTerm.makeSymbol("min", 2);
    static SymbolTerm SYM_RADIANS_1    = SymbolTerm.makeSymbol("radians", 1);
    static SymbolTerm SYM_RINT_1       = SymbolTerm.makeSymbol("rint", 1);
    static SymbolTerm SYM_ROUND_1      = SymbolTerm.makeSymbol("round", 1);
    static SymbolTerm SYM_SIN_1        = SymbolTerm.makeSymbol("sin", 1);
    static SymbolTerm SYM_SQRT_1       = SymbolTerm.makeSymbol("sqrt", 1);
    static SymbolTerm SYM_TAN_1        = SymbolTerm.makeSymbol("tan", 1);
    static SymbolTerm SYM_REM_2        = SymbolTerm.makeSymbol("rem", 2);
    static SymbolTerm SYM_SIGN_1       = SymbolTerm.makeSymbol("sign", 1);
    static SymbolTerm SYM_FLOAT_1      = SymbolTerm.makeSymbol("float", 1);
    static SymbolTerm SYM_INTPART_1    = SymbolTerm.makeSymbol("float_integer_part", 1);
    static SymbolTerm SYM_FRACTPART_1  = SymbolTerm.makeSymbol("float_fractional_part", 1);
    static SymbolTerm SYM_TRUNCATE_1   = SymbolTerm.makeSymbol("truncate", 1);

	/**
	 * Evaluates <code>_t</code> as an arithmetic expression,
	 * and returns the resulting number as <code>NumberTerm</code>.
	 *
	 * @throws PInstantiationException if <code>_t</code> contains unbound variables.
	 * @throws IllegalDomainException  if <code>_t</code> is not an arithmetic expression.
	 */
	public static NumberTerm evaluate(Term _t)
			throws PInstantiationException, IllegalDomainException {
		while (true) {
			Term t = _t.dereference();

			if (t.isVariable())
				throw new PInstantiationException();
			else if (t.isInteger())
				return (IntegerTerm) t;
			else if (t.isDouble())
				return (DoubleTerm) t;
			else if (t.equals(SYM_RANDOM))
				return new DoubleTerm(Math.random());
			else if (t.equals(SYM_PI))
				return new DoubleTerm(Math.PI);
			else if (t.equals(SYM_E))
				return new DoubleTerm(Math.E);
			else if (t.isList()) {
				_t = ((ListTerm) t).car();
				continue;
			} else if (!t.isStructure())
				throw new IllegalDomainException("arithmetic expression", t);

			SymbolTerm func = ((StructureTerm) t).functor();
			Term[] args = ((StructureTerm) t).args();

			if (func.equals(SYM_ADD_1)) {
				_t = args[0];
			} else if (func.equals(SYM_NEGATE_1))
				return evaluate(args[0]).negate();
			else if (func.equals(SYM_ADD_2))
				return evaluate(args[0]).add(evaluate(args[1]));
			else if (func.equals(SYM_SUBTRACT_2))
				return evaluate(args[0]).subtract(evaluate(args[1]));
			else if (func.equals(SYM_MULTIPLY_2))
				return evaluate(args[0]).multiply(evaluate(args[1]));
			else if (func.equals(SYM_INTDIVIDE_2))
				return evaluate(args[0]).intDivide(evaluate(args[1]));
			else if (func.equals(SYM_DIVIDE_2))
				return evaluate(args[0]).divide(evaluate(args[1]));
			else if (func.equals(SYM_MOD_2))
				return evaluate(args[0]).mod(evaluate(args[1]));
			else if (func.equals(SYM_REM_2))
				return evaluate(args[0]).mod(evaluate(args[1]));
			else if (func.equals(SYM_AND_2))
				return evaluate(args[0]).and(evaluate(args[1]));
			else if (func.equals(SYM_OR_2))
				return evaluate(args[0]).or(evaluate(args[1]));
			else if (func.equals(SYM_XOR_2))
				return evaluate(args[0]).xor(evaluate(args[1]));
			else if (func.equals(SYM_NOT_1))
				return evaluate(args[0]).not();
			else if (func.equals(SYM_SHIFTLEFT_2))
				return evaluate(args[0]).shiftLeft(evaluate(args[1]));
			else if (func.equals(SYM_SHIFTRIGHT_2))
				return evaluate(args[0]).shiftRight(evaluate(args[1]));
			else if (func.equals(SYM_ABS_1))
				return evaluate(args[0]).abs();
			else if (func.equals(SYM_MIN_2))
				return evaluate(args[0]).min(evaluate(args[1]));
			else if (func.equals(SYM_MAX_2))
				return evaluate(args[0]).max(evaluate(args[1]));
			else if (func.equals(SYM_RINT_1))
				return evaluate(args[0]).rint();
			else if (func.equals(SYM_ROUND_1))
				return evaluate(args[0]).round();
			else if (func.equals(SYM_FLOOR_1))
				return evaluate(args[0]).floor();
			else if (func.equals(SYM_CEIL_1))
				return evaluate(args[0]).ceil();
			else if (func.equals(SYM_SIN_1))
				return evaluate(args[0]).sin();
			else if (func.equals(SYM_COS_1))
				return evaluate(args[0]).cos();
			else if (func.equals(SYM_TAN_1))
				return evaluate(args[0]).tan();
			else if (func.equals(SYM_ASIN_1))
				return evaluate(args[0]).asin();
			else if (func.equals(SYM_ACOS_1))
				return evaluate(args[0]).acos();
			else if (func.equals(SYM_ATAN_1))
				return evaluate(args[0]).atan();
			else if (func.equals(SYM_SQRT_1))
				return evaluate(args[0]).sqrt();
			else if (func.equals(SYM_LOG_1))
				return evaluate(args[0]).log();
			else if (func.equals(SYM_EXP_1))
				return evaluate(args[0]).exp();
			else if (func.equals(SYM_POW_2))
				return evaluate(args[0]).pow(evaluate(args[1]));
			else if (func.equals(SYM_DEGREES_1))
				return evaluate(args[0]).toDegrees();
			else if (func.equals(SYM_RADIANS_1))
				return evaluate(args[0]).toRadians();
			else if (func.equals(SYM_SIGN_1))
				return evaluate(args[0]).signum();
			else if (func.equals(SYM_FLOAT_1))
				return evaluate(args[0]).toFloat();
			else if (func.equals(SYM_INTPART_1))
				return evaluate(args[0]).floatIntPart();
			else if (func.equals(SYM_FRACTPART_1))
				return evaluate(args[0]).floatFractPart();
			else if (func.equals(SYM_TRUNCATE_1))
				return evaluate(args[0]).truncate();
			else
				throw new IllegalDomainException("arithmetic expression", t);
		}
	}
}
