package jhelp.util.commands;

import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;

import java.io.File;
import java.util.List;

class WindowsCommandsProvider
      implements CommandsProvider
{
   @Override
   public List<String>[] createChangeWallPaperCommands(final File wallpaper)
   {
      // {@todo} TODO Implements createChangeWallPaperCommands
      Debug.printTodo("Implements createChangeWallPaperCommands");
      return null;
   }

   @Override
   public String extractIPFormResultLines(final List<String> linesResult)
   {
      for(final String line : linesResult)
      {
         Debug.println(DebugLevel.VERBOSE, line);
      }

      // {@todo} TODO Implements extractIPFormResultLines in jhelp.util.commands [JHelpUtil]
      Debug.printTodo("Implements extractIPFormResultLines in jhelp.util.commands [JHelpUtil]");

      return "127.0.0.1";
   }

   @Override
   public void fillCommandForGetIP(final List<String> command)
   {
      command.add("ipconfig");
   }

   @Override
   public List<String> openFileExplorerCommand(final File file)
   {
      // {@todo} TODO Implements openFileExplorerCommand
      Debug.printTodo("Implements openFileExplorerCommand");
      return null;
   }
}