> ###Perception In NARS  
> How to do perception in NARS

***

####Basics

In the context of NARS, "perception" is the process to organize the system's experience into its internal representation, to be used to carry out its tasks.

Perception depends on "sensation", the process to convert the interaction between the system and its environment into Narsese. Sensation of NARS is carried out eventually by the operations, which can be built-in (defined in NAL-9, and implemented in every NARS), implanted-in (not defined in NAL, but innate in a NARS+ at design time), or plugged-in (added into a NARS+ at run time). In all these forms, it can be invoked by NARS, and its observable consequences will come into the system's experience, either immediately or after an unspecified amount of time. There is no additional requirement on the nature of the sensors --- they do not need to similar to human sensors, or limited by our current knowledge.

Perception is based on sensation, but provides more complicated structures and patterns to satisfy the needs of the system. Perception is restricted by the system's (1) sensitive capability (what can be experienced), (2) cognitive capability (what can be derived from experience), and (3) motivations/tasks (what are important and urgent). The actual result of perception also depends on the content of the system's experience. Therefore, perception in NARS is not an attempt to model the world "as it is", nor to emulate human perception in all details.

In NARS there is no separate "perception module" (or modules for vision, hearing, etc.). Perception is unified with other cognitive processes, including reasoning, learning, and so on. Notions like "perception" can be used to stress certain aspects of this process, but not to isolate a sub-process from it.

####Term and Concept

A "term" in NARS is an identifier to a recognizable entity in its experience or memory. Unlike "symbol" in traditional AI and cognitive science, a term does not "refers to" or "represent" an "object" in the world.

A "concept" in NARS is a data structure in its memory. Every concept is named by exactly one term, but not every term names a concept --- some terms are variables (see [VariableUsage](https://github.com/opennars/opennars/wiki/Variable-Usage)), while some others could name concepts but the system does not consider them important enough to deserve the expense.

Terms are related to each other in the system's experience, and these relations are organized into relations of the corresponding concepts. It is these relations that defines the "meaning" of a term and a concept. There is no "interpretation" necessary, and nor "symbol grounding problem" --- the meaning of a term/concept is always "grounded" in the system's experience about it. A term may correspond to words in a language, sensational signals, perceptual patterns, executable operators, or none of them but an "abstract" identifier. However, in all these cases, its meaning is determined in the same way, i.e., by how it is related to the other terms.

Since the system's experience constantly unfolds in time, the meaning of a term is never fixed, but change over time. Furthermore, when a term is used, only part of its relations are involved, due to the resource restriction and attentional focus of the system. Even so, in a given time some terms may have relatively stable meaning. Sometimes it is possible to identify certain relations about a term (or concept) as its "essence" or "definition", in the sense that they are stable and can derive the other relations. However, this is usually an approximation, and even such an approximation is not always possible. Many terms (concepts) cannot be defined at all, though they are still meaningful. It is just that their meaning cannot be briefly and reliably summarized. Terms corresponding to words in natural languages may have "conventional definitions" made by the community using the language, but these definitions do not fully capture the meaning of these terms to the system, though contribute to it. Actuately speaking, a term in NARS has no "correct" or "true" meaning, nor does it converge to such a meaning in the long run.

####Compound Terms

Beside atomic terms that are simply strings in an alphabet, NARS has various types of compound term, formed by a logical operator (or connector) and one or more component terms, which can be compound terms themselves. There is no limit in the level of composition, though terms with complicated internal structure are not favored in resource competition, other factors being the same.

This mechanism let the system to compose new concepts from existing ones. When the terms involved are operations, this is how skills and programs are formed recursively. Then the terms correspond to sensory signals, the compound terms correspond to patterns formed by these signals recursively. In principle, all meaningful concepts can be formed in this way.

NARS does not search (deterministically or randomly) the space of all possible compound terms. Instead, new compound terms are formed only when the corresponding structure happens in the system's experience. For example, the term (&, **[red]**, apple) (for "red apple") is formed only when the system knows an apple that is red, or when the system needs to find an apple that is red. The system may never form the term (&,**[blue]**, apple), though it is understandable. Unlike proposed by some researchers, NARS does not create new concepts by arbitrarily "blend" or "twist" existing concepts.

Furthermore, the system does not keep a concept for every compound term it ever formed in history. On the contrary, most of them are forgot, and the ones kept are the concepts that have relatively high priorities, which means they have a relatively rich and stable meaning, as well as have been useful in the past in processing the tasks. In this way, the evaluation criteria for what concepts to keep are not coded in a fixed formula or algorithm, but are gradual, cumulative, and statistical. For a pattern in the experience to become a concept, both its occurrence frequency and its usefulness to the system matter. Also, if the pattern is complicated, it may take a long time for the system to recognize it, if it is found at all.

####Conceptual Relations

To specify "meaning" by "relations" or "usages" is not really a new idea in AI and cognitive science. For example, similar ideas can be found in semantic networks, neural networks, and statistical semantics. What makes NARS different from the other approaches is how these "relations" are represented and processed.

NARS recognizes the following types of relations among terms:

* Syntactic relations between a compound term and its components. This types of relation is identified by the operator plus the index of the component in the compound. These relations are binary --- there are either there or not, though the syntactic complexity of the compound term may be considered.

* Semantic relations between terms. There are only four of them: inheritance and similarity indicates how the extension and intension of two terms are related; implication and equivalence indicates how the truth-values of two statements (as special compound terms) are related. These relations are measured by the two-factor truth-value of NAL.

* Temporal-spatial relations between terms. They are not used alone, but are embedded in the other relations or mechanisms. The temporal relations are based on the occurring order of events, as defined in NAL-7. The spatial relations are implied in the spatial arrangement of the related sensors. For example, if the system has a group of light sensors arranged in a matrix, then their relative position will represent spatial relations among the corresponding terms.

The above types of conceptual relations are built-in, in the sense that they are directly recognized and processed by NARS, and therefore with fixed meaning. They can be described using "meta-terms". On the contrary, all the other conceptual relations are acquired as terms, with variable meaning. For example, the "parent" relation between "Tom" and "Mary" is actually represented and processed in NARS as an inheritance relation between compound term "(x, {Tom}, {Mary})" and a relational term "parent", as defined in NAL-4.

For a given concept, all of its relations with other concepts collectively determines its meaning, though at a certain time some relations may contribute more than the others. However, the meaning of a term or concept is rarely determined by a single relation. The meaning of a compound term cannot be reduced to its components via the syntactic relations. If a term names a sensation, a perception, or an operation, such a relation still do not decides its full meaning.

NARS concepts are naturally multi-modal. For example, the meaning of "cat" may consist of the vision of cats, the sounds they make, their smell and touch, the system's knowledge and beliefs about them, as well as the pending questions and goals about them.

####Categorization Process

In this context, "categorization" is the process by which a concept is created, modified, evaluated, and used in various ways. In NARS, this process is fully carried out by the inference rules, so it is the same process as "reasoning" and "learning", though each notion stress certain aspects of the process.

The concepts in NARS is not generated by a single algorithm, but by various rules in different ways, triggered by suitable experience of the system. Similarly, a certain conceptual relation can be built in more than one ways, which means the same question about categorical relationship can be answered in different ways.

With inference rules processing the built-in relations, NARS does not merely use conceptual relations to express meanings of concepts, but can also effectively manipulate these relations. This cannot be done in a semantic network, where a link can represent any relation, so there is no clear way to use it; this also cannot be done in a neural network, where a link merely means two nodes are related, with little further information.

A question on categorical relationship typically requests the evaluation of a semantic relation, and the answer is usually a matter of degree. Such a conclusion is usually the result of a cooperation of multiple inference rules in an inference process. Beside the available knowledge, this process is also influenced by the available resources. This process is history-dependent and context-sensitive, and usually not accurately repeatable.

####Compared with other AI Techniques

The approach NARS taking for perception is fundamentally different from the most common approaches.

NARS does not take perception as a separate module or process, but see it as unified with other cognitive processes.

NARS does not take perception as a purely input, "data-mining", process that takes all sensory data as the input at the beginning, and produces certain desired results at the end. Instead, perception is taken to be an interactive, incremental, and lifelong process that is driven both by input data and active tasks (goals).

NARS does not following a predetermined algorithm at the problem-solving level. Since many factors are ever-changing and their combinations are not repeatable, the process does not follow an overall algorithm. Even for the same input, in different context the results of perception may be different, though they are neither arbitrary nor random.

The development of perception in NARS will be more like what happens in a baby than in an traditional AI program.

####Implementation and Application

To realize perception in the OpenNARS implementation, the following steps will be taken.

1. Identify a proper testing domain. The system should be able to get continues input, probably both in some Narsese channels and some non-Narsese channels together. Natural language channels are possible, too, but maybe at a later stage.

2. Implement the necessary sensors and actuators as NARS operations. NARS allows multiple levels of granularity of a certain modality. Take vision for instance, it is fine to have operations corresponding to the sensation of pixels, as well as more complicated operations that directly recognize objects of certain type, or a mixture of them.

3. Confirm that Narsese has enough expressive power for the perception-related compound terms, especially various temporal-spatial patterns. If necessary, revise or extend the grammar rules.

4. Confirm that NAL has enough inferential power for the perception-related tasks. It may be necessary to implement certain special-purpose "macro-rules" for important perceptual routines.

5. Identify and implement the mental operators needed for perception-related tasks.

6. Refine the control mechanisms to efficiently carry out perceptual processes, as well as to balance them with other processes in the system.

7. Explore various perceptual tasks. Beside the traditional tasks like the learning, recognition, and clustering of perceptual patterns, there are also tasks demanding sensorimotor coordination and perception–cognition continuum, like imitation, skill learning from reading, etc.