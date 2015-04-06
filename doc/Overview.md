> ### Overview  
> A high-level overview of this NARS implementation 

***

Open-NARS is the open-source version of [NARS (Non-Axiomatic Reasoning System)](https://sites.google.com/site/narswang/home), a general-purpose AI system, designed in the framework of a reasoning system.

NARS works by processing tasks imposed by human users, other computer systems, as well as its own needs. Tasks can arrive at any time, and there is no restriction on their contents, as far as they can be expressed in Narsese, the I/O language of NARS (see [Input Output Format](https://github.com/opennars/opennars/wiki/Input-Output-Format) for the Grammar of Narsese).

There are several types of tasks:

* Judgment. To process it means to accept it as the system's belief, as well as to derive new beliefs and to revise old beliefs accordingly.

* Goal. To process it means to carry out some system operations to realize it. (Not available in versions 1.5.n.)

* Question. To process it means to find the best answer to it according to current beliefs and desires.

* As a reasoning system, the [architecture of NARS](http://www.cis.temple.edu/~pwang/Implementation/NARS/architecture.pdf) consists of a memory, an inference engine, and a control mechanism.

The memory contains a collection of concepts, a list of operators, and a buffer for new tasks. Each concept is identified by a term, and contains tasks and beliefs directly on the term, as well as links to related tasks and terms.

The inference engine carries out various type of inference, according to a set of built-in rules. Each inference rule derives certain new tasks from a given task and a belief that are related to the same concept.

Roughly speaking, the control mechanism repeatedly carries out the working cycle of the system, consisting of the following steps:

1. Select tasks in the buffer to insert into the corresponding concepts, which may include the creation of new concepts and beliefs, as well as direct processing on the tasks.

2. Select a concept from the memory, then select a task and a belief from the concept.

3. Feed the task and the belief to the inference engine to produce derived tasks.

4. Add the derived tasks into the task buffer, and send report to the environment if a task provides a best-so-far answer to an input question, or indicates the realization of an input goal.

5. Return the processed belief, task, and concept back to memory with feedback.

All the selections in steps 1 and 2 are probabilistic, in the sense that all the items (tasks, beliefs, or concepts) within the scope of the selection have priority values attached, and the probability for each of them to be selected at the current moment is proportional to its priority value. When an new item is produced, its priority value is determined according to its parent items, as well as the type of mechanism that produces it. At step 5, the priority values of all the involved items are adjusted, according to the immediate feedback of the current cycle.

At the current time, the most comprehensive description of NARS are the books [Rigid Flexibility: The Logic of Intelligence (2006)](http://www.springer.com/us/book/9781402050442) and [Non-Axiomatic Logic: A Model of Intelligent Reasoning (2013)](http://www.worldscientific.com/worldscibooks/10.1142/8665). Various aspects of the system are introduced and discussed in many papers, most of which are available [here](http://www.cis.temple.edu/~pwang/papers.html).

Beginners can start at the following online materials:

* The basic ideas behind the project: [The Logic of Intelligence](http://www.cis.temple.edu/~pwang/Publication/logic_intelligence.pdf)

* The high-level engineering plan: [From NARS to a Thinking Machine](http://www.cis.temple.edu/~pwang/Publication/roadmap.pdf)

* The core logic: [From Inheritance Relation to Non-Axiomatic Logic](http://www.cis.temple.edu/~pwang/Publication/inheritance_nal.pdf)

* The semantics: [Experience-Grounded Semantics: A theory for intelligent systems](http://www.cis.temple.edu/~pwang/Publication/semantics.pdf)

* The memory and control: [Computation and Intelligence in Problem Solving](http://www.cis.temple.edu/~pwang/Writing/computation.pdf)

To test the current implementation, download the [most recent version](https://drive.google.com/a/temple.edu/folderview?id=0B8Z4Yige07tBUk5LSUtxSGY0eVk&usp=sharing#), then try the working examples in the ZIP file. These examples are explained in [Single Step Testing Cases](https://github.com/opennars/opennars/wiki/Single-Step-Testing-Cases) and [Multi-Step Examples](https://github.com/opennars/opennars/wiki/MultiStep-Examples).