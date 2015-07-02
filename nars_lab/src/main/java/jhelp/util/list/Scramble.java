/**
 * Project : unit.test<br>
 * Package : jhelp.unitTests<br>
 * Class : Scramble<br>
 * Date : 15 fevr. 2009<br>
 * By JHelp
 */
package jhelp.util.list;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Utilities for scramble<br>
 * <br>
 * Last modification : 15 fevr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class Scramble
{
   /**
    * Scramble an array list
    * 
    * @param <T>
    *           Element type
    * @param arrayList
    *           List to scramble
    */
   public static <T> void scramble(final ArrayList<T> arrayList)
   {
      if((arrayList == null) || (arrayList.size() < 2))
      {
         return;
      }
      final int length = arrayList.size();
      final int time = (length << 1) + (length >> 3) + 1;
      int first;
      int second;
      T temp;
      for(int i = 0; i < time; i++)
      {
         first = (int) (Math.random() * length);
         second = (int) (Math.random() * length);
         if(first != second)
         {
            temp = arrayList.get(first);
            arrayList.set(first, arrayList.get(second));
            arrayList.set(second, temp);
         }
      }
      temp = null;
   }

   /**
    * Scramble an array of integers
    * 
    * @param integers
    *           Array to scramble
    */
   public static void scramble(final int[] integers)
   {
      if((integers == null) || (integers.length < 2))
      {
         return;
      }
      final int length = integers.length;
      final int time = (length << 1) + (length >> 3) + 1;
      int first;
      int second;
      int temp;
      for(int i = 0; i < time; i++)
      {
         first = (int) (Math.random() * length);
         second = (int) (Math.random() * length);
         if(first != second)
         {
            temp = integers[first];
            integers[first] = integers[second];
            integers[second] = temp;
         }
      }
   }

   /**
    * Scramble an array
    * 
    * @param <T>
    *           Type of element
    * @param objects
    *           Array to scramble
    */
   public static <T> void scramble(final T[] objects)
   {
      if((objects == null) || (objects.length < 2))
      {
         return;
      }
      final int length = objects.length;
      final int time = (length << 1) + (length >> 3) + 1;
      int first;
      int second;
      T temp;
      for(int i = 0; i < time; i++)
      {
         first = (int) (Math.random() * length);
         second = (int) (Math.random() * length);
         if(first != second)
         {
            temp = objects[first];
            objects[first] = objects[second];
            objects[second] = temp;
         }
      }
      temp = null;
   }

   /**
    * Scramble a vector
    * 
    * @param <T>
    *           Element type
    * @param vector
    *           Vector to scramble
    */
   public static <T> void scramble(final Vector<T> vector)
   {
      if((vector == null) || (vector.size() < 2))
      {
         return;
      }
      final int length = vector.size();
      final int time = (length << 1) + (length >> 3) + 1;
      int first;
      int second;
      T temp;
      for(int i = 0; i < time; i++)
      {
         first = (int) (Math.random() * length);
         second = (int) (Math.random() * length);
         if(first != second)
         {
            temp = vector.get(first);
            vector.set(first, vector.get(second));
            vector.set(second, temp);
         }
      }
      temp = null;
   }

   /**
    * Scramble an integer array
    * 
    * @param integers
    *           Integer array
    * @return Integer array scrambled
    */
   public static int[] scramble2(final int[] integers)
   {
      Scramble.scramble(integers);

      return integers;
   }
}