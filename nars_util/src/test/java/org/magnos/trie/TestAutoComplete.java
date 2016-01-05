package org.magnos.trie;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;


public enum TestAutoComplete
{
   ;

   public static void main(String[] args)
   {
      Scanner dictionaryInput = new Scanner( TestAutoComplete.class.getResourceAsStream( "dictionary.txt" ) );
      List<String> dictionary = new ArrayList<>();

      long t0 = System.nanoTime();
      
      while (dictionaryInput.hasNextLine())
      {
         dictionary.add( dictionaryInput.nextLine() );
      }
      
      dictionaryInput.close();
      
      long t1 = System.nanoTime();
      
      Trie<String, Boolean> trie = Tries.forInsensitiveStrings( Boolean.FALSE );
      
      for (String word : dictionary)
      {
         trie.put( word, Boolean.TRUE );
      }
      
      long t2 = System.nanoTime();
      
      System.out.format( "Dictionary of %d words loaded in %.9f seconds.\n", dictionary.size(), (t1 - t0) * 0.000000001 );
      System.out.format( "Trie built in %.9f seconds.\n", (t2 - t1) * 0.000000001 );
      
      Scanner in = new Scanner( System.in );
      
      while (in.hasNextLine())
      {
         String line = in.nextLine();
     
         long t3 = System.nanoTime();
         Set<String> keys = trie.keySet( line, TrieMatch.PARTIAL );
         System.out.print( keys );
         long t4 = System.nanoTime();
         System.out.format( " with %d items in %.9f seconds.\n", keys.size(), (t4 - t3) * 0.000000001 );
      }
      
      in.close();
   }
   
}
