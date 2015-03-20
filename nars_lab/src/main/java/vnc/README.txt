
TightVNC Java Viewer version 2.7.2
Copyright (C) 2011, 2012 GlavSoft LLC. All rights reserved.
======================================================================

This software is distributed under the GNU General Public Licence
as published by the Free Software Foundation. Please see the file
LICENCE.txt for the exact licensing terms and conditions.

The license does not permit incorporating this software into
proprietary programs. If you wish to do so, please order commercial
source code license. See the details here:

  http://www.tightvnc.com/licensing/


Using TightVNC Java Viewer
~~~~~~~~~~~~~~~~~~~~~~~~~~
TightVNC Java Viewer can work either as a normal application or as
an applet. To run it as an application, just execute the JAR file
(tightvnc-jviewer.jar), e.g. double-click it in the Windows Explorer.
If Java Runtime is installed on your computer correctly, that should
be enough. You will be prompted for the TightVNC Server to connect to.

If you would like to start the viewer from the command line, here are
a few examples:

  java -jar tightvnc-jviewer.jar
  java -jar tightvnc-jviewer.jar hostname
  java -jar tightvnc-jviewer.jar -port=nnn hostname
  java -jar tightvnc-jviewer.jar -port=nnn -host=hostname

... where hostname and nnn should be replaced with the actual hostname
and port number correspondingly. Note that you can use an IP address
as a hostname. Port 5900 will be used if not specified.

Important: the syntax like hostname:display or hostname::port is not
           supported in this version of TightVNC Java Viewer.

For more command line params info run:

  java -jar tightvnc-jviewer.jar -help

Finally, if you would like to use the viewer as an applet, please see
the example HTML page included (viewer-applet-example.html).

======================================================================

Thank you for using TightVNC!

Visit our Web site:    http://www.tightvnc.com/
Follow us on Twitter:  http://www.twitter.com/tighvnc
