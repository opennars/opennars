# A proposal for complete libraryfication of OpenNARS

# Introduction

The past couple of months I've been working (off and on, alas) towards the 
transformation of NARS into a library which may be easily plugged into other
projects. In the process, as always happens, I've become very aware of the
mismatches between the current design of the software, and what is needed in
order to really use NARS as a library, and as a research tool. This document is
my attempt at a new design for OpenNARS which will allow it to be easily 
plugged in to other systems, and also allow it to be dynamically extended so
that we researchers can easily test our ideas. The design described here owes
a lot to Pei's proposal, described in [[Modules]], but goes further to separate 
the various components of an AGI reasoner.

# Motivations

A reasoner is the combination of three distinct components; a language used for
structuring knowledge, a collection of inference rules which describe valid
transformations of that language and a control strategy which manages state and
applies the appropriate inference rules to language statements it selects.

Depending on the situation, different sets of inference rules or different
control strategies may be appropriate. In a research setting, it is desirable
that we may easily change the rules being used (so that we might investigate
new ones) and the control strategy; preferably at run-time. This allows us to
test several different combinations of rules and control simultaneously against
uncertain environments like the real world. In a more practical setting, it
becomes desirable to easily specify 'profiles' which are adapted to a
particular situation. Not every application needs every sort of inference from
NAL-1 to NAL-8, and having the engine generate such inferences is needlessly
wasteful of time and space.

The way that a reasoner may be used is also in need of abstraction. As Pei
points out, there are lots of sources of knowledge and action which a given
reasoner may be connected to. It should be a matter of simple gluing (rather
than subclassing) to configure and reconfigure a system to be connected in a
different fashion to the outside environment.

This design is my take on satisfying these requirements. Producing an
implementation of such a system is far from trivial, but a good design document
definitely helps. The specific implementation ideas are documented towards the
end of this note.

# Components

OpenNARS will consist of five main types of component, which will form the top
level packages for the system. These are:

- Language ({{{com.googlecode.opennars.language}}})
- Inference rules ({{{com.googlecode.opennars.rules}}})
- Control strategies ({{{com.googlecode.opennars.strategies}}})
- Communication channels ({{{com.googlecode.opennars.channels}}})
- Core reasoner ({{{com.googlecode.opennars.core}}})

The language components are further divided into logical parts of the language
(in {{{com.googlecode.opennars.language.logic}}}) and command and control parts (in
{{{com.googlecode.opennars.language.command}}}). The top level language package
will contain the core components of a language (with abstract classes for
terms, atoms, compounds, commands, etc.) which may then be extended by specific
sub-packages. It is expected that each level of NAL will gain its own
sub-package ({{{com.googlecode.opennars.language.logic.nal1}}}, say.) The language
classes will make heavy use of the visitor design pattern, to completely
separate the structural from the algorithmic. No inferencing or other reasoning
details will be included in the language classes; these will be handled by the
writing of specific visitors for each task. This will greatly increase the
flexibility of the system, specifically by allowing channels to bandy around
language objects without them being tied to a specific memory or rule set as
they are now.

The inference rules package is divided into a number of sub-packages
corresponding to different rule-sets (for example, the rules of NAL-1 are kept
separate from the rules of NAL-2.) The top level package for this component is
charged with holding abstract classes and interfaces for inference rules, and
the core library of things such as truth values.

The control strategies package includes a sub-package for each control strategy
which is implemented. At present, there will be only one; the bag strategy that
is described in the literature. Other strategies are possible and will be added
in time.

The communication channels package includes all the classes and interfaces that
are responsible for handling communications between the core and the outside
world. An account of some of the possible forms these channels may take is in
[[Modules]]. Unlike that account however, this design doesn't use the notion of
a unique channel ID apart from the reference to the channel object. Likewise,
channel type names are simply the fully qualified names of the respective class
rather than something beyond this. It is the responsibility of the library user
to handle the creation of a reasoner with a given rule set and strategy, and
the connection of channels to this reasoner. Whilst there is some potential for
automation of this, I feel that this is out-of-scope for a simple library
project. Existing architectures, such as the OSGi, may be more appropriate and
there are so many options it seems wise not to pick one and force our users to
use it.

The core reasoner package is the hub of the library. The classes necessary for
connecting communication with reasoning live in this package. In particular, it
is this package which provides the 'normal' view across all the particular
abstractions and modularisations which are used.

# Implementation Issues

Obviously, this design represents a major change over the existing systems;
both Pei's original system and my library form. In light of the massive changes
which are required (and rule 19 of 'Facts and Fallacies of Software
Engineering' which says that if you need to modify much of an existing system
it is often more efficient to begin again) I propose that we reboot the code
from scratch. I will begin work on implementing this design in a separate
branch in the repository so that people can still make changes to the existing
system. I hope that at some point in the not-too-distant future we'll switch
the redesigned system into the trunk.

Obviously with such a grand design, it is important to identify those parts
where we can get away with reusing an existing component. As with my
implementation of LOAN, which uses the BNFC parser generator, any savings
gained here will be essential for making this design feasible. To that end, I
am considering using tuProlog (a very lightweight Prolog-like system written in
Java) as the core of the inference rules implementation. This brings with it a
co-commitant decision for the languages component; namely that language classes
must ultimately inherit from tuProlog terms, so that they may be constructed
and decomposed by inference rules. The main advantage to this is not only that
we save the energy required to handle rule-tables, etc. (a major source of
difficulty with working with the code at present for me as someone who didn't
write the original code) but that we can also write down our rules in Prolog
(good for clarity and documentation) and potentially re-use them in other ports
without worrying about whether or not a given port is using correct rules.

I feel that it is essential that this effective rewrite start with good
practices. This means heavy use of separation of concerns and, since Java is
an OO language, adherence to OO principles wherever possible. It also means the
extensive use of unit and functional tests. These are not only important for us
as developers, they also increase confidence in the code; particularly
important for commercial third-party developers who may want to use OpenNARS as
an AI system in their projects. It should be a requirement that any new class
come with a unit test suite. It goes without saying, that full Javadoc should
also be required at least for all public classes, methods and fields. Ideally
this would be more than just a one-liner, particularly for important methods
and types. Thorough documentation, especially with examples, will help others
not only use the library, but also understand the code-base. This will help
foster a community of developers of OpenNARS systems. By following good 
common-sense practices, such as pragmatic reuse, thorough documentation and
test-driven development, we will keep the code-base clean and flexible for a
prolonged time and encourage others to feel able to contribute to an exciting
project.  