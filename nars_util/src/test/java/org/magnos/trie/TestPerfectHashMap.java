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

import static org.junit.Assert.*;


public class TestPerfectHashMap
{

   @Test
   public void testEmpty()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();

      assertEquals( 0, map.capacity() );
      assertEquals( 0, map.size() );
      assertNull( map.get( 0 ) );
      assertNull( map.get( 1 ) );
      assertNull( map.get( 2 ) );
   }

   @Test
   public void testOne()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();

      map.put( 45, "Hello World!" );

      assertEquals( 45, map.getMin() );
      assertEquals( 45, map.getMax() );
      assertEquals( 1, map.size() );
      assertEquals( 1, map.capacity() );

      assertEquals( "Hello World!", map.get( 45 ) );
   }

   @Test
   public void testFirstConstructor()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>(45, "Hello World!");

      assertEquals( 45, map.getMin() );
      assertEquals( 45, map.getMax() );
      assertEquals( 1, map.size() );
      assertEquals( 1, map.capacity() );

      assertEquals( "Hello World!", map.get( 45 ) );
   }

   @Test
   public void testPutAfter()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();

      map.put( 45, "First" );

      map.put( 47, "Second" );

      assertEquals( 2, map.size() );
      assertEquals( 3, map.capacity() );
      assertEquals( 45, map.getMin() );
      assertEquals( 47, map.getMax() );
      assertEquals( "First", map.get( 45 ) );
      assertEquals( "Second", map.get( 47 ) );

   }

   @Test
   public void testPutBefore()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();

      map.put( 45, "First" );

      map.put( 47, "Second" );

      assertEquals( 2, map.size() );
      assertEquals( 3, map.capacity() );
      assertEquals( 45, map.getMin() );
      assertEquals( 47, map.getMax() );
      assertEquals( "First", map.get( 45 ) );
      assertEquals( "Second", map.get( 47 ) );

      map.put( 42, "Third" );

      assertEquals( 3, map.size() );
      assertEquals( 6, map.capacity() );
      assertEquals( 42, map.getMin() );
      assertEquals( 47, map.getMax() );
      assertEquals( "First", map.get( 45 ) );
      assertEquals( "Second", map.get( 47 ) );
      assertEquals( "Third", map.get( 42 ) );
   }

   @Test
   public void testPutMiddle()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();

      map.put( 45, "First" );

      map.put( 47, "Second" );

      assertEquals( 2, map.size() );
      assertEquals( 3, map.capacity() );
      assertEquals( 45, map.getMin() );
      assertEquals( 47, map.getMax() );
      assertEquals( "First", map.get( 45 ) );
      assertEquals( "Second", map.get( 47 ) );

      map.put( 46, "Third" );

      assertEquals( 3, map.size() );
      assertEquals( 3, map.capacity() );
      assertEquals( 45, map.getMin() );
      assertEquals( 47, map.getMax() );
      assertEquals( "First", map.get( 45 ) );
      assertEquals( "Second", map.get( 47 ) );
      assertEquals( "Third", map.get( 46 ) );
   }

   @Test
   public void testExists()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();
      map.put( -14, "First" );
      map.put( -11, "Second" );
      map.put( -10, "Third" );
      
      assertFalse( map.exists( -15  ) );
      assertTrue( map.exists( -14  ) );
      assertFalse( map.exists( -13  ) );
      assertFalse( map.exists( -12  ) );
      assertTrue( map.exists( -11  ) );
      assertTrue( map.exists( -10  ) );
      assertFalse( map.exists( -9  ) );
      assertFalse( map.exists( -8  ) );
   }
   
   @Test
   public void testRemoveFirst()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();
      map.put( -14, "First" );
      map.put( -11, "Second" );
      map.put( -10, "Third" );

      assertEquals( -14, map.getMin() );
      assertEquals( "First", map.get( -14 ) );
      assertEquals( 3, map.size() );
      
      map.remove( -14 );

      assertEquals( -11, map.getMin() );
      assertNull( map.get( -14 ) );

      assertEquals( 2, map.size() );
      assertEquals( "Second", map.get( -11 ) );
      assertEquals( "Third", map.get( -10 ) );
   }

   @Test
   public void testRemoveMiddle()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();
      map.put( -14, "First" );
      map.put( -11, "Second" );
      map.put( -10, "Third" );

      assertEquals( -14, map.getMin() );
      assertEquals( "Second", map.get( -11 ) );
      assertEquals( 3, map.size() );
      
      map.remove( -11 );

      assertEquals( -14, map.getMin() );
      assertNull( map.get( -11 ) );

      assertEquals( 2, map.size() );
      assertEquals( "First", map.get( -14 ) );
      assertEquals( "Third", map.get( -10 ) );
   }

   @Test
   public void testRemoveLast()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();
      map.put( -14, "First" );
      map.put( -11, "Second" );
      map.put( -10, "Third" );

      assertEquals( -14, map.getMin() );
      assertEquals( -10, map.getMax() );
      assertEquals( "Third", map.get( -10 ) );
      assertEquals( 3, map.size() );
      
      map.remove( -10 );

      assertEquals( -14, map.getMin() );
      assertEquals( -11, map.getMax() );
      assertNull( map.get( -10 ) );

      assertEquals( 2, map.size() );
      assertEquals( "First", map.get( -14 ) );
      assertEquals( "Second", map.get( -11 ) );
   }

   @Test
   public void testRemoveAll()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();
      map.put( -14, "First" );
      map.put( -11, "Second" );
      map.put( -10, "Third" );

      map.remove( -11 );
      map.remove( -14 );
      map.remove( -10 );
      
      assertEquals( 0, map.size() );
      assertEquals( 0, map.capacity() );
      assertTrue( map.isEmpty() );
      
      assertFalse( map.exists( -14 ) );
      assertFalse( map.exists( -11 ) );
      assertFalse( map.exists( -10 ) );
   }

   @Test
   public void testClear()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();
      map.put( -14, "First" );
      map.put( -11, "Second" );
      map.put( -10, "Third" );
      map.clear();
      
      assertEquals( 0, map.size() );
      assertEquals( 0, map.capacity() );
      assertTrue( map.isEmpty() );
      
      assertFalse( map.exists( -14 ) );
      assertFalse( map.exists( -11 ) );
      assertFalse( map.exists( -10 ) );
   }

}
