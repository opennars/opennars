package jhelp.util.commands;

import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.list.ListFromArray;

import java.io.File;
import java.util.List;

class LinuxCommandsProvider
      implements CommandsProvider
{
   private final static String INET_ADDRESS        = "inet adr:";
   private static final int    INET_ADDRESS_LENGTH = LinuxCommandsProvider.INET_ADDRESS.length();
   private final static String INGNORE             = "lo";

   @Override
   public List<String>[] createChangeWallPaperCommands(final File wallpaper)
   {
      final List<String>[] commands = new List[2];

      final String path = wallpaper.getAbsolutePath();
      commands[0] = new ListFromArray<String>("/usr/bin/gsettings", "set", "org.gnome.desktop.background", "picture-uri", "file://" + path);
      commands[1] = new ListFromArray<String>("/usr/bin/gconftool-2", "--type=string", "--set", "/desktop/gnome/background/picture_filename", path);

      return commands;
   }

   @Override
   public String extractIPFormResultLines(final List<String> linesResult)
   {
      Debug.printMark(DebugLevel.VERBOSE, "RESULT");

      boolean ignore = true;
      int indexStart, indexEnd;

      for(final String line : linesResult)
      {
         Debug.println(DebugLevel.VERBOSE, line);

         if(line.charAt(0) > 32)
         {
            ignore = line.startsWith(LinuxCommandsProvider.INGNORE);
         }

         if(ignore == false)
         {
            indexStart = line.indexOf(LinuxCommandsProvider.INET_ADDRESS);

            if(indexStart >= 0)
            {
               indexStart += LinuxCommandsProvider.INET_ADDRESS_LENGTH;

               indexEnd = line.indexOf(' ', indexStart);
               if(indexEnd < 0)
               {
                  indexEnd = line.length();
               }

               return line.substring(indexStart, indexEnd);
            }
         }
      }

      return "127.0.0.1";
   }

   @Override
   public void fillCommandForGetIP(final List<String> command)
   {
      command.add("ifconfig");
   }

   @Override
   public List<String> openFileExplorerCommand(File file)
   {
      if(file.isDirectory() == false)
      {
         file = file.getParentFile();
      }

      return new ListFromArray<String>("nautilus", file.getAbsolutePath());
   }

}