# The Modular Structure of NARS

# Introduction

This memo is triggered by Joe's refactoring NARS into a library component (which suggests several good ideas), as well as by a few other proposed ways to extend NARS or to connect it to other systems.

Previously, NARS was designed as a stand-alone system that communicates with a user through a GUI. For its long-term goal, such a usage surely needs to be extended, and now is the time to prepare for such a move.

The new structure proposed in the following won't be applied to modify the existing code soon, but hopefully it will grow into a design document, and when the design is stable and mature enough, it will be used to integrate the various branches into a unified open-nars code base. For this reason, all the people who are working on the code (or porting it to other languages) may want to move toward this direction, as well as to check if this design will work.

# Overview

Each NARS-based system will consist of a *core* module and one or more *channel adapter* modules. A channel adapter provides the core a channel of communication with something in the environment, which can be a human user, another computer system, or device. The core is independent of any channel adapter, while a channel adapter depends on the core, but not on other adapters. Each module can run in a different thread, or even a different computer.

# The core module

The core consists of the system's memory, inference engine, and control mechanism. In the current NARS code, it includes the following packages
- nars.language (Narsese terms)
- nars.entity (data items)
- nars.storage (data structures)
- nars.inference (rules and functions)
The four packages will be kept, after necessary modifications.

Major refactoring is needed for the nars.main package, which currently include the following classes:
- nars.main.Memory --- it may be moved into storage
- nars.main.Parameters --- it should stay, with minor changes
- nars.main.NARS --- to be modified
- nars.main.Center --- to be modified

I like Joe's idea of merging the relevant methods of the last two classes into nars.main.Reasoner. No matter what the class is called, in the new design what is needed here is a class that serves as the interface between the core and all the channel adapters. It should meet the following considerations:
- Each core can have one or more communication channels, and among them there is a unique control channel. The channels are distinguished by channel IDs.
- Each communication event happens between the core and a specific channel, usually by sending/receiving a NARS task (as a Java Object).
- The control channel can send commands (beside tasks) to the core, which include *run*, *stop*, *steps*, etc. Consequently, if the control channel is the only communication channel of a core, the system can work in *batch processing* mode, programmed by input tasks with *steps* in between, and the system's behavior will be completely determined by the "program" and accurately repeatable. On the contrary, when there are other channels or when the system is commanded to *run*, the processing will be in *real-time*, and the system's behavior in any channel will not be completely predictable or repeatable.
- This interface class should provide centralized access for selected data and status within the system, for the purpose of demo and monitoring. In the current code, such functions are distributed over the system, though invoked together in the MainWindow under menu "View" and "Parameter".
- The interface should allow the core to work with any number of communication channels, and may even allow channels to be added or dropped at run time.


# Channel types and their adapters

A communication channel must belong to one of the following predetermined types, with an adapter to convert NARS tasks to and from various other forms.

## Native

A native channel allows two NARS-based systems to directly exchange tasks as Java objects. It will be used to build multi-agent system among NARS-based agents, which use the same core code, but may have different parameters and different memory contents.

At the conceptual level, the adapter for this channel type does not need to do anything, though at a lower level, it needs to handle communications between threads, or even between computers.

A related change in the core is to remove the static-ness of the variables (as Joe did), so that each NARS-based agent can have its own values.

This adapter can serve as the super class of all the other adapters.

## Parser/Formatter

This type of channel exchanges NARS tasks in their ASCII text form, so the job of the adapter is to convert a string to and from a task.

The input part (Parser) is currently in nars.io.StringParser (or opennars.parser.narsese.NarseseParser).

The output part (Formatter) is distributed in the "toString()" methods of various classes. It is not clear whether it is necessary to move them together.

Both parts use nars.io.Symbols.

If the channel does not connect the core to another system, but to a file, then the communication becomes "reading" or "writing", and the adapter should also handle the "reading/writing speed" by setting the time interval of the adjacent lines, either as a parameter, or according to the information from the core.

## GUI

This type of channel provides human user interface of the system. It is built on top of the Parser/Formatter adapter.

The current code for GUI is in package nars.gui (the windows) and class nars.io.Record (the inference process log).

There will be different GUIs for different purposes: demo, monitor/control, tutor, development, etc, with different functionality, like the current applet vs. application mode distinction.

## Interpreter

This type of channel exchanges NARS tasks in a formal language that is different from Narsese.

An interpreter converts NARS tasks to and from sentences of the target language in a sentence-by-sentence manner. The interpretation is pure syntactic, not semantic, in the sense that the adapter does not need to access the core during the interpretation process.

The interpretation process may either convert the sentences from the target language into Narsese strings (which are then passed to the Parser), or directly build NARS objects (and bypass the Parser). It will also cover reading/writing as a special case.

Joe's LOAN should be able to be handled in this way, and so are future projects on Cyc, WordNet, etc.

Please note that natural language processing won't be handled in this way, because NLP is semantic by nature. So, NARS based NLP cannot be handled by an adapter in isolation, but by the whole system, and most of the work will be done by the core.

## Tool

This type of channel takes out-going NARS tasks as commands or procedure calls for a hardware or software connected to the system.

The adapter is responsible for operator registration, operation execution, and feedback collection (as NARS input tasks).

The existing code (not much) is in package nars.operation, and will be extended as the research proceeds.

Please not that since feedback is collected as the result of certain operations, this adapter is not output-only, but both input and output. It can be seen as a sensorimotor mechanism of the system.

## Backup

The system will need a special non-control channel to backup the memory and parameters into permanent storage, which then can be copied, reloaded, inspected, and even edited directly, outside the NARS system.

There is no code existing for this channel yet, though there are MainWindow menu items for some of the functionalities.