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

import java.nio.ByteBuffer;

/**
 * A class that neatly creates Tries and will hide which Trie implementation is
 * returned.
 * 
 * @author Philip Diffenderfer
 * 
 */
public   enum Tries
{
   ;

   /**
    * Creates a Trie where the keys are case-sensitive Strings.
    * 
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<String, T> forStrings()
   {
      return new Trie<>(new TrieSequencerCharSequence<>());
   }

   /**
    * Creates a Trie where the keys are case-sensitive Strings.
    * 
    * @param defaultValue
    *        The default value of the Trie is the value returned when
    *        {@link #get(Object)} or {@link #get(Object, TrieMatch)} is called
    *        and no match was found.
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<String, T> forStrings( T defaultValue )
   {
      return new Trie<>(new TrieSequencerCharSequence<>(), defaultValue);
   }

   /**
    * Creates a Trie where the keys are case-insensitive Strings.
    * 
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<String, T> forInsensitiveStrings()
   {
      return new Trie<>(new TrieSequencerCharSequenceCaseInsensitive<>());
   }

   /**
    * Creates a Trie where the keys are case-insensitive Strings.
    * 
    * @param defaultValue
    *        The default value of the Trie is the value returned when
    *        {@link #get(Object)} or {@link #get(Object, TrieMatch)} is called
    *        and no match was found.
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<String, T> forInsensitiveStrings( T defaultValue )
   {
      return new Trie<>(new TrieSequencerCharSequenceCaseInsensitive<>(), defaultValue);
   }

   /**
    * Creates a Trie where the keys are case-sensitive character arrays.
    * 
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<char[], T> forChars()
   {
      return new Trie<>(new TrieSequencerCharArray());
   }

   /**
    * Creates a Trie where the keys are case-sensitive character arrays.
    * 
    * @param defaultValue
    *        The default value of the Trie is the value returned when
    *        {@link #get(Object)} or {@link #get(Object, TrieMatch)} is called
    *        and no match was found.
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<char[], T> forChars( T defaultValue )
   {
      return new Trie<>(new TrieSequencerCharArray(), defaultValue);
   }

   /**
    * Creates a Trie where the keys are case-insensitive character arrays.
    * 
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<char[], T> forInsensitiveChars()
   {
      return new Trie<>(new TrieSequencerCharArrayCaseInsensitive());
   }

   /**
    * Creates a Trie where the keys are case-insensitive character arrays.
    * 
    * @param defaultValue
    *        The default value of the Trie is the value returned when
    *        {@link #get(Object)} or {@link #get(Object, TrieMatch)} is called
    *        and no match was found.
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<char[], T> forInsensitiveChars( T defaultValue )
   {
      return new Trie<>(new TrieSequencerCharArrayCaseInsensitive(), defaultValue);
   }

   /**
    * Creates a Trie where the keys are bytes.
    * 
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<byte[], T> forBytes()
   {
      return new Trie<>(new TrieSequencerByteArray());
   }

   /**
    * Creates a Trie where the keys are bytes.
    * 
    * @param defaultValue
    *        The default value of the Trie is the value returned when
    *        {@link #get(Object)} or {@link #get(Object, TrieMatch)} is called
    *        and no match was found.
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<byte[], T> forBytes( T defaultValue )
   {
      return new Trie<>(new TrieSequencerByteArray(), defaultValue);
   }

   /**
    * Creates a Trie where the keys are shorts.
    * 
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<short[], T> forShorts()
   {
      return new Trie<>(new TrieSequencerShortArray());
   }

   /**
    * Creates a Trie where the keys are shorts.
    * 
    * @param defaultValue
    *        The default value of the Trie is the value returned when
    *        {@link #get(Object)} or {@link #get(Object, TrieMatch)} is called
    *        and no match was found.
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<short[], T> forShorts( T defaultValue )
   {
      return new Trie<>(new TrieSequencerShortArray(), defaultValue);
   }

   /**
    * Creates a Trie where the keys are integers.
    * 
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<int[], T> forInts()
   {
      return new Trie<>(new TrieSequencerIntArray());
   }

   /**
    * Creates a Trie where the keys are integers.
    * 
    * @param defaultValue
    *        The default value of the Trie is the value returned when
    *        {@link #get(Object)} or {@link #get(Object, TrieMatch)} is called
    *        and no match was found.
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<int[], T> forInts( T defaultValue )
   {
      return new Trie<>(new TrieSequencerIntArray(), defaultValue);
   }

   /**
    * Creates a Trie where the keys are longs.
    * 
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<long[], T> forLongs()
   {
      return new Trie<>(new TrieSequencerLongArray());
   }

   /**
    * Creates a Trie where the keys are longs.
    * 
    * @param defaultValue
    *        The default value of the Trie is the value returned when
    *        {@link #get(Object)} or {@link #get(Object, TrieMatch)} is called
    *        and no match was found.
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<long[], T> forLongs( T defaultValue )
   {
      return new Trie<>(new TrieSequencerLongArray(), defaultValue);
   }

   /**
    * Creates a Trie where the keys are ByteBuffers.
    * 
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<ByteBuffer, T> forByteBuffers()
   {
      return new Trie<>(new TrieSequencerByteBuffer());
   }

   /**
    * Creates a Trie where the keys are ByteBuffers.
    * 
    * @param defaultValue
    *        The default value of the Trie is the value returned when
    *        {@link #get(Object)} or {@link #get(Object, TrieMatch)} is called
    *        and no match was found.
    * @return The reference to a newly instantiated Trie.
    */
   public static <T> Trie<ByteBuffer, T> forByteBuffers( T defaultValue )
   {
      return new Trie<>(new TrieSequencerByteBuffer(), defaultValue);
   }

}
