package jhelp.util.list;

/**
 * Stack of integer.<br>
 * More adapted than use {@link java.util.Stack Stack&lt;Integer&gt;}
 * 
 * @author JHelp
 */
public class StackInt
{
   /** Stack size */
   private int   size;
   /** Stack itself */
   private int[] stack;

   /**
    * Create a new instance of StackInt
    */
   public StackInt()
   {
      this(128);
   }

   /**
    * Create a new instance of StackInt
    * 
    * @param size
    *           Start size
    */
   public StackInt(final int size)
   {
      this.stack = new int[Math.max(size, 128)];
      this.size = 0;
   }

   /**
    * Expand the stack is need
    * 
    * @param more
    *           Number of element will add
    */
   private void expand(final int more)
   {
      if((this.size + more) >= this.stack.length)
      {
         int s = this.size + more;
         s += (s / 10) + 1;
         final int[] temp = new int[s];
         System.arraycopy(this.stack, 0, temp, 0, this.size);
         this.stack = temp;
      }
   }

   /**
    * Stack size
    * 
    * @return Stack size
    */
   public int getSize()
   {
      return this.size;
   }

   /**
    * Indicates if stack is empty
    * 
    * @return {@code true} if stack is empty
    */
   public boolean isEmpty()
   {
      return this.size == 0;
   }

   /**
    * Obtain the integer at the top of the stack and remove it from the stack
    * 
    * @return Integer on the top
    */
   public int pop()
   {
      if(this.size <= 0)
      {
         throw new IllegalStateException("Stack is empty");
      }

      this.size--;
      return this.stack[this.size];
   }

   /**
    * Push integer on the top of the stack
    * 
    * @param integer
    *           Integer to store on the top
    */
   public void push(final int integer)
   {
      this.expand(1);

      this.stack[this.size] = integer;
      this.size++;
   }
}