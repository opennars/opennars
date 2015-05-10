package org.projog.core.function.classify;

import static java.lang.Character.MAX_VALUE;
import static org.projog.core.term.TermUtils.getAtomName;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.projog.core.PredicateKey;
import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.function.io.GetChar;
import org.projog.core.term.Atom;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermUtils;

/* TEST
 %FALSE char_type(a, digit)
 %TRUE char_type(a, lower)
 %FALSE char_type(a, upper)
 %TRUE char_type(a, alpha)
 %TRUE char_type(a, alnum)
 %FALSE char_type(a, white)
 
 %FALSE char_type('A', digit)
 %FALSE char_type('A', lower)
 %TRUE char_type('A', upper)
 %TRUE char_type('A', alpha)
 %TRUE char_type('A', alnum)
 %FALSE char_type('A', white)

 %TRUE char_type('1', digit)
 %FALSE char_type('1', lower)
 %FALSE char_type('1', upper)
 %FALSE char_type('1', alpha)
 %TRUE char_type('1', alnum)
 %FALSE char_type('1', white)
 
 %FALSE char_type(' ', digit)
 %FALSE char_type(' ', lower)
 %FALSE char_type(' ', upper)
 %FALSE char_type(' ', alpha)
 %FALSE char_type(' ', alnum)
 %TRUE char_type(' ', white)
 
 %FALSE char_type('\t ', digit)
 %FALSE char_type('\t', lower)
 %FALSE char_type('\t', upper)
 %FALSE char_type('\t', alpha)
 %FALSE char_type('\t', alnum)
 %TRUE char_type('\t', white)

 %QUERY char_type(z, X)
 %ANSWER X=alnum
 %ANSWER X=alpha
 %ANSWER X=lower
 %NO
 
 %QUERY char_type(X, digit)
 %ANSWER X=0
 %ANSWER X=1
 %ANSWER X=2
 %ANSWER X=3
 %ANSWER X=4
 %ANSWER X=5
 %ANSWER X=6
 %ANSWER X=7
 %ANSWER X=8
 %ANSWER X=9
 %NO

 %QUERY char_type(X, upper)
 %ANSWER X=A
 %ANSWER X=B
 %ANSWER X=C
 %ANSWER X=D
 %ANSWER X=E
 %ANSWER X=F
 %ANSWER X=G
 %ANSWER X=H
 %ANSWER X=I
 %ANSWER X=J
 %ANSWER X=K
 %ANSWER X=L
 %ANSWER X=M
 %ANSWER X=N
 %ANSWER X=O
 %ANSWER X=P
 %ANSWER X=Q
 %ANSWER X=R
 %ANSWER X=S
 %ANSWER X=T
 %ANSWER X=U
 %ANSWER X=V
 %ANSWER X=W
 %ANSWER X=X
 %ANSWER X=Y
 %ANSWER X=Z
 %NO

 %QUERY char_type(X, lower)
 %ANSWER X=a
 %ANSWER X=b
 %ANSWER X=c
 %ANSWER X=d
 %ANSWER X=e
 %ANSWER X=f
 %ANSWER X=g
 %ANSWER X=h
 %ANSWER X=i
 %ANSWER X=j
 %ANSWER X=k
 %ANSWER X=l
 %ANSWER X=m
 %ANSWER X=n
 %ANSWER X=o
 %ANSWER X=p
 %ANSWER X=q
 %ANSWER X=r
 %ANSWER X=s
 %ANSWER X=t
 %ANSWER X=u
 %ANSWER X=v
 %ANSWER X=w
 %ANSWER X=x
 %ANSWER X=y
 %ANSWER X=z
 %NO
 
 %QUERY char_type(X, alnum)
 %ANSWER X=0
 %ANSWER X=1
 %ANSWER X=2
 %ANSWER X=3
 %ANSWER X=4
 %ANSWER X=5
 %ANSWER X=6
 %ANSWER X=7
 %ANSWER X=8
 %ANSWER X=9
 %ANSWER X=A
 %ANSWER X=B
 %ANSWER X=C
 %ANSWER X=D
 %ANSWER X=E
 %ANSWER X=F
 %ANSWER X=G
 %ANSWER X=H
 %ANSWER X=I
 %ANSWER X=J
 %ANSWER X=K
 %ANSWER X=L
 %ANSWER X=M
 %ANSWER X=N
 %ANSWER X=O
 %ANSWER X=P
 %ANSWER X=Q
 %ANSWER X=R
 %ANSWER X=S
 %ANSWER X=T
 %ANSWER X=U
 %ANSWER X=V
 %ANSWER X=W
 %ANSWER X=X
 %ANSWER X=Y
 %ANSWER X=Z
 %ANSWER X=a
 %ANSWER X=b
 %ANSWER X=c
 %ANSWER X=d
 %ANSWER X=e
 %ANSWER X=f
 %ANSWER X=g
 %ANSWER X=h
 %ANSWER X=i
 %ANSWER X=j
 %ANSWER X=k
 %ANSWER X=l
 %ANSWER X=m
 %ANSWER X=n
 %ANSWER X=o
 %ANSWER X=p
 %ANSWER X=q
 %ANSWER X=r
 %ANSWER X=s
 %ANSWER X=t
 %ANSWER X=u
 %ANSWER X=v
 %ANSWER X=w
 %ANSWER X=x
 %ANSWER X=y
 %ANSWER X=z
 %NO
 
 white_test :- char_type(X, white), write('>'), write(X), write('<'), nl, fail. 
 %QUERY white_test
 %OUTPUT
 % >\t<
 % > <
 %
 %OUTPUT
 %NO
 */
/**
 * <code>char_type(X,Y)</code> - classifies characters.
 * <p>
 * Succeeds if the character represented by <code>X</code> is a member of the character type represented by
 * <code>Y</code>. Supported character types are:
 * <ul>
 * <li><code>digit</code></li>
 * <li><code>upper</code> - upper case letter</li>
 * <li><code>lower</code> - lower case letter</li>
 * <li><code>alpha</code> - letter (upper or lower)</li>
 * <li><code>alnum</code> - letter (upper or lower) or digit</li>
 * <li><code>white</code> - whitespace</li>
 * </ul>
 * </p>
 */
public final class CharType extends AbstractRetryablePredicate {
   private static final Type[] EMPTY_TYPES_ARRAY = new Type[] {};
   private static final Atom[] ALL_CHARACTERS = new Atom[MAX_VALUE + 2];
   static {
      for (int i = -1; i <= MAX_VALUE; i++) {
         ALL_CHARACTERS[i + 1] = new Atom(charToString(i));
      }
   }
   private static final Map<PredicateKey, Type> CHARACTER_TYPES_MAP = new LinkedHashMap<>();
   private static final Type[] CHARACTER_TYPES_ARRAY;
   static {
      // populate CHARACTER_TYPES_MAP

      Set<String> digits = createSetFromRange('0', '9');
      Set<String> upper = createSetFromRange('A', 'Z');
      Set<String> lower = createSetFromRange('a', 'z');

      addType("alnum", digits, upper, lower);
      addType("alpha", upper, lower);
      addType("digit", digits);
      addType("upper", upper);
      addType("lower", lower);
      addType("white", intsToStrings('\t', ' '));

      CHARACTER_TYPES_ARRAY = CHARACTER_TYPES_MAP.values().toArray(new Type[CHARACTER_TYPES_MAP.size()]);
   }

   @SafeVarargs
   private static void addType(String id, Set<String>... charIdxs) {
      Set<String> superSet = new HashSet<String>();
      for (Set<String> s : charIdxs) {
         superSet.addAll(s);
      }
      addType(id, superSet);
   }

   private static void addType(String id, Set<String> charIdxs) {
      Atom a = new Atom(id);
      PredicateKey key = PredicateKey.createForTerm(a);
      Type type = new Type(a, charIdxs);
      CHARACTER_TYPES_MAP.put(key, type);
   }

   private static Set<String> createSetFromRange(int from, int to) {
      int[] range = createRange(from, to);
      return intsToStrings(range);
   }

   private static int[] createRange(int from, int to) {
      int length = to - from + 1; // +1 to be inclusive
      int[] result = new int[length];
      for (int i = 0; i < length; i++) {
         result[i] = from + i;
      }
      return result;
   }

   private static Set<String> intsToStrings(int... ints) {
      Set<String> strings = new HashSet<>();
      for (int i : ints) {
         // +1 as "end of file" (-1) is stored at idx 0
         strings.add(ALL_CHARACTERS[i + 1].getName());
      }
      return strings;
   }

   private final State state;

   public CharType() {
      this.state = null;
   }

   private CharType(State state) {
      this.state = state;
   }

   @Override
   public CharType getPredicate(PTerm character, PTerm type) {
      PTerm[] characters;
      if (character.type().isVariable()) {
         characters = ALL_CHARACTERS;
      } else {
         characters = new PTerm[] {character};
      }
      Type[] characterTypes = {};
      if (type.type().isVariable()) {
         characterTypes = CHARACTER_TYPES_ARRAY;
      } else {
         PredicateKey key = PredicateKey.createForTerm(type);
         Type t = CHARACTER_TYPES_MAP.get(key);
         if (t != null) {
            characterTypes = new Type[] {t};
         } else {
            characters = TermUtils.EMPTY_ARRAY;
            characterTypes = EMPTY_TYPES_ARRAY;
         }
      }
      return new CharType(new State(characters, characterTypes));
   }

   @Override
   public boolean evaluate(PTerm character, PTerm type) {
      while (state.hasNext()) {
         state.next();
         character.backtrack();
         type.backtrack();
         if (character.unify(state.getCharacter()) && state.getType().unify(character, type)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean couldReEvaluationSucceed() {
      return state.hasNext();
   }

   /** @see GetChar#toString(int) */
   private static String charToString(int c) {
      if (c == '\t') {
         return "\\t";
      } else {
         return Character.toString((char) c);
      }
   }

   private static class State {
      final PTerm[] characters;
      final Type[] characterTypes;
      int characterCtr = 0;
      int characterTypeCtr = -1;

      State(PTerm[] characters, Type[] characterTypes) {
         this.characters = characters;
         this.characterTypes = characterTypes;
      }

      boolean hasNext() {
         return characterCtr + 1 < characters.length || characterTypeCtr + 1 < characterTypes.length;
      }

      void next() {
         characterTypeCtr++;
         if (characterTypeCtr == characterTypes.length) {
            characterTypeCtr = 0;
            characterCtr++;
         }
      }

      PTerm getCharacter() {
         return characters[characterCtr];
      }

      Type getType() {
         return characterTypes[characterTypeCtr];
      }
   }

   private static class Type {
      final Atom termId;
      final Set<String> characters;

      Type(Atom termId, Set<String> characters) {
         this.termId = termId;
         this.characters = characters;
      }

      boolean unify(PTerm character, PTerm type) {
         return characters.contains(getAtomName(character)) && type.unify(termId);
      }
   }
}
