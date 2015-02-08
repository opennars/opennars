## Jscheme (Scheme (in Java (by Peter Norvig)))

This is the code found at http://norvig.com/jscheme.html. During the first commit it was prepared for execution
in IntelliJ and slightly formatted to more modern coding standards. All of the code committed as "first commit"
was written by Peter Norvig and subject to the http://www.norvig.com/license.html agreement.

## History

Some of the Clojure compiler design ideas can be tracked down to [dotLisp](http://dotlisp.sourceforge.net/dotlisp.htm),
a Lisp interpreter written by Rich Hickey and targeting the Microsoft CLR.

dotLisp design ideas were borrowed from [Jscheme-2002](http://jscheme.sourceforge.net/jscheme/main.html)
(as stated on the dotLisp main project homepage), sharing part of the code at first and ending up being completely
rewritten at the end.

Jscheme 2002 (as I call it to distinguish it from its predecessor) is the "enterprise version" the original prototype
written by Peter Norvig around 1998 that is still online and available today. This Github repo started from the source code
found at http://norvig.com/jscheme.html

## Why?

I'm having fun understanding the Clojure compiler internals and runtime (namely Compiler.java and RT.java) but there's
quite a lot of theory behind, plus optimisations that over time tend to "obfuscate" the code. The few classes contained
in this project follows similar design principles in a much simpler (and commented) form. I think that understanding
this code first will make much easier to understand Clojure internals after, at least in my opinion.

It is also a very good exercise in compiler theory, bootstrapping problems, evaluation and much more. Enjoy.
