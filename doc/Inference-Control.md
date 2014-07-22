# The control aspect of NARS.

# Inference Control in NARS

## Introduction

Every reasoning system has a "logic" part and a "control" part. The former specifies what can be expressed (the grammar and semantics) and what can be derived in each step (the inference rules), while the latter specifies what tasks can be carried out by linking the steps into a process. This relation is put into an intuitive formula by Robert Kowalski as "Algorithm = Logic + Control", though it can be extended beyond algorithmic processes.

The logic part of NARS consists of the formal language [[Input Output Format|Narsese]], [Experience-Grounded Semantics (EGS)](http://www.cis.temple.edu/~pwang/Publication/semantics.pdf), and [Non-Axiomatic Logic (NAL)](http://www.worldscientific.com/worldscibooks/10.1142/8665). It has been formally specified and mostly implemented.

The control part of NARS is relatively underdeveloped. It fundamental principles have been decided at the very beginning of the project, many concrete details remains to be decided, and the current implementation (1.5.5) is only a very rough prototype that only handles simple [[Multi Step Examples|examples]]. A major reason of this situation is the dependency of the control part on the logic part: each time a major change is made in the former, the letter often needs to be changed accordingly, while on the other hand, the logic part does not depend on the details of the control part. Because of this, the past work on NARS has been focused on the logic part, and the control part has been implemented to serve the purpose of testing Narsese and NAL, rather than for the solving of actual problems. Now the logic part is relatively mature, so the attention is gradually moving into the control part of the system.

## Design principle

Traditionally, the control mechanism in a reasoning system depends on either a domain-specific "strong method" or a domain-independent "weak method". In the former case, the inference steps are organized into inference algorithms specially designed for the problem to be solved; in the latter case, some universal algorithm, such as exhaustive research, is applied that does not need domain-specific knowledge. In both cases, the inference process follows an algorithm that is repeatable, either deterministically or probabilistically.

NARS is based on the theory that intelligence is a relative rationality, and an intelligent system should be adaptive and works with insufficient knowledge and resources. Consequently, NARS uses neither the "strong method" nor the "weak method", but work in an "non-algorithmic" manner, by selecting its inference steps at run time, depending on many ever-changing factors. Consequently, an individual inference process, as part of the system's lifelong experience, is not repeatable, even in the probabilistic sense. It is not a "strong method" because NARS is designed to be general-purpose, without assuming any domain knowledge. On the other hand, it is not "weak method", neither, because the system learns domain-knowledge gradually, so its selection of steps will become more and more based on the past problems it was given previously.

Roughly speaking, the function of control mechanism in NARS is to dynamically allocate the system's limited resources to the activities demanding them, according to the estimations made according to the system's beliefs that summarize the system's past experience. Under the assumption of insufficient resources, the system will be unable to satisfy all the demands, and under the assumption of insufficient knowledge, there is no guarantee that the selections the system makes are optimal, according to the system's future experience.

For theoretical discussions about this approach, see the two books and the [publications on Resource Management](http://www.cis.temple.edu/~pwang/papers.html).

## Memory structure and control strategy

To realize dynamic resource allocation, NARS self-organizes its memory using probabilistic priority-queue to give data items different treatment. A bag is a probabilistic priority-queue. Its basic operations are (1) put in, (2) take out, and (3) access by key. The take-out operation is probabilistic, according to the priority distribution of the items in the bag. Each operation takes a small constant time to finish. Bag supports the usage of insufficient time and space using relative and absolute forgetting.

Since NAL is a term logic, and its inference rules typically require shared term in premises, tasks and beliefs can be indexed and clustered by their subject and predicate. In NARS, each concept is named by a term, and refers to the tasks and beliefs with the term in it. A concept becomes an independent unit of storage and processing. Roughly speaking, the system's memory contains a bag of concepts; a concept contains a bag of tasks and a bag of beliefs.

A summary for the architecture and working cycle is [here](https://sites.google.com/site/narswang/home/nars-introduction/architecture.jpg). NARS repeats the following working cycle:
1. Select a concept from the memory
1. Select a task from the concept
1. Select a belief from the concept
1. Use the task and the belief as premises for the applicable rules to derive new tasks
1. Return the used items with adjusted priority
1. Add the derived tasks and input tasks obtained in this cycle into the corresponding concepts. Report an answer to an input question if it is the best that the system has found.

The priority of an item (task, belief, or concept) is determined at its creation time, then adjusted each time the item is accessed, mainly depending on the following factors:
- its intrinsic quality (which remains stable over time)
- its performance record (which accumulates over time)
- its contextual relevance (which changes constantly).

Generally speaking, the system constantly reorganizes its knowledge for question answering. The major criteria in organizing knowledge include:
- Correctness with respect to experience,
- Concreteness of prediction
- Compactness in content

## Dual-process in control

Beside the above *automatic* process that *unconsciously* controls the inference process of the system, the mental operations defined in NAL-9 also let the system to *consciously* control its own inference process, as well as to self-program the system and to follow existing algorithms.

In the long run, the inference activities of the system will be controlled by both mechanisms.

Beside knowledge about the environment, NARS will learn several types of knowledge directly related to inference control:
- *Structural* knowledge embedded among the priority values. This type of knowledge decides the chance for a data item (task, belief, or concept) to be accessed, and it cannot be expressed in Narsese, but have to be obtained via repeated practise.
- *Procedural* knowledge expressed as compound operations. This type of knowledge control the routine actions taken by the system, and is also mainly obtained through practise, though it can be expressed in Narsese.
- *Declaritive* knowledge expressed as descriptions of methods and processes. This type of knowledge can be fully represented as judgments in Narsese, and can influence the system's selections.

## The proper usage of NARS

The current implementation mostly services as a proof of concept and a platform to test Narsese and NAL. As shown by the [testing cases](http://www.cis.temple.edu/~pwang/demos.html), the current system can be used to carry out single-step inference processes, as well as simple multi-step inference processes. However, the system is not ready for complicated inference processes.

In the future versions, the inference control of NARS will be improved in the following ways:
- A refined design of the automatic control mechanism. Effort is being made to develop a general theory of resource allocation, based on the previous research on attention mechanism and decision theory.
- A full implementation of self-monitor and self-control. Certain internal control activities will be implemented as mental operations that can be executed as the result of inference.
- Proper tutoring. As mentioned above, control knowledge usually cannot be directly loaded into the system, but has to grow by the system itself. Similar to how a human is trained in a domain, not only the content of the input knowledge matters, but also the order and timing of the input, as well as the existence of guiding questions and driving goals.

On the other hand, if the objective of a project is to build a NARS-based application for a specific domain, the above strategy is probably not the best approach. It will be much more efficient to control the inference process using problem-specific algorithms.