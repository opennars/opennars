package inc.glamdring.vtables;

import java.util.EnumSet;

/**
 * vtable has a bag of traits, an array following a bitmap
 */
public interface _vtable<sType extends Enum<sType> & _vtable<? super sType>>  
{

    /**
     * can this be coerced right as ...
     * @param ptrait
     * @return can be coerced to ?
     */
    boolean is(_ptrait ptrait);

    /**
     * a bitset of possible coercion targets 
     * 
     * @return a EnumSet bitmap
     */
    EnumSet<_ptrait> getPrimaryTraits();
    int $as$extent$offset$int();
    int $as$extent$length$int();
    _ref<?> $();
}