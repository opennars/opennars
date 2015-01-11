package nars.jwam.compiler;

/**
 * These tokens are containers with data that is needed to decide which
 * instruction to take.
 *
 * @author Bas Testerink, Utrecht University, The Netherlands
 *
 */
public class Token {

    public int /*type, */perm_vars_afterwards, register1, register2, cut_var, index;
    public boolean is_perm, is_unsafe, part_of_last_goal, is_cut, is_deep, is_neck, is_argument, is_built_in2, make_get_level;

    public Token(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return Integer.toString(index);
    }
    
    
}
