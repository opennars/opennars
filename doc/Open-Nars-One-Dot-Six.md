# New functions of Open-NARS version 1.6.x.


# Introduction

Open-NARS 1.6.x is different from 1.5.x in the following major aspects:
1. In logic, it implements NAL-7, NAL-8, and NAL-9, and therefore completely implements NAL, as specified in [Non-Axiomatic Logic: A Model of Intelligent Reasoning](http://www.worldscientific.com/worldscibooks/10.1142/8665). It is an attempt to re-do what [[Open Nars One Dot Four]] planned to achieve.
1. In control, it refines the automatic resources allocation that has been there from the beginning, and combines it with the voluntary control introduced in NAL-9.
1. In software structure, it absorbs the ideas from previous discussions (such as in [[Abstract Design Proposal]], [[Modules]], [and implementations (the version built by Joe Geldart), as well as the recent discussions.


# Temporal inference (NAL-7)

Beside [http://www.worldscientific.com/worldscibooks/10.1142/8665 Non-Axiomatic Logic: A Model of Intelligent Reasoning](GUIRequest])), temporal reasoning in NARS is also discussed in [Issues in Temporal and Causal Inference](http://www.cis.temple.edu/~pwang/Publication/temporal-causal.pdf). A previous implementation exists as version 1.3.3, with [working examples](https://code.google.com/p/open-nars/source/browse/trunk/nars-dist/Examples-1.3.3/Example-NAL7-abridged.txt).

In package nars.language, the following three classes representing compound terms will be given an optional temporal order among its components: *Implication*, *Equivalence*, and *Conjunction*. This order can be represented either as subclasses, or as an attribute that takes three possible values: -1 for backward, +1 for forward, and 0 for concurrent. The plain text forms of the term connectors are listed in [[Input Output Format]].

In class nars.entity.Stamp, an occurrenceTime records the (observed, remembered or estimated) occurrence time of the event, together with its creation time. Different from the description in the book, the "tense" of a sentence is not stored with a sentence, but is only used at the interface. When a sentence is an input or output, the internal occurrenceTime will be translated to/from the external sense that is explicitly expressed in Narsese, as listed in [[Input Output Format]].

The class nars.io.StringParser will be modified to support this temporal version of Narsese, as specified in the Grammar Rules of NAL-7 (Table 11.1) in the book, as well as implemented as [[Input Output Format]].

As explained in the publications, temporal inference in NARS is designed by processing the logical and the temporal aspects separately. Therefore all the existing rules will be kept, except that the conclusion may contain a temporal term (with temporal order among components), a time interval(similar to the numbers in experience), and/or a temporal truth-value (with an occurrence time).

As specified in the book, the temporal rules are designed in the following way:
1. Each inference rule of IL-6 is extended by using the temporal copulas and connectors to replace the atemporal ones in the premises, then identifying the conclusions where both the logical and the temporal relations can be derived, so as to establish valid inference rules for IL-7.
1. Each inference rule of IL-7 is extended into a strong inference rule in NAL-7, using the same truth-value function as the corresponding rule in NAL-6.
1. Each strong inference rule of NAL-7 suggests one or more weak inference rules according to the reversibility relationship among the types of inference.

Therefore, the temporal term in the conclusion of a strong rule is produced in the following way:
1. The events in each premise are ordered temporally.
1. The two event sequences are merged, if possible.
1. The temporal order of events in the conclusion is marked accordingly.

For a weak rule, the temporal order is the one in the corresponding strong rule.  If the temporal orders among the premises are incompatible or insufficient to decide the order in the conclusion, no derivation will happen. 

Different from the experience, the time interval between terms are represented as a special type of term, named Interval. This term represents the system's "sense of time", which is approximate, as the rounded logarithm function of the number of clock cycles in the interval. That is, Math.round(Math.log(n)). This approximation is used, because otherwise similar intervals will be judged as different, so the corresponding evidence will not be accumulated by the revision rule. Whenever accurate time measurement is needed, the system should not depend on this approximate measurement, but uses an external reference (e.g., clock). The logarithm function may be replaced by a power function, such as Math.sqrt(n). This can be decided later.

Ideally, temporal inference is valid only when the premises are about the same moment, i.e., have the same occurrenceTime or no occurrenceTime (i.e., eternal). However, since occurrenceTime is approximate, a conclusion about one moment (that of the belief) can be projected to another (that of the task), at the cost of a confidence discount. Let *t0* be the current time, and *t1* and *t2* are the occurrenceTime of the premises, then the discount factor is *d* = 1 - |*t1-t2*| / (|*t0-t1*| + |*t0-t2*|), which is in [This factor _d_ is multiplied to the confidence of a promise as a "temporal discount" to project it to the occurrence of the other promise, so as to derive a conclusion about that moment. In this way, if there are conflicting conclusions, the temporally closer one will be preferred by the choice rule.

The conclusion does not always has the same occurrenceTime as a premise. For instance, if _A_ and _A /=> B_ has occurrence time _t_, then the conclusion _B_ should have occurrence time _t+1_. If _A_ and _(&&,A,C) /=> B_ has occurrence time _t_ and _C_ is an Interval term with argument _n_, _B_ should have occurrence time _t_+1+Math.exp(_n_).


# Procedural inference (NAL-8)

Beside [http://www.worldscientific.com/worldscibooks/10.1142/8665 Non-Axiomatic Logic: A Model of Intelligent Reasoning](0,1].), procedural reasoning in NARS is also discussed in [Solving a Problem With or Without a Program](http://www.degruyter.com/view/j/jagi.2012.3.issue-3/v10229-011-0021-5/v10229-011-0021-5.xml?format=INT). A previous implementation exists as version 1.3.3, with [working examples in several files](https://code.google.com/p/open-nars/source/browse/trunk/nars-dist/Examples-1.3.3/), as well as descriptions in [[Procedural Inference]], [[Procedural Examples]], and [[Procedural Learning]].

In nars.entity.Sentence, add a "desire" value, which is truth-value interpreted differently. Furthermore, add "Goal" and "Query" as two types of Sentence, and process them like "Judgment" and "Question", but using desire-value, rather than truth-value.

In all the inference rules, "Query/Goal" and "Judgment/Question" are processed in parallel. Most of the code for this already exist in 1.3.3, and only need to be revised.

Though both Question and Goal are derived by backward inference, there is a major difference between the two: while derived Questions are directly accepted as new tasks, derived Goals do not immediately become tasks. instead, they contribute to the desirability of the corresponding sentences. Only when the desirability of a sentence reach a threshold, does a decision-making rule is triggered to decide whether to pursue the sentence as a goal.

Goals are eventually achieved by the execution of operations. An "operation" package will be introduced, with each operator defined as a class, with an "execute" method that takes a task as argument, which provides the arguments of the operation. In Memory, a HashMap<String, Operator> will be used to remember the operators. When a task corresponds to an operation, the execute method is called.

This part of the system will be further extended into a "universal sensorimotor interface", so that the commands or functions of other systems or decides can be registered in NARS, and called by it. With each operator, some initial knowledge about its preconditions and consequences should be provided to the system.


# Introspective inference (NAL-9)

A previous preliminary implementation exists as version 1.4.0, and explained briefly in [[Open Nars One Dot Four]].

This layer of NAL will be mainly implemented as a set of mental operations whose major consequences are within the system itself. These operations carries out various types of self-monitoring and self-control.

As planned for version 1.4, a few indicators are used to measure the current status of the system as a whole:
- *happyness*: a weighted average satisfaction values (i.e., the quality of its current solution) of the recently pursued goals
- *busyness*: a weighted average priority values of the recently processed tasks

Here "recentness" is defined as a system parameter, which can indicate either an attention span or the proportion of the newest item in the overall measurement. Probably the latter is better.

The current value of either *happyness* or *busyness* can be detected by a *feeling* operations, and the results become parts of the system's *inner experience*, and will be processed in the same way as the external experience coming from the outside.

Other monitoring operations include:
- anticipate: get a task from the task buffer matching a given pattern
- know: find the truth-value of a statement
- assess:
find the desire-value of a statement

The following control operations will be implemented:
- believe: create a judgment with a given statement
- want: create a goal with a given statement
- wonder: create a question with a given statement
- evaluate: create a query with a given statement
- doubt: decrease the confidence of a belief
- hesitate: decrease the confidence of a goal
- remind: activate a concept
- consider: do a step of inference on a concept
- infer: carry out an inference step with the given statements as premises

The name of a mental operator only roughly corresponds to its meaning. The exact meaning of an operator is revealed by its preconditions and consequences.

There are some other mental operations under consideration, but will not be attempted in this version.


# Memory and control

Beside the main memory and existing task buffers for input tasks and tasks with novel (no-existing) concepts, new storage structures will be added for
- recent events, as a Queue
- operation registry, as a HashMap
- global indicators, including *happyness* and *busyness*

The control mechanism will be revised to allow the mental operations to change the default working procedure and the resource allocation policy.


# Software structure

Since the main purpose of this version is still to test the conceptual design of NARS, especially Narsese and NAL, the software development should focus in the functionality of the design, rather than on the quality of code as asked for practical applications. Changes purely for software design reasons will be postponed to future versions.

One task is to analyze and compare the versions of open-nars, and merge them into a single trunk at the project website, with optional parts that are clearly documented.

One optional task is to explore the structure for NARS to call other systems, as well as for other systems to call NARS for reasoning service.