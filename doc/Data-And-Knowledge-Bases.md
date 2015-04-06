> ### Data And Knowledge Bases  
> How to connect NARS to databases and knowledge bases.

***

### Introduction

The current implementation (open-nars 1.5.5) does not use database. The system keeps its whole memory in RAM, and handles input and output using GUI and text files. However, in the future there are several possible ways for databases and knowledge bases to be used in NARS.

### Permanent memory

For NARS to keep its memory from run to run, it is necessary to save the image of the memory in a database, and to start a new system with a preloaded memory. Beside the save and load functions, the database may provide additional functions:

* To display the priority distribution among concepts;

* To display the content of a concept, including tasks and beliefs;

* To make minor modifications to the content of the memory.

One possible way is to swap concepts between RAM and database when the system is running. In this way, the system only keep high-priority concepts in the RAM, and save the low-priority concepts in the database.

Since the units of data are belief, task, and concept, and the database also need to keep the priority distribution among them, the database should be object-oriented, rather than relational.

### Knowledge source

Another way to use database in NARS is to store knowledge to be read into the system. Different from the database that stores the memory image, a knowledge base of NARS can have any internal structure, as far as its query result can be translated into Narsese sentences. Examples of knowledge base include WordNet, Cyc, and DBpedia. It is also possible to build database containing Narsese sentences that are converted from other sources, and use it to train a NARS.

There are two major modes for NARS to get knowledge from such a knowledge base:

1. Query driven, that is, use it to answer the system's questions;

2. Knowledge browsing, that is, systematically convert part or all of its content into the system's experience.

In the browsing mode, the knowledge base only consists part of the system's experience, without blocking the other input channels. It is like reading a book, in the sense that the system not only inserts the new knowledge into memory, but also carries out inference triggered by the knowledge. Therefore, to keep a proper "browsing speed" is necessary.

### JDBC

http://en.wikipedia.org/wiki/Java_Database_Connectivity http://docs.oracle.com/javase/7/docs/api/java/sql/package-summary.html http://docs.oracle.com/javase/7/docs/api/javax/sql/package-summary.html

JDBC is a Java standard for connecting to databases - of all kinds, not just SQL. For example, there exist JDBC interfaces to other types of databases (NoSQL) and also data sources like CSV (comma separated value) and the file system. So if NARS can completely access the JDBC interface (query and results), and possibly create a "connect string" (usually including host, username, and password) for dynamically creating a connection, it will have much data available.

* MySQL http://dev.mysql.com/downloads/connector/j/

* PostgreSQL http://jdbc.postgresql.org/

* CSV http://csvjdbc.sourceforge.net/

* MongoDB - http://www.unityjdbc.com/mongojdbc/mongo_jdbc.php

* File System https://code.google.com/p/jdbcfolder/

* Other - http://www.databasedrivers.com/jdbc/

* Other - http://www.oracle.com/technetwork/java/index-136695.html#

A database's schema which should also be accessible via JDBC will also inform NARS.

### Experience record

The current implementation already has the ability of reading experience from a text file, as well as writing experience and responses into a text file. A database can be used to organize such contents, as well as to provide additional functions.