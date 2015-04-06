# Theory FAQ #

  * How relevant is Pei Wang’s thesis to today’s theory? As of 2013, are there some revisions of ideas, or ”just” deepening of analysis?

Most of the basic ideas in Pei Wang’s thesis (1995) and first book (2006) remain in today's NARS, though some details (grammar rules, inference rules, implementation, etc.) have be revised. The most comprehensive and up-to-date description of the Non-Axiomatic Logic (i.e., the grammar rules and inference rules implemented in NARS) is Pei Wang's second book (2003).

  * What is the difference between NARS reasoning and OpenCog’s PLN (Probabilistic Logic Network)?

This issue is addressed briefly in a footnote of Wang (2013): "This `[`probability theory conflicts with the assumption of insufficient knowledge and resources`]` is the root of the differences between NAL and Probabilistic Logic Network (PLN) `[`Goertzel et al. (2008)`]`, which is partially based on NAL and partially based on probability theory. Such a mixture is a meta-level inconsistency not allowed in NAL --- to me, it is invalid to treat a measurement as probability in some places of the system, while in other places to process the same measurement using methods that violate the axioms of probability theory."

  * What are the tasks and kind of applications most suited for NARS ?

-- The system has insufficient knowledge and resources to use traditional models (mathematical logic, probability theory, problem-specific algorithm, etc.).

-- At the current stage, all the tasks and solutions must be expressible in Narsese (though in the future NARS can be integrated with various I/O channels).

-- There is a tolerance to the mistakes the system makes at the beginning.

  * Is the NARS inference parallelisable?

Conceptually, NARS can be implemented as a paralleled or distributed system, since its concepts serves as storage and processing units that are independent of each other. However, the current software is not designed to support parallel inference.

  * Is some ”convergence” reached when continuing steps after last input?

# Software FAQ #

  * What support is available for using NARS ?
The mailing list : https://groups.google.com/forum/?fromgroups=#!forum/open-nars
and the IRC chat channel #nars at irc.freenode.net.

  * What importers are there from other data or knowledge formats?
None at the moment, but work is under way in EulerGUI project for a translation from RDF/N3 (Semantic Web standards) into Narsese.
  * Which API is available for embedding NARS in an application?
There is the ReasonerBatch class, which has no dependencies to AWT or Swing classes.
  * Are there code examples of embedding NARS in an application?
Not really, but you can have a look at tests in https://open-nars.googlecode.com/svn/trunk/nars_core/src/main/java and https://open-nars.googlecode.com/svn/trunk/nars_core/src/main/scala
  * Are there some examples for supervised machine learning with NARS ?
  * How to call Java code from NARS?
  * Are there concrete examples of reasoning with NARS, larger and closer to useful things than wiki examples?
There are descriptions of demos for NARS (dbPedia, health, video games, pattern matching, etc) here: http://www.cis.temple.edu/~pwang/demos.html
  * Is it possible to use fancy atomic term syntax like URI’s?
Not at the moment, but work is under way to accept URI’s or abridged URI’s in N3/Turtle style.
  * Is it possible, or even necessary to declare terms and relations like in done in RDF Schema, OWL, UML, or are terms and relations implicitly declared like in Prolog?
NO
  * Is there some special syntax for literals ( numbers, strings, ...) ?
NO
  * Does NARS index strings and identifiers, like Prolog implementations do ?
Yes, in the sense that in NARS all sentences involving a term are referred in a concept named by the term.
  * Are there some stress tests for NARS?
Yes