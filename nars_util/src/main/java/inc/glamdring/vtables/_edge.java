package inc.glamdring.vtables;

/**
 * edge class.   midpoint between 2 casts
 * <p/>
 * type,edge, delta, coersion points, etc.
 * <p/>
 * User: jim
 * Date: Sep 18, 2008
 * Time: 6:05:14 AM
 */
public interface _edge<l, r> extends _proto<l> {

    /**
     * left type node with induction
     *
     * @param edge copy ctor/factory proto
     * @return shift left
     */
    l demote(_edge<l, r> edge);

    /**
     * right type node with induction
     *
     * @param edge copy ctor/factory proto
     * @return right shift
     */
    r promote(_edge<l, r> edge);

    /**
     * binds two types
     *
     * @param l
     * @param r
     * @return fused arc
     */
    _edge<l, r> bind(l l, r r);

}
/**
 *  
public interface €<Ω, µ> extends _proto<Ω> { Ω Ω(€<Ω, µ> €); µ µ(€<Ω, µ> €); €<Ω, µ> €(Ω Ω, µ µ);}*/