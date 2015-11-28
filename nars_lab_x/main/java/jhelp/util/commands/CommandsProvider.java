package jhelp.util.commands;

import java.io.File;
import java.util.List;

interface CommandsProvider
{
   List<String>[] createChangeWallPaperCommands(File wallpaper);

   String extractIPFormResultLines(List<String> linesResult);

   void fillCommandForGetIP(List<String> command);

   List<String> openFileExplorerCommand(File file);
}