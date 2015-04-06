> ### OpenNarsOneDotSix  
> Functions and remaining issues of Open-NARS 1.6.x.

***
### Introduction

Open-NARS 1.6.x is different from 1.5.x in the following major aspects:

In logic, it implements NAL-7, NAL-8, and NAL-9, and therefore completely implements NAL, as specified in [Non-Axiomatic Logic: A Model of Intelligent Reasoning](http://www.worldscientific.com/worldscibooks/10.1142/8665).
It introduces some new ideas to be further explored, such as [Perception In NARS](https://github.com/opennars/opennars/wiki/Perception-In-NARS) and [Plugins](https://github.com/opennars/opennars/wiki/Plugins).
In [control](https://github.com/opennars/opennars/wiki/Inference-Control), it refines the automatic resources allocation that has been there from the beginning, and combines it with the voluntary control introduced in NAL-9.
In software structure, it absorbs the ideas from previous discussions, as well as the recent discussions. The program has a completely new GUI and very different internal structure.

***
### Temporal Inference (NAL-7)

#### Representation of temporal information

Beside [Non-Axiomatic Logic: A Model of Intelligent Reasoning](http://www.worldscientific.com/worldscibooks/10.1142/8665), temporal reasoning in NARS is also discussed in [Issues in Temporal and Causal Inference](http://www.cis.temple.edu/~pwang/Publication/temporal-causal.pdf). A previous implementation exists as version 1.3.3, with working examples.

Beside using terms whose meanings are explicitly temporal, Narsese is extended to allow temporal order among components in certain CompoundTerm, as well as an "occurrence time stamp" on a truth-value.

In package nars.language, the following three classes representing compound terms will be given an optional temporal order among its components: Implication, Equivalence, and Conjunction. This order can be represented either as subclasses, or as an attribute that takes three possible values: -1 for backward, +1 for forward, 0 for concurrent, and 2 for "no temporal order". These numbers are selected to make the later processing easy and natural, so should not be changed unless after careful consideration. The plain text forms of the term connectors are listed in [Input Output Format](https://github.com/opennars/opennars/wiki/Input-Output-Format).

In class nars.entity.Stamp, an occurrenceTime records the (observed, remembered, or estimated) occurrence time of the event, together with its creation time. Different from the description in the book, the "tense" of a sentence is not stored with a sentence, but is only used at the interface. When a sentence is an input or output, the internal occurrenceTime will be translated to/from the external sense that is explicitly expressed in Narsese, as listed in [Input Output Format](https://github.com/opennars/opennars/wiki/Input-Output-Format).

A system parameter DURATION is used to define the "present tense" as for events with an occurrenceTime in the [-DURATION, DURATION] neighborhood of the current time, as given by the system's internal clock. It's current default value is 5. The previous idea is to take 1 as its value, so "presently" means the current working cycle, but it seems too restrictive. For an input judgment, if its tense is "present", its occurrenceTime will be set to the current time on the internal clock, "past" is mapped to "current time - DURATION", and "future" to "current time + DURATION". If no tense is used, the occurrenceTime takes a special value ETERNAL. The reverse mapping is used for output.

####Inference on temporal order

As explained in the publications, temporal inference in NARS is designed by processing the logical and the temporal aspects separately. Therefore all the existing rules will be kept, except that the conclusion may contain a temporal term (with temporal order among components), a time interval (similar to the numbers in experience), and/or a temporal truth-value (with an occurrence time).

The temporal order in CompoundTerms is processed in the following way:

1. As described in the book, each inference rule of NAL-6 is extended by using the temporal copulas and connectors to replace the atemporal ones in the premises, then identifying the conclusions where both the logical and the temporal relations can be derived, so as to establish valid inference rules for NAL-7.

2. Different from what is hinted in the book, the temporal order is taken to be binary, not multi-valued, so the matter of degree in the truth-value is completely caused by the logical factor, not the temporal factor. For this reason, in all inference rules (both strong and weak), the temporal order among the terms in the conclusion is the same order as those terms in the premises. If no such a temporal order can be decided in a certain combination of the terms, no conclusion is derived.

#####-Inference on occurrence time
In most rules, the occurrenceTime is processed independent of the other aspects of the rule. There are the following cases:

* If both premises are ETERNAL, so is the conclusion. Therefore all the inference rules defined in the lower layers remain valid.

* If the task is "tensed" (i.e., with an occurrenceTime that is not ETERNAL) but the belief is not, then the conclusion has the same occurrenceTime as the tensed premise.

* If the task is not tensed but the belief is, then an eternalization rule is used to take the belief as providing evidence for the sentence in the task.

* If both premises are tensed, then the belief is "projected" to the occurrenceTime of the task. Ideally, temporal inference is valid only when the premises are about the same moment, i.e., have the same occurrenceTime or no occurrenceTime (i.e., eternal). However, since occurrenceTime is an approximation and the system is adaptive, a conclusion about one moment (that of the belief) can be projected to another (that of the task), at the cost of a confidence discount. Let t0 be the current time, and t1 and t2 are the occurrenceTime of the premises, then the discount factor is d = 1 - |t1-t2| / (|t0-t1| + |t0-t2|), which is in [0,1]. This factor d is multiplied to the confidence of a promise as a "temporal discount" to project it to the occurrence of the other promise, so as to derive a conclusion about that moment. In this way, if there are conflicting conclusions, the temporally closer one will be preferred by the choice rule.

Normally, if the premises are "tensed", so is the conclusion --- the knowledge derived about a moment is only valid for that moment. It is through "eternalization" that an eternal conclusion is arrived, at the cost of a confidence lost.

In principle, all inference rules should take temporal information into account. However, since an inference rule may be implemented as the cooperation of multiple methods, not all of them will be responsible to check temporal information.

#####-Temporal induction and detachment
An inference where temporal order and occurrence time are tangled is temporal induction and its reverse, detachment (as special case of deduction and abduction).

In NARS, temporal induction refers to the situation where events e1 and e2 are observed as occurring in succession, with a time interval in between with a length of n clock cycles. As far as this sequence of events is noticed by the system, an inductive conclusion (&/,e1,interval(n)) =/> e2 is derived.

This rule is one of the exceptions of the "generalized syllogistic" pattern in NAL inference rules, which requires the premises to have a common term. This pattern guarantees the semantic relevance among the statements involved, and also greatly reduced the number of legal combinations of premises. The current exception is allowed, because the two events are related temporally, though not semantically, and in future works on sensation organs similar exceptions will be allowed for spatially related events to be combined into compound events without shared components. This rule also includes variable introduction. If e1 and e2 have common subject or predicate, an independent variable will be introduced in the conclusion.

**Issue:** There may be other forms of variable introduction where the shared term is neither the common subject nor the common predicate.

**Issue:** Whether to derive conclusions like e2 =\> (&/,e1,interval(n))?

**Issue:** Though in principle temporal induction can happen between any two tensed judgments, to do that exhaustively is not affordable. Beside what has been implemented, the rule may be invoked (1) as question driven, too, in response to e1 =/> e2? or similar questions, and (2) exhaustively between events in a buffer (the system's "flow of consciousness") that contains "active events", which is separated from the current task buffers in that it only contains significant events after preprocessing. In this way, non-event tasks and trivial (i.e., anticipated or irrelevant) events will not be involved.

As for the time interval n between the two events, to simply derive e1 =/> e2 is correct, but it does not capture the temporal information about the interval. To use another event to represent the interval (as tried in version 1.3.3) is too accurate to be useful, because (&/,e1,interval(100)) =/> e2 and (&/,e1,interval(101)) =/> e2 probably should be merged by the revision rule, since the difference between the two numbers are negligible. For this consideration, in the current implementation, the interval is represented by a special type of term, which approximately record m = log(n) as an integer. With such a belief as premise, the detachment rule will do deduction with e1, and abduction with e2, and the occurrenceTime of the conclusion is calculated from that of the other event, plus or minus exp(m) to approximately recover the n value. Whether the log/exp functions should be replaced by power functions (such as sqrt/square) is a question to be answered later. As in other places, whenever an accurate time interval is needed, it is always possible to replace the approximate time interval by a better defined event, so that is not an issue.

Since the interval measurement is subjective, it will not appear in the system's communication language, but is used in internal representation only. In the future, different intervals will be associated with different temporal terms, such as "after a while", "hours later", etc., via learning, so as to be expressible in communication.

***
### Procedural Inference (NAL-8)
Beside [Non-Axiomatic Logic: A Model of Intelligent Reasoning](http://www.worldscientific.com/worldscibooks/10.1142/8665), procedural reasoning in NARS is also discussed in [Solving a Problem With or Without a Program](http://www.degruyter.com/view/j/jagi.2012.3.issue-3/v10229-011-0021-5/v10229-011-0021-5.xml?format=INT). A previous implementation exists as version 1.3.3, with working examples in several files, as well as descriptions in [Procedural Learning](https://github.com/opennars/opennars/wiki/Procedural-Learning).

####Inference on goals

In nars.entity.Sentence, a "desire" value is added, which is truth-value interpreted differently. The desire-value of statement S is the truth-value of S==>D, where D is a virtual statement representing a desired situation. Furthermore, add "Goal" and "Query" as two types of Sentence, and process them like "Judgment" and "Question", but using desire-value, rather than truth-value.

In all the inference rules, "Query/Goal" and "Judgment/Question" are processed in parallel. Most of the code for this already exist in 1.3.3, and only minor revisions are made.

Given the reversibility of the inference rules of NAL, the derivation of Goals and Quests are basically the existing rules, except that the desire-values are calculated using different functions, according to the above correspondence between truth-value and desire-value.

Though both Question and Goal are derived by backward inference, there is a major difference between the two: while derived Questions are directly accepted as new tasks, derived Goals do not immediately become tasks. instead, they contribute to the desirability of the corresponding sentences. Only when the desirability of a sentence reach a threshold, does a decision-making rule is triggered to decide whether to pursue the sentence as a goal.

Issue: The inference on Quest, as Question on desire-values, has not been carefully evaluated. In particular, the budget-value functions need to be properly selected in each rule, since in the same method, the selection may be different between Quest and Question on truth-value.

####Inference on operations

Goals are eventually achieved by the execution of operations. An "operation" package is introduced, with each operator defined as a class, with an "execute" method that takes a list of arguments for the operation. In Memory, a HashMap<String, Operator> is used to associate the operators with their names. When a goal corresponds to an operation, the execute method of its operator is called.

An operation with acquired operator oper and arguments args is internally represented as an Inheritance statement (*, SELF, args) --> oper, though at the I/O interface it is displayed as (oper, args). Here SELF is a special term representing the system itself.

**Issue:** Technically, (*, SELF, args) --> oper and (*, args, SELF) --> oper both work as the internal representation of an operation. The former looks more natural (so is used in the book), while the latter is slightly more efficient (so is used in the code). A final decision will need to be made to keep the descriptions consistent.

The key of procedural learning is the generation of "compound operations". The major structures compound operations are sequential, parallel, conditional, and recursive.

**Issue:** Do all of the four fully covered in the current code? Does recursive operations need special treatment, or can be handled as special cases of conditional statements?

**Issue:** Should "compound operations" be clearly separated from "compound terms", so that they can be directly executed, without going through the intermediate inference steps?

####Operational interface

A "universal sensorimotor interface" is provided, where the commands or functions of other systems or decides can be registered in NARS, and called by it. With each operator, some initial knowledge about its preconditions and consequences should be provided to the system, so inference can be carried out on it.

As an example, a nars.operator.math package is defined to contain some simple math operators. Currently there are two operators, which can be called as "(^count, {a, b, c}, #x)!" and "(^add, 2, 3, #x)!", where #x will be replaced by the result after the execution, as "(^count, {a, b, c}, 3)." and "(^add, 2, 3, 5)." To embed them into knowledge, the input arguments should be independent variables, such as "... (^count, $y, #x) ..." and "... (^add, $y, $z, #x) ...". In the future, each group of optional operators should be kept in a sub-package of nars.operator, and each class should extend Operator.java, as well as to add a line to call registerOperator in its super class.

In summary, there will be three types of operators in NARS that share the format (oper, args):

* Native operators, the connectors of CompoundTerms that are directly recognized and processed by the grammar rules and inference rules

* Standard operators, the operators to be introduced in NAL-9, and equipped in every normal NARS implementation

* Optional operators, the specific operators that turn a standard NARS into a customized NARS+ described in the book

The first two types will be fixed, while the last is extendable. For more details, see [Plugins](https://github.com/opennars/opennars/wiki/Plugins).

***
### Introspective Inference (NAL-9)

This layer of NAL will be mainly implemented as a set of mental operations whose major consequences are within the system itself. These operations carries out various types of self-monitoring and self-control.

####Deliberate control

The package nars.operator.mental contains operators that allow the system to deliberately override the automatic inference control mechanism:

* task creation: This group of operators each takes a Term as argument, and uses it as content to create a new task to be processed: ^believe for judgment, ^want for goal, ^wonder for question, and ^evaluate for query.

* truth-value/desire-value correction: The operators ^doubt and ^hesitate each takes a Term as argument, find the corresponding Concept, then reduce the confidence by Parameters.DISCOUNT_RATE of all the truth-values and desire-values, respectively. These operations are used when the system realized that some previous evidence is unreliable or unjustified. Please note that no operator is needed to increase the confidence value, because it can be achieved by the revision rule with new evidence.

* compound compiling: The operators ^name and ^abbreviate both builds a similarity relation with high confidence between a compound term and an atomic term, so as to reduce the syntactic complexity of the corresponding concept. Their difference is that ^name takes a given atomic term as the second argument, while ^abbreviate makes a new internal name.

* concept activation: The operators ^remind increases the priority of a specified concept; ^consider directly carries out an inference cycle on a specified concept.

* operator binding: The operators ^register add a new operator when the system is running.

The name of a mental operator only roughly corresponds to its meaning. The exact meaning of an operator is revealed by its preconditions and consequences.

These mental operators are introduced for experimental purpose. The details in each of them may be revised, and other operators may be introduced. In general, each operator should be relatively simple and meaningful. It is neither necessary nor possible for the system to explicitly manage all its internal actions, so it is not a good idea to define a large number of mental operators.

####Feeling and emotion
Feeling and emotion allow the system to appraise individual items and the overall situation, with respect to the system's goals, so as to make proper response.

This appraisal process starts from the given (input or implant) goals. The desire-value of these goals represent drives and values imposed on the system by its designer or user. From these given goals, plus the system's beliefs, the derived goals are generated, which is biased by the system's own experience.

Each Goal (as a subclass of Task) has a satisfaction value, which is the quality of the goal's current solution (i.e., the statement that best matches its content), so measures the extent to which the goal has already been satisfied.

There is an overall satisfaction value that measures the extent to which the existing goals have been satisfied, so it measures the system's appraisal of the overall situation. This value is updated after each working cycle by taking a weighted average of its previous value and a new value from the goal processed in the cycle (if there is one) weighted by the priority of the goal. In this way, this satisfaction value represents the appraisal of the system to the "recent" situation.

This appraisal can be felt by the system itself. When the satisfaction is beyond the neutral zone (around 0.5, defined by a system parameter), a "feeling" operator is triggered to represent an event "<{SELF} --> [SATISFIED]>. :|:" as part of the system's internal experience, with the truth-value and priority-value determined by the satisfaction value. In this way, the appraisal enters the system consciousness, and is processed together with the system's other experienced events. Combined with other information, various feelings can be generated.

Finally, the desire-value in statements will be extended to all concepts. While a statement's desire-value will still be mainly determined by its relations with the goals, for the other concepts their desire-values will be mainly determined by their relation to the overall satisfaction of the system, rather than about the achieving of individual goals. At the end of each working cycle, the desire-value of the fired concept is adjusted by the current satisfaction value (again using a weighted average). So, in the long run, the desire-value of a concept indicates the extent to which its firing is associated with positive emotions of the system, which provides a rough summary with the relation between the concept and the goals.

In summary, at any moment the system's appraisal is represented at two levels: at the "unconscious" (Java) level, it is represented by the desire-values of the concepts, the satisfaction-values of the goals, and the overall satisfaction; at the "conscious" (Narsese) level, it is represented by sentences generated by the related mental operators on what the system "wants" and how it "feels", which selectively express the related information at the unconscious level.

The appraisal information will serve several functions:

* The desire-values of data items (concepts, tasks, and beliefs) will be taken into account by the budget functions, where items with strong feeling (extreme desire-values) will get more resources than items with weak feeling (neutral desire-values).

* The overall satisfaction will be used as feedback to adjust the desire-values of data items, so that the ones associated with positive feeling will be rewarded, and the ones associated with positive feeling punished. In this way, the system will show a "pleasure seeking" tendency, and its extent can be adjusted by a system parameter.

* By involving the feeling-grounded concepts in self-control knowledge, the system will learn strategies to handle various situations specified by their associated emotions. For example, the system will learn what it should do when it is "happy" or "sad", each of which covers many situations that differ in details, but share important common properties.

* The system's feelings and emotions can be communicated to other systems, either by using the "emotional" terms in Narsese, or by showing the related measurements in the GUI.

####Internal experience

Beside satisfaction, the system may use other system-level indicators:

* busyness that summarizes the average priority values of the recently processed. This measurement will decide the chance for tentative ideas to be explored.

* alertness that summarizes the average difference between recently processed input and the corresponding anticipation. This measurement will decide the time spent on direct input.

* well-being that summarizes the situation of energy supply, I/O channel connection, device functioning, etc. This measurement will become necessary when the system directly controls a hardware body.

There will be corresponding feeling operators for each of these indicators. In a sense these indicators are not emotions, since they are not handled exactly as described in the previous section, though in a similar way. All these feelings are included in the system's internal experience.

Beside that, it will also be necessary to record the major events within the system, and make it available to the inference. For this purpose, the current inference log will become partially perceivable by the system itself via certain feeling operations, or, some events will even automatically get into the internal experience, so as to allow the system to answer questions like "What I have been thinking?" or "what methods I've tried on that problem?".

To achieve this purpose, an inference step can be summarized afterwards as an Implication statement from the premise(s) to the conclusion. For double-premise inference rules, it may be better to only include one premise (the task) in the summarizing Implication statement, while taking the other one (the belief) as part of the background knowledge.

The internal experience may be treated similarly as the I/O channels, with a buffer and other features, though there is probably only one such channel.