package jhelp.util.math.formal;

import jhelp.util.list.SortedArray;

/**
 * Variable list<br>
 * <br>
 * <br>
 * 
 * @author JHelp
 */
public class VariableList
{
   /**
    * Variable list
    */
   private final SortedArray<Variable> list;

   /**
    * Create empty list
    */
   public VariableList()
   {
      this.list = new SortedArray<Variable>(Variable.class, true);
   }

   /**
    * Add a variable list
    * 
    * @param list
    *           List to add
    */
   public void add(final String... list)
   {
      final int nb = list.length;
      for(int i = 0; i < nb; i++)
      {
         this.add(list[i]);
      }
   }

   /**
    * Add a variable
    * 
    * @param variable
    *           Variable to add
    */
   public void add(final String variable)
   {
      this.add(new Variable(variable));
   }

   /**
    * Add a variable
    * 
    * @param variable
    *           Variable to add
    */
   public void add(final Variable variable)
   {
      this.list.add(variable);
   }

   /**
    * Add a variable list
    * 
    * @param list
    *           List to add
    */
   public void add(final VariableList list)
   {
      final int nb = list.numberOfVariables();
      for(int i = 0; i < nb; i++)
      {
         this.add(list.get(i));
      }
   }

   /**
    * Clear the list
    */
   public void clear()
   {
      this.list.clear();
   }

   /**
    * Indicates if a variable is in the list
    * 
    * @param variable
    *           Tested variable
    * @return {@code true} if a variable is in the list
    */
   public boolean contains(final Variable variable)
   {
      return this.list.contains(variable);
   }

   /**
    * Indicates if an object is equals to the variable list <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param object
    *           Object to compare
    * @return {@code true} if equals
    * @see Object#equals(Object)
    */
   @Override
   public boolean equals(final Object object)
   {
      if(object == null)
      {
         return false;
      }

      if(this == object)
      {
         return true;
      }

      if((object instanceof VariableList) == false)
      {
         return false;
      }

      return this.equals((VariableList) object);
   }

   /**
    * Indicates if a variable list is equals to the variable list
    * 
    * @param variableList
    *           Variable list to compare
    * @return {@code true} if equals
    */
   public boolean equals(final VariableList variableList)
   {
      if(variableList == null)
      {
         return false;
      }

      if(this == variableList)
      {
         return true;
      }

      final int size = this.list.getSize();

      if(size != variableList.numberOfVariables())
      {
         return false;
      }

      for(int i = 0; i < size; i++)
      {
         if(this.list.getElement(i).equals(variableList.list.getElement(i)) == false)
         {
            return false;
         }
      }

      return true;
   }

   /**
    * Obtain a variable from list
    * 
    * @param index
    *           Variable index
    * @return Variable at the index
    */
   public Variable get(final int index)
   {
      return this.list.getElement(index);
   }

   /**
    * Number of variable inside the list
    * 
    * @return Number of variable inside the list
    */
   public int numberOfVariables()
   {
      return this.list.getSize();
   }

   /**
    * String representation
    * 
    * @return String representation
    * @see Object#toString()
    */
   @Override
   public String toString()
   {
      final StringBuffer sb = new StringBuffer(123);
      final int nb = this.numberOfVariables();
      for(int i = 0; i < nb; i++)
      {
         sb.append(this.get(i).toString());
         if(i < nb - 1)
         {
            sb.append(" | ");
         }
      }
      return sb.toString();
   }
}