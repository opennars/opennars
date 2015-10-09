**Open-NARS** is the open-source version of [NARS](https://sites.google.com/site/narswang/home), a general-purpose AI system, designed in the framework of a reasoning system.  This project is an evolution of the [v1.5 system](http://code.google.com/p/open-nars/).  The [mailing list](https://groups.google.com/forum/?fromgroups#!forum/open-nars) discusses both its theory and implementation.

![OpenNARS Logo](https://raw.githubusercontent.com/opennars/opennars/graphplan1/doc/opennars_logo.png)

Theory Overview
---------------

Non-Axiomatic Reasoning System ([NARS](https://sites.google.com/site/narswang/home)) processes tasks imposed by its environment, which may include human users or other computer systems. Tasks can arrive at any time, and there is no restriction on their contents as far as they can be expressed in __Narsese__, the I/O language of NARS.

There are several types of __tasks__:

 * **Judgment** - To process it means to accept it as the system's belief, as well as to derive new beliefs and to revise old beliefs accordingly.
 * **Question** -  To process it means to find the best answer to it according to current beliefs.
 * **Goal** - To process it means to carry out some system operations to realize it.

> _The design of NARS makes no assumption about the content or desire-value of the given goals. How to choose proper given goals for each application is a problem to be solved in the future by the people responsible for the application._


As a reasoning system, the [architecture of NARS](http://www.cis.temple.edu/~pwang/Implementation/NARS/architecture.pdf) consists of a **memory**, an **inference engine**, and a **control mechanism**.

The **memory** contains a collection of concepts, a list of operators, and a buffer for new tasks. Each concept is identified by a term, and contains tasks and beliefs directly on the term, as well as links to related tasks and terms.

The **inference engine** carries out various type of inference, according to a set of built-in rules. Each inference rule derives certain new tasks from a given task and a belief that are related to the same concept.

The control mechanism repeatedly carries out the **working cycle** of the system, generally consisting of the following steps:

 1. Select tasks in the buffer to insert into the corresponding concepts, which may include the creation of new concepts and beliefs, as well as direct processing on the tasks.
 2. Select a concept from the memory, then select a task and a belief from the concept.
 3. Feed the task and the belief to the inference engine to produce derived tasks.
 4. Add the derived tasks into the task buffer, and send report to the environment if a task provides a best-so-far answer to an input question, or indicates the realization of an input goal.
 5. Return the processed belief, task, and concept back to memory with feedback.

All the **selections** in steps 1 and 2 are **probabilistic**, in the sense that all the items (tasks, beliefs, or concepts) within the scope of the selection have priority values attached, and the probability for each of them to be selected at the current moment is proportional to its priority value. When an new item is produced, its priority value is determined according to its parent items, as well as the type of mechanism that produces it. At step 5, the priority values of all the involved items are adjusted, according to the immediate feedback of the current cycle.

At the current time, the most comprehensive description of NARS are the books [Rigid Flexibility: The Logic of Intelligence](http://www.springer.com/west/home/computer/artificial?SGWID=4-147-22-173659733-0) and [Non-Axiomatic Logic: A Model of Intelligent Reasoning](http://www.worldscientific.com/worldscibooks/10.1142/8665) . Various aspects of the system are introduced and discussed in many papers, most of which are [available here](http://www.cis.temple.edu/~pwang/papers.html).

Beginners can start at the following online materials:

 * The basic ideas behind the project: [The Logic of Intelligence](http://www.cis.temple.edu/~pwang/Publication/logic_intelligence.pdf)
 * The high-level engineering plan: [From NARS to a Thinking Machine](http://www.cis.temple.edu/~pwang/Publication/roadmap.pdf)
 * The core logic: [From Inheritance Relation to Non-Axiomatic Logic](http://www.cis.temple.edu/~pwang/Publication/inheritance_nal.pdf)
 * The semantics: [Experience-Grounded Semantics: A theory for intelligent systems](http://www.cis.temple.edu/~pwang/Publication/semantics.pdf)
 * The memory and control: [Computation and Intelligence in Problem Solving](http://www.cis.temple.edu/~pwang/Writing/computation.pdf)


Contents
--------
 * **nars_java** - main logic engine
 * **nars_gui** - java.swing GUI
 * **nars_scala** - embryo of NARS in Scala (not currently active, just to see how NARS could look in Scala)
 * **nars_web** - web server
 * **nars_lab** - experiments & demos
 * **nars_test** - unit tests
 * **nal** - examples

In nars_java/ and nars_gui/ are the NARS core and the Swing GUI in Java. This is derived from the code of Pei Wang in nars_java.0/ directory.

For an overview of reasoning features, see working examples explained in SingleStepTestingCases and MultiStepExamples.


Run Requirements
----------------
 * Java 8 (OpenJDK or Oracle JDK)


Command Shell Usage
-------------------
You can launch NARS in several ways from command window (shell):

```
- console (reads from stdin and writes to stdout):
    ./nars.sh

- gui (Swing)
    ./gui.sh [.nal input file]
    
- reasoner loaded from an experience file:
    ./nars.sh Examples/Example-NAL1-edited.txt --silence 90

- web server start for HTML GUI usage:
    ./web.sh 9999 localhost 9100 1000
     listen port^     ^IP    ^port ^time for each reasoning step in ms
```


Example Files
-------------

Example experience files are in directory nal/Examples
 * "Example-NALn-*.txt" contains single step examples for most of the inference rules defined in NAL level n. The "edited" version contains English translations, and with the unrelated information removed; the "unedited" version contains the actual input/output data recorded by the "Save Experience" function of the GUI. The files can be loaded using the "Load Experience" function of the GUI. 
 * "Example-MultiStep-edited" contains multi-step inference examples described in http://code.google.com/p/open-nars/wiki/MultiStepExamples
 * "Example-NLP-edited" contains an example of natural language processing described in the AGI-13 paper "Natural Language Processing by Reasoning and Learning".

Unit tests are in nal/test.


Development Requirements
------------------------
 * maven, or an IDE with maven support


History
-------
Under the nars_java.0/ directory is the code Pei Wang originally moved into the project, which is still the base of his own programming. This is no active anymore, replaced by nars_java/ and nars_gui/ .

Later Joe Geldart started the nars_java.geldart/ version of NARS, which contains many good ideas (many of which are accepted into 1.5), but it isn't fully consistent with Pei's plan, especially about the new layers (7,8,9), so Pei didn't continue on that code base.

Jean-Marc Vanel continued work on this roadmap, mainly in GUI and software engineering tasks contributing a non-regression test suite and separating NARS into Core and GUI modules.


Source Code status
------------------
See also http://code.google.com/p/open-nars/wiki/ProjectStatus

Current version has been fully tested for single capability at a time; there may still be bugs when combining capabilities.



Links
-----
An (outdated) HTML user manual:
 * http://www.cis.temple.edu/~pwang/Implementation/NARS/NARS-GUI-Guide.html

The project home page:
 * https://code.google.com/p/open-nars/

This version was however developed on Github:
 * https://github.com/opennars/opennars

Discussion Group:
 * https://groups.google.com/forum/?fromgroups#!forum/open-nars
