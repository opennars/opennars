/* 
 * NOTICE OF LICENSE
 * 
 * This source file is subject to the Open Software License (OSL 3.0) that is 
 * bundled with this package in the file LICENSE.txt. It is also available 
 * through the world-wide-web at http://opensource.org/licenses/osl-3.0.php
 * If you did not receive a copy of the license and are unable to obtain it 
 * through the world-wide-web, please send an email to magnos.software@gmail.com 
 * so we can send you a copy immediately. If you use any of this software please
 * notify me via our website or email, your feedback is much appreciated. 
 * 
 * @copyright   Copyright (c) 2011 Magnos Software (http://www.magnos.org)
 * @license     http://opensource.org/licenses/osl-3.0.php
 *              Open Software License (OSL 3.0)
 */

package org.magnos.trie;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


public class TestTrieSet
{

   @Test
   public void testConstructor()
   {
      TrieSet<String> set = new TrieSet<>(Tries.forStrings());

      assertTrue( set.isEmpty() );
      assertEquals( 0, set.size() );
   }

   @Test
   public void testAdd()
   {
      TrieSet<String> set = new TrieSet<>(Tries.forStrings());

      assertFalse( set.contains( "meow" ) );
      assertTrue( set.add( "meow" ) );
      assertTrue( set.contains( "meow" ) );
      assertFalse( set.isEmpty() );
      assertEquals( 1, set.size() );

      assertFalse( set.contains( "meowa" ) );
      assertFalse( set.contains( "meo" ) );
      assertFalse( set.contains( "me" ) );
      assertFalse( set.contains( "m" ) );
   }

   @Test
   public void testAddAll()
   {
      TrieSet<String> set = new TrieSet<>(Tries.forStrings());

      List<String> words = Arrays.asList( "meow", "kitten", "purr" );

      set.addAll( words );

      assertEquals( 3, set.size() );
      assertTrue( set.contains( "meow" ) );
      assertTrue( set.contains( "kitten" ) );
      assertTrue( set.contains( "purr" ) );
   }

   @Test
   public void testClear()
   {
      TrieSet<String> set = new TrieSet<>(Tries.forStrings());

      set.add( "meow" );
      set.add( "kitten" );
      set.add( "purr" );

      assertEquals( 3, set.size() );
      assertTrue( set.contains( "meow" ) );
      assertTrue( set.contains( "kitten" ) );
      assertTrue( set.contains( "purr" ) );

      set.clear();

      assertEquals( 0, set.size() );
      assertTrue( set.isEmpty() );
      assertFalse( set.contains( "meow" ) );
      assertFalse( set.contains( "kitten" ) );
      assertFalse( set.contains( "purr" ) );
   }

   @Test
   public void testContainsAll()
   {
      TrieSet<String> set = new TrieSet<>(Tries.forStrings());

      set.add( "meow" );
      set.add( "kitten" );
      set.add( "purr" );

      assertTrue( set.containsAll( Arrays.asList( "meow" ) ) );
      assertTrue( set.containsAll( Arrays.asList( "meow", "kitten" ) ) );
      assertTrue( set.containsAll( Arrays.asList( "meow", "kitten", "purr" ) ) );
      assertTrue( set.containsAll( Arrays.asList( "purr" ) ) );
      assertTrue( set.containsAll( Arrays.asList() ) );

      assertFalse( set.containsAll( Arrays.asList( "NOPE" ) ) );
      assertFalse( set.containsAll( Arrays.asList( "meow", "NOPE" ) ) );
      assertFalse( set.containsAll( Arrays.asList( "meow", "kitten", "NOPE" ) ) );
      assertFalse( set.containsAll( Arrays.asList( "meow", "kitten", "purr", "NOPE" ) ) );
      assertFalse( set.containsAll( Arrays.asList( "purr", "NOPE" ) ) );
   }
   
   @Test
   public void testIterator()
   {
      TrieSet<String> set = new TrieSet<>(Tries.forStrings());

      set.add( "meow" );
      set.add( "kitten" );
      set.add( "purr" );
    
      String expected = "kittenmeowpurr";
      String actual = "";
      
      for (String key : set)
      {
         actual += key;
      }
      
      assertEquals( expected, actual );
   }
   
   @Test
   public void testRemove()
   {
      TrieSet<String> set = new TrieSet<>(Tries.forStrings());

      set.add( "meow" );
      set.add( "kitten" );
      set.add( "purr" );
      
      assertTrue( set.contains( "meow" ) );
      assertTrue( set.remove( "meow" ) );
      assertFalse( set.contains( "meow" ) );
      assertEquals( 2, set.size() );
   }
   
   @Test
   public void testRemoveAll()
   {
      TrieSet<String> set = new TrieSet<>(Tries.forStrings());

      set.add( "meow" );
      set.add( "kitten" );
      set.add( "purr" );
      
      set.removeAll( Arrays.asList( "meow", "kitten", "NOPE" ) );
      
      assertEquals( 1, set.size() );
      assertTrue( set.contains( "purr" ) );
      assertFalse( set.contains( "meow" ) );
      assertFalse( set.contains( "kitten" ) );
   }
   
   @Test
   public void testRetainAll()
   {
      TrieSet<String> set = new TrieSet<>(Tries.forStrings());
      
      set.add( "meow" );
      set.add( "kitten" );
      set.add( "purr" );
      
      set = set.retainsAll( Arrays.asList( "meow", "kitten", "NOPE" ) );

      assertEquals( 2, set.size() );
      assertFalse( set.contains( "purr" ) );
      assertTrue( set.contains( "meow" ) );
      assertTrue( set.contains( "kitten" ) );
      assertFalse( set.contains( "NOPE" ) );
   }
   

}
