 * OpenNARS Home-Page: http://code.google.com/p/open-nars/
 * User Manual (HTML): http://www.cis.temple.edu/~pwang/Implementation/NARS/NARS-GUI-Guide.html
 * Discussion Group: https://groups.google.com/forum/?fromgroups#!forum/open-nars


Contents
--------
 * nars_java - main logic engine
 * nars_gui - java.swing GUI
 * nars_scala - embryo of NARS in Scala (not currently active, just to see how NARS could look in Scala)
 * nars_web - web server
 * nars_test - unit tests
 * nal - examples

In nars_java/ and nars_gui/ are the NARS core and the Swing GUI in Java. This is derived from the code of Pei Wang in nars_java.0/ directory.	


Requirements
------------
 * Java 8
 * ant


Build
-----
To compile, test, and create a complete OpenNARS.jar: build.sh


Test
----
ant test


History
-------
Under the nars_java.0/ directory is the code Pei Wang originally moved into the project, which is still the base of his own programming. This is no active anymore, replaced by nars_java/ and nars_gui/ .

Later Joe Geldart started the nars_java.geldart/ version of NARS, which contains many good ideas (many of which are accepted into 1.5), but it isn't fully consistent with Pei's plan, especially about the new layers (7,8,9), so Pei didn't continue on that code base.



Source Code status
------------------
See also http://code.google.com/p/open-nars/wiki/ProjectStatus

Current version has been fully tested for single capability at a time; there may still be bugs when combining capabilities.

Jean-Marc Vanel is working on this roadmap, mainly in GUI and software engineering tasks :
- reestablish a non-regression test suite
- make an independant syntax verifyer based on a grammar parser : it will give the column & line of error (there is a Scala combinator grammar)
- separate NARS in 2 modules with a Maven build : nars_gui and nars_java
