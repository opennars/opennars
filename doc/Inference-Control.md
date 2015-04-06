> ### InferenceControl 
> The control aspect of NARS.  

***

### Inference Control in NARS

####Introduction

Every reasoning system has a "logic" part and a "control" part. The former specifies what can be expressed (the grammar and semantics) and what can be derived in each step (the inference rules), while the latter specifies what tasks can be carried out by linking the inference steps into inference processes. This relation is put into an intuitive formula by Robert Kowalski as "Algorithm = Logic + Control", though it can be extended beyond algorithmic processes.

The logic part of NARS consists of the formal language [Narsese](https://github.com/opennars/opennars/wiki/Input-Output-Format), [Experience-Grounded Semantics (EGS)](http://www.cis.temple.edu/~pwang/Publication/semantics.pdf), and [Non-Axiomatic Logic (NAL)](http://www.worldscientific.com/worldscibooks/10.1142/8665). It has been formally specified and mostly implemented.

The control part of NARS is relatively underdeveloped. Its fundamental principles had been decided at the very beginning of the project, but many concrete details remain to be decided, and the current implementation is only a very rough prototype that only handles simple cases. A major reason of this situation is the dependency of the control part on the logic part: each time a major change is made in the logic, the control often needs to be changed accordingly, while on the other hand, the logic part does not depend on the details of the control part. Because of this, the past work on NARS has been focused on the logic, and the control has been implemented mainly to test Narsese and NAL, rather than to solve actual problems. Now the logic of NARS is relatively mature, so the work is gradually moving into the control part of the system.

####Design principle

Traditionally, the control mechanism in a reasoning system depends on either a domain-specific "strong method" or a domain-independent "weak method". In the former case, the inference steps are organized into inference algorithms specially designed for the problem to be solved; in the latter case, some universal algorithm, such as exhaustive research, is applied that does not need domain-specific knowledge. In both cases, the inference process follows an algorithm that is repeatable, either deterministically or probabilistically.

NARS is based on the theory that intelligence is a relative rationality, and an intelligent system should be adaptive and work with insufficient knowledge and resources. Consequently, NARS uses neither the "strong method" nor the "weak method", but works in a "non-algorithmic" manner, by selecting its inference steps at run time, depending on many ever-changing factors. Consequently, an individual inference process, as part of the system's lifelong experience, is not repeatable, even in the probabilistic sense. It is not a "strong method" because NARS is designed to be general-purpose, without assuming any domain knowledge. On the other hand, it is not "weak method", neither, because the system learns domain knowledge gradually, so its selection of steps will become more and more based on its past experience.

Roughly speaking, the function of control mechanism in NARS is to dynamically allocate the system's limited resources to the activities demanding them. The objective is to optimize the expected overall efficiency, estimated according to the system's beliefs summarizing the past experience. Under the assumption of insufficient resources, the system will not be able to satisfy all the resource demands, and under the assumption of insufficient knowledge, there is no guarantee that the selections the system makes are really optimal when judged according to the system's future experience.

For theoretical discussions about this approach, see the two books and the [publications on Resource Management](http://www.cis.temple.edu/~pwang/papers.html).

####Formal model of resource allocation

The control problem can be abstractly specified as an optimization problem: given finite processing time and storage space, find the best allocation plan to satisfy the demands as much as possible.

There are many formal models for such problems, but none of them can be used here, because their assumptions cannot be satisfied in NARS, even approximately. For instance, here it cannot be assumed that the problems occur according to a probability distribution (even a unknown one), nor that the problems will appear at specific time. New models need to be developed.

A major job of the control mechanism is to select the most proper task (judgment, goal, or question) to carry out at every moment. Given that NAL usually does not produce "final solution" to tasks, the more beliefs are used on a task, the better the solution will be. Since new tasks may come to the system at any moment, it is usually impossible to decide an algorithm, with a resource budget, before the processing starts and remains fixed.

Given this situation, the objective of NARS is not to process all of its tasks perfectly, not even to process them to a "satisfying level", but to achieve the highest overall efficiency in resource usage. This is achieved by distributing the resources according to priority values summarizing the various factors involved in resource allocation, and to adjust the priority values at run time to reflect the changes in the environment and within the system.

Similar to the design of truth-value functions, in the control part of NARS almost all related quantities are represented as "extended Boolean values" (EB values), that is, real numbers in [0, 1] with the boundary cases taken as Boolean values. The basic functions defined on them are "extended Boolean functions" (EB functions) that extend the traditional Boolean functions and, or, and not from binary to EB values (the first two are also known as triangular norm and triangular conorm, or t-norm and t-conorm, respectively). The basic EB functions used in NARS are and(x, y) = xy, or(x, y) = 1 - (1 - x)(1 - y), and not(x) = 1 - x. A few other simple functions are also used, such as the average of a few EB values.

A control function is designed by first considering the factors that should be involved in that case, then analyzing their relationship and representing it using the basic EB functions. All the control functions are tentative, since they are based on the current research results, so may be revised as the research progresses.

####Memory structure and control strategy

To realize dynamic resource allocation, NARS self-organizes its memory using probabilistic priority-queue to give data items prioritized treatment. A bag is a data structure with the basic operations (1) put in, (2) take out, and (3) access by key. The take-out operation is probabilistic, according to the priority distribution of the items in the bag. Each operation takes a small constant time to finish.

Issue: Currently the probability of an item to be taken out is proportional to its priority value. It may be necessary to add a parameter to control the evenness of the probability.

Since NAL is a term logic, its inference rules typically require shared term in premises, so tasks and beliefs can be indexed and clustered by their component terms. In NARS, each concept is a data structure named by a term, and it links to the tasks and beliefs with the term in it. Consequently, a concept is an independent unit of storage and processing.

Roughly speaking, the system's memory contains a bag of concepts; a concept contains a bag of task-links referring to the relevant tasks (justment, goal, or question) and a bag of term-links referring to the relevant beliefs (judgments only).

The following figure summarizes the overall architecture and procedure of NARS:

![architecture](https://cloud.githubusercontent.com/assets/11791925/6994023/e5f32f68-db3b-11e4-8814-aa544e7fae27.jpg)

More accurately, the system runs by repeating the following working cycle:

1. Probabilistically select a concept C from the memory

2. Probabilistically select a tasklink from C, which specifies the task T to be used

3. Probabilistically select a termlink from C, which specifies the belief B to be used

4. With T and B as premises, trigger the applicable inference rules to derive new tasks and add them into the task buffer

5. Probabilistically select some tasks from the task buffer for pre-processing

Pre-processing of a task means to puts it into the corresponding concepts (and create them if they do not exist). Within a concept, the new task may create new belief, revise existing beliefs, satisfy a goal, or answer a question. If an input question obtains a best-so-far answer, it will be reported.

A graphical description of how questions are answered by the system by this process:

![question_answering](https://cloud.githubusercontent.com/assets/11791925/6994024/04f57344-db3c-11e4-9a9b-b3a6ac54a699.png)

**Issue:** It may be necessary to add task buffers to each concept to hold the tasks added, so as to postpone their processing until the concept is selected. Competition can be added into the buffer, so only selected tasks are processed.

The system's working cycle follow a fixed algorithm, and takes a small constant time to finish. The processing of a task is carried out by a number of working cycles, though their number and order are not predetermined, but decided by the control mechanism at the run time.

#### Budget values and functions

In OpenNARS, every data item participating in resource competition (such as belief, task, concept, as well as the links to them) extends the abstract class Item, which has a budget consisting of 3 EB values: priority, durability, and quality.

* The priority of an item determines its current access chance with respect to the other items in the same bag. This value is increased when the item is activated, and is highly context-sensitive. It summarizes many factors.

* The durability of an item determines the decay rate of the priority by multiplying into it periodically. This process is responsible for the relative forgetting of items, and is normally the only one that deactivates an item, that is, there is no "inhibition" or "forced forgetting" at this level.

* The quality of an item is mostly determined by its intrinsic value to the system, without considering its relevance to the current context. It also determines the residual priority of the item where relative forgetting stops.

In each working cycle, only the directly related items will have their budgets adjusted, which means the system may need to decide for an item how long a decay has been "overdue", and to decrease its priority for multiple decay periods. The system will not attempt to globally traverse the items to implement forgetting.

####budget of belief

A belief is a judgment that is already accepted into memory as summary of the system's partial experience. It is created by a task with identical content, and its initial budget will simply be a copy of that of the task. However, after that its budget is adjusted independently.

The quality of a belief is determined by the following factors:

* its confidence: higher values are preferred

* its frequency: extreme values (closer to 1 or 0) are preferred

* its complexity: lower values are preferred

The priority a belief is increased in the following situations:

* When the same conclusion is derived repeatedly, it will become more active.

* When a belief contributes to the solution of a task, it will be rewarded and become more accessible.

* When the concepts a belief refers to are active, the belief becomes more accessible. Therefore, when a concept is activated, the beliefs mentioning it in the other concepts will become more accessible.

**Issue:** The last process has not been implemented.

The durability a belief is increased together with the priority in the first two situations listed above, so useful items will be remembered longer.

**Issue:** The increasing of priority and durability should be further differentiated to show different effects.

####budget of task

A task can be a new judgment to be absorbed into beliefs, a goal to be achieved, or a question to be answered. Each of the three types is handled differently, though there are some common treatments.

The quality of a task is similar to that of a belief, though goal and question have no truth-value.

The priority and durability of an input task are specified by the user, which gives the user an opportunity to influence the system's processing of the task. System default values are used when no values are provided.

Issue: In the future when different types of users and task sources are involved, each may have a different default budget or a different allowed budget range. One option is to add this into input channel.

The priority and durability of a derived task depends on those of its parent task and belief, as well as the type of the inference.

Issue: One unusual feature of NARS is that a derived task will exist and be processed independently, without a permanent bond to its parent task. For some applications, such a feature may be undesired. In that case, a link can be added so every derived task links to its parent task. Each time when it is selected, the existence of its parent task is checked first, and when the parent no longer exist, the child will be removed, too. However, such a treatment will decrease the system's creativity and autonomy. A compromise is to use a system parameter to tune the durability of the derived tasks, so that after the parent task is removed, its children will all disappear soon.

The priority and durability of a task are increased only on one situation: the same task is repeatedly generated, either from the same source and from different sources. This situation should not be confused with the treatment of truth-value. If the same judgment is derived multiple times from the same parent task and belief, the copies will be merged into a task with a larger budget, though the truth-value remains the same, since the premises are based on overlapping evidence. In this way, repeated inference is not completely a waste of time, since it will at least increase the chance for the conclusion to be further processed.

The budget of a goal or a question is reduced when the task is partially satisfied. The better the solution is (evaluated by the choice rule), the lower the budget will become. This process and the relative forgetting process will eventually let a task be removed from the system, though it does not necessarily mean that the task has been processed to a certain level of satisfaction.

####budget of concept

A concept is created when an accepted task contains a term for which there is no existing concept. In this case, the initial priority and durability of the concept is determined by those of the task. After that, the budget of the concept is adjusted independently.

The quality of a concept is initially determined by the complexity of the term, and then adjusted according to the quality of the links and the usefulness of the concept to the system.

Issue: Currently the complexity of a term is completely defined according to its internal structure: atomic terms have complexity value 1, and a compound's complexity is the sum of the complexity values of its components plus 1 (for the operator). To reward often used compounds, their complexity may be considered as lower than their "literal complexity" defined above, since they are often used as a whole, and its internal structure becomes less and less "visible". The same effect can be achieved by the ^naming operator, which creates an atomic term for an often used compound, and builds a strong similarity statement between the two. It is unclear whether both mechanisms are needed, and if not, which one should be used.

The quality of links is a more complicated issue. Currently only a very rough function is there as a placeholder.

**Issue:** The quality of a concept is probably a compromise or balance of the following factors:

* "Well-defined" concepts are preferred. The beliefs of such a concept can be derived from a small and relatively stable "core meaning" (as its "essence" or "definition"), and most questions about its instance or properties have sharp "yes/no" answers.

* "Basic-level" concepts are preferred. Such a concept has balanced extension and intension, that is, many instances sharing many properties. For example, concept "car" is usually preferred than both the more general concept "vehicle" and the more specific concept "truck".

* Useful concepts are preferred. This evaluation is completely based on the system's experience, according how often a task is successfully solved using the concept.

How to measure the above factors and whether there are others remain to be decided.

####budget of link

Each concept contains a bag of task-links that refers to the relevant tasks and a bag of term-links that refers to its compound or component terms from where the beliefs are obtained. Therefore, the initial budget of a link is obtained from the target it links to. However, after that, the budget of the link is adjusted independently. For example, the belief "dove --> bird" is linked from both the concept "dove" and the concept "bird", and the quality of the belief remains the same for the two concepts. However, this belief is more useful for "dove" than for "bird", so gradually the two links will have very different budget values.

####Attention and activation spreading

The control mechanism of NARS shares ideas with evolutionary computation and neural network, though does not directly use their techniques.

The resource competition of items in a bag is similar to the competition of individuals in a population; the priority-biased processing is similar to the fitness-biased reproduction; and the derivation of a new task from a parent task and a parent belief is similar to the creation of an individual from two parents. On the other hand, NARS does not have pure random action like mutation, and the various inference carries out much more complicated and justifiable ways to produce new tasks than crossover.

The memory of NARS can be naturally interpreted as a conceptual network that supports distributed representation and parallel processing, with the inference rules as actions that modify the topological structure and weights on the links. Under this interpretation, the priority distribution among concepts is similar to the activation distribution in neural networks (NNs), and priority adjustments is similar to activation spreading. However, there are several key differences in this analogy:

* In NARS, the logic and the control are clearly separated. Priority is part of the latter, so does not play a representational role (as in NNs).

* In NARS, activation spreading is not a stand-alone process, but a consequence of task derivation. That is, what is sent from a concept to another is not "pure energy", but a task with meaning, which triggers an increase in priority of the receiver.

* In NARS, this activation spreading is highly selective. A "firing concept" does not increase the activity of all of its neighbors, but only a small portion of them.

The overall priority distribution in memory at a moment represents how the system's attention is distributed at that time, and this attention shifts constantly, both according to the external input tasks and the internal processing of the tasks.

For a given task, the current priority distribution in memory and in the relevant concept form the processing context of the task, and it determines what beliefs will be used to process the task. Unlike most other approaches, NARS does not take "context" as micro world models with labels, but as the internal environment in which a task is processed. In this sense, context is unlabeled, ever-changing, and have no clear boundary. Even so, it still captures what we usually mean by "context sensitive" in AI and cognitive science.

####System parameters

The control mechanism is specified with many "system parameters", which are quantities that has not been given a unique value. It is may be because the best value has not been found, but more likely it is because there is no "best value". As the development advances, the former cases will be gradually eliminated, while the latter cases will remain.

A system parameter indicates a bias of the system, so different values will give the system different "personalities". There is no best value because a certain personality may be good for solving some problems, but bad for some others. Even though, usually there is a "normal range" for each parameter, and values outside it will not be acceptable.

One way to tune and study system parameters is to compare them in a community of multiple NARS implementations, each with a different personality. It is also possible to use an evolutionary process to generate and select new personalities. However, such experiments should wait until the individual NARS systems to become relatively complete and stable.

####Dual-process in control

Beside the above automatic process that unconsciously controls the inference process of the system, the mental operations defined in NAL-9 also let the system consciously control its own inference process, as well as to self-program the system and to follow existing algorithms.

In the long run, the inference activities of the system will be controlled by both mechanisms.

Beside (external) knowledge about the environment, NARS will learn several types of (internal) knowledge directly related to inference control:

* Structural knowledge embedded among the priority values. This type of knowledge decides the chance for a data item (task, belief, or concept) to be accessed, and it cannot be expressed in Narsese, but have to be obtained via repeated practise.

* Procedural knowledge expressed as compound operations. This type of knowledge control the routine actions taken by the system, and is also mainly obtained through practise, though it can be expressed in Narsese.

* Declaritive knowledge expressed as descriptions of methods and processes. This type of knowledge can be fully represented as judgments in Narsese, and can influence the system's selections.

####Feeling and emotion

Feeling and emotion play an important role in truly intelligent systems, and they start from subjective appraisal of events, entities, and situations, with respect to goals of the system.

Goals provide the ultimate criteria for the system's appraisals and references.

First, the appraisal of statements is represented by the desire-value of a statement. It is determined by the statement's logical relation with the goals that have been taken into account.

Second, the overall status of the system in goal satisfaction is measured by a few global variables, such as satisfaction and busyness.

Finally, a desire-value will be added to every term, to record the system's appraisal to it, evaluated according to the relation of the term with the events and situations. Its value may be various degrees of "like" or "dislike", as well as "mixed" or "don't care".

**Issue:** The last type has not been implemented yet, and may wait the first two to become relatively stable. Another open issue is how to effectively use emotion in the control mechanism.

####The proper usage of NARS

The current implementation mostly services as a proof of concept and a platform to test Narsese and NAL. As shown by the [testing cases](http://www.cis.temple.edu/~pwang/demos.html), the current system can be used to carry out single-step inference processes, as well as simple multi-step inference processes. However, the system is not ready for complicated inference processes.

In the future versions, the inference control of NARS will be improved in the following ways:

* A refined design of the automatic control mechanism. Effort is being made to develop a general theory of resource allocation, based on the previous research on attention mechanism and decision theory.

* A full implementation of self-monitor and self-control. Certain internal control activities will be implemented as mental operations that can be executed as the result of inference.

* Proper tutoring. As mentioned above, control knowledge usually cannot be directly loaded into the system, but has to grow by the system itself. Similar to how a human is trained in a domain, not only the content of the input knowledge matters, but also the order and timing of the input, as well as the existence of guiding questions and driving goals. This will still be the case after the control system is fully developed.

On the other hand, if the objective of a project is to build a NARS-based application for a specific domain, the above strategy is probably not the best approach. It will be much more efficient to control the inference process using problem-specific algorithms.