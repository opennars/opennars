> ### Project Status  
> The current status and the plan for the near future of the project.
***

### Current Situation

The most recent release [OpenNARS 1.6.2](https://github.com/opennars/opennars/wiki/OpenNARS-One-Dot-Six-Dot-Two) (finished in March 2015) implements the whole Non-Axiomatic Logic (NAL 1-9), as described in [Non-Axiomatic Logic: A Model of Intelligent Reasoning](http://www.worldscientific.com/worldscibooks/10.1142/8665).

Another major release is OpenNARS 1.5.7, a stable implementation of NAL 1-6.

***
### Future Tasks

In the near future, there are the following tasks to be accomplished, probably in parallel.

####Conceptual design

Though the major design decisions have been implemented, there are still remaining conceptual issues. Some of them are listed in [OpenNars One Dot Six](https://github.com/opennars/opennars/wiki/OpenNARS-One-Dot-Six), and there will surely more to be found in the testing process.

The conceptual issues usually cannot be resolved by coding considerations, but should be addressed in ways that are consistent with the overall design of NARS.

In the near future, the focus of conceptual design will still be on the logic part, that is on the expressive power of Narsese and the inferential power of NAL. The hypothesis to be tested is that the language and the logic are complete with respect to the objective of A(G)I.

The details of the control part (memory structure and resource allocation) will wait until the logic part is relatively stable.

####Working examples and applications

There are several types of testing cases:

* [Single-step examples](https://github.com/opennars/opennars/wiki/Single-Step-Testing-Cases) show the premise-conclusion relationship of each inference rule

* [Multi-step examples](https://github.com/opennars/opennars/wiki/MultiStep-Examples) show typical functions of the system that takes several steps

* There are more complicated tests described in [Application Programs](https://github.com/opennars/opennars/wiki/Application-Programs) and [Testing Cases](http://www.cis.temple.edu/~pwang/demos.html).

To carry out an application test, it is important to remember that it cannot be done by simply dump a large number of tasks into the system then let it run. It is more efficient to do it in a step-by-step manner, and to follow the order of (1) Narsese (i.e., make sure all the problems and solutions can be expressed), (2) NAL (i.e., make sure all the desired steps are supported by existing rules), and (3) NARS (i.e., make sure the complete process can be carried out when given proper experience).

Some testing cases can be the problems that have been studied in AI and cognitive science. To process them in NARS will clarify its relations with other theories and models.

####Software development

Beside the conceptual issues, future developmental tasks include:

* Debugging

* Documentation (both javadoc and wiki)

* Refactoring (some ad hoc code should be merged into the overall structure)

* Database connection (as explained in [Data And Knowledge Bases](https://github.com/opennars/opennars/wiki/Data-And-Knowledge-Bases))

In the near future, it is still too early to spend time on detailed parameter tuning or performance improvements.