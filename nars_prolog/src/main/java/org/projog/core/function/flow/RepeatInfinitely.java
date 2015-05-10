package org.projog.core.function.flow;

import org.projog.core.function.AbstractRetryablePredicate;

/* TEST
 write_to_file(X) :-
    open('io_test.tmp', write, Z), 
    set_output(Z), 
    write(X), 
    close(Z), 
    set_output('user_output').

 read_from_file :-
    open('io_test.tmp', read, Z), 
    set_input(Z), 
    print_first_sentence, 
    close(Z).
   
 print_first_sentence :- 
    repeat, 
    get_char(C), 
    write(C), 
    C=='.', 
    !.
	
 %TRUE write_to_file('The first sentence. The second sentence.')
 
 %QUERY read_from_file
 %OUTPUT The first sentence.
 %ANSWER/
 %NO
 */
/**
 * <code>repeat</code>- always succeeds.
 * <p>
 * <code>repeat</code> <i>always</i> succeeds even when an attempt is made to re-satisfy it.
 * </p>
 */
public final class RepeatInfinitely extends AbstractRetryablePredicate {
   @Override
   public boolean evaluate() {
      return true;
   }

   @Override
   public RepeatInfinitely getPredicate() {
      return this;
   }
}