![OpenNARS Logo](https://github.com/opennars/opennars/blob/bf53ceef9f2399de70dc63e5507e42d639144c96/doc/opennars_logo2.png)
**Open-NARS** is the open-source version of [NARS](https://sites.google.com/site/narswang/home), a general-purpose AI system, designed in the framework of a reasoning system.  This project is an evolution of the [v1.5 system](http://code.google.com/p/open-nars/).  The [mailing list](https://groups.google.com/forum/?fromgroups#!forum/open-nars) discusses both its theory and implementation.

[![Build Status](https://travis-ci.org/opennars/opennars.svg?branch=master)](https://travis-ci.org/opennars/opennars)
[![codecov](https://codecov.io/gh/opennars/opennars/branch/master/graph/badge.svg)](https://codecov.io/gh/opennars/opennars)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/fce375943907463fa53dc5bebcefebbd)](https://www.codacy.com/app/freemo/opennars?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=opennars/opennars&amp;utm_campaign=Badge_Grade)

How to build OpenNARS
---------------------
Using mvn:

for each of the projects: 

	https://github.com/opennars/opennars-parent.git     
	https://github.com/opennars/opennars.git     
	https://github.com/opennars/opennars-lab.git     
	https://github.com/opennars/opennars-applications.git     
	https://github.com/opennars/opennars-gui.git

	git clone 'project'
	cd 'project_dir'
	mvn clean install 

optionally append -Dmaven.javadoc.skip=true to skip documentation generation

	cd 'project_dir'
	mvn exec:java

Alternatively, using IntelliJ IDE:

Install git https://git-scm.com/downloads

Install OpenJDK 11 https://jdk.java.net/11/

Install community edition IntelliJ https://www.jetbrains.com/idea/download/

Checkout https://github.com/opennars/opennars.git

Checkout https://github.com/opennars/opennars-lab.git

Checkout https://github.com/opennars/opennars-applications.git

You can either checkout within Intellij or use the Github desktop (available from the github clone button in the repo)

Build opennars

If this is a fresh install you will be prompted to enter the jdk path (where you installed it above)
You may be prompted to update maven dependencies - do this if prompted

Build opennars-lab

Select org.opennars.lab.launcher.Launcher as the main entry point

Build opennars-applications

Select org.opennars.applications.Launcher

Application Launchers
---------------------
The launchers are the easiest way to run the various apps

opennars-lab 

    Main GUI - Main user interface for NARS

    Test Chamber - Simulation environment for testing behaviours

    Micro world	- Behaviour learning by simple insect like creature

    NAR Pong - The classic pong game

    Language Lab - For experimenting with parts of speech (POS) and grammar learning

    Perception Test - Pattern matching experiment

    Prediction Test - Predicts a waveform - Can be run directly from Intellij (Current issue with running with launcher)

    Vision - Vision experiment - Can be run direcly from Intellij (Current issue with running with launcher)

opennars-applications

    Main GUI - A simple MIT license GUI

    Crossing - A smart city traffic intersection simulation

    Identity mapping - An experimental setup for testing aspects of Relations Frame Theory (RFT)


Opennars Core is run directly by the Lab and Applications Launchers.

Example Narsese files
--------------------
Here is a link to some Narses examples including:

    Toothbrush example - how to use a toothbrush to undo a screw?
    
    Detective example - who is the criminal?
    
https://github.com/opennars/opennars/tree/master/src/main/resources/nal/application    

Theory Overview
---------------

Non-Axiomatic Reasoning System ([NARS](https://sites.google.com/site/narswang/home)) processes tasks imposed by its environment, which may include human users or other computer systems. Tasks can arrive at any time, and there is no restriction on their contents as far as they can be expressed in __Narsese__, the I/O language of NARS.

There are several types of __tasks__:

 * **Judgment** - To process it means to accept it as the system's belief, as well as to derive new beliefs and to revise old beliefs accordingly.
 * **Question** -  To process it means to find the best answer to it according to current beliefs.
 * **Goal** - To process it means to carry out some system operations to realize it.

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
 * The core logic: [From Inheritance Relation to Non-Axiomatic Logic](https://cis.temple.edu/~pwang/Publication/inheritance_nal.pdf)
 * The semantics: [Experience-Grounded Semantics: A theory for intelligent systems](https://cis.temple.edu/~pwang/Publication/semantics.pdf)
 * The memory and control: [Computation and Intelligence in Problem Solving](https://cis.temple.edu/~pwang/Writing/computation.pdf)


Contents
--------
 * **[core](https://github.com/opennars/opennars/tree/master/src/main/java/org/opennars)** - reasoning engine
 * **[nal](https://github.com/opennars/opennars/tree/master/src/main/resources/nal)** - examples/unit tests

The core is derived from the code of Pei Wang.


Run Requirements
----------------
 * Java 8+ (OpenJDK 10 recommended)

Example Files
-------------
For an overview of reasoning features, see working examples (tests) in the nal folder, also explained in [SingleStepTestingCases](https://github.com/opennars/opennars/tree/master/src/main/resources/nal/single_step) and [MultiStepExamples](https://github.com/opennars/opennars/tree/master/src/main/resources/nal/multi_step).


Development Requirements
------------------------
 * Maven

Links
-----
 * [Website](http://opennars.github.io/opennars/)
 * [All downloads](https://drive.google.com/drive/folders/0B8Z4Yige07tBUk5LSUtxSGY0eVk?usp=sharing)
 * [An (outdated) HTML user manual](http://www.cis.temple.edu/~pwang/Implementation/NARS/NARS-GUI-Guide.html)
 * [The Project homepage](https://code.google.com/p/open-nars/)
 * [google groups - Discussion Group](https://groups.google.com/forum/?fromgroups#!forum/open-nars)
 * [IRC](http://webchat.freenode.net?channels=nars)
 * [Try online](http://91.203.212.130/NARS)
