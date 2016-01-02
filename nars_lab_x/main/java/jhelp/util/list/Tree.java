package jhelp.util.list;

/**
 * A generic tree
 * 
 * @author JHelp
 * @param <INFORMATION>
 *           Information carry type
 */
public class Tree<INFORMATION>
{
   /**
    * Comparator used to compare 2 branches on using there carry information
    * 
    * @author JHelp
    * @param <INFO>
    *           Information type
    */
   static class ComparatorTree<INFO>
         implements Comparator<Tree<INFO>>
   {
      /** Comparator used to compare 2 information */
      Comparator<INFO> comparator;

      /**
       * Create a new instance of ComparatorTree
       */
      ComparatorTree()
      {
      }

      /**
       * Compare 2 branches.<br>
       * It returns a value :
       * <table>
       * <tr>
       * <th>&lt;0</th>
       * <td>:</td>
       * <td>If first branch if before second branch</td>
       * </tr>
       * <tr>
       * <th>0</th>
       * <td>:</td>
       * <td>If the 2 branches are the same</td>
       * </tr>
       * <tr>
       * <th>&gt;0</th>
       * <td>:</td>
       * <td>If first branch if after second branch</td>
       * </tr>
       * </table>
       * 
       * @param tree1
       *           First branch
       * @param tree2
       *           Second branch
       * @return Comparison value
       */
      @Override
      public int compare(final Tree<INFO> tree1, final Tree<INFO> tree2)
      {
         return this.comparator.compare(tree1.information, tree2.information);
      }
   }

   /**
    * Method of search inside the tree.<br>
    * In search explanation, we explain how the order of eploring the folling tree :
    * 
    * <pre>
    *      A
    *      |
    *   -------
    *   |     |
    *   B     C
    *   |     |
    *  ---   ---
    *  | |   | |
    *  D E   F G
    * </pre>
    * 
    * See {@link Tree#searchBranch(TestFoundListener, SearchMode, boolean)}
    * 
    * @author JHelp
    */
   public static enum SearchMode
   {
      /** Search order : A B D E C F G */
      LEFT_TO_RIGHT_DEPTH,
      /** Search order : A B C D E F G */
      LEFT_TO_RIGHT_HIGH,
      /** Search order : A C G F B E D */
      RIGHT_TO_LEFT_DEPTH,
      /** Search order : A C B G F E D */
      RIGHT_TO_LEFT_HIGH
   }

   /**
    * Listener called each tile we test an information to know if this information is the searched one. See
    * {@link Tree#searchBranch(TestFoundListener, SearchMode, boolean)}
    * 
    * @author JHelp
    * @param <INFO>
    *           Information type
    */
   public static interface TestFoundListener<INFO>
   {
      /**
       * Called to test if an information is the searched one
       * 
       * @param information
       *           Tested information
       * @return {@code true} if information is the searched one
       */
      public boolean isElementSearched(INFO information);
   }

   /** Tree branches */
   private final ArrayList<Tree<INFORMATION>> branches;
   /** Comparator used to compare 2 branches */
   private final ComparatorTree<INFORMATION>  comparatorTree;
   /** Trunk that contains this tree */
   private final Tree<INFORMATION>            trunk;
   /** Carried information */
   final INFORMATION                          information;

   /**
    * Create a new instance of Tree
    * 
    * @param trunk
    *           Trunk parent
    * @param information
    *           Information carry
    */
   private Tree(final Tree<INFORMATION> trunk, final INFORMATION information)
   {
      if(information == null)
      {
         throw new NullPointerException("information musn't be null");
      }

      this.trunk = trunk;
      this.information = information;
      this.branches = new ArrayList<Tree<INFORMATION>>();
      this.comparatorTree = new ComparatorTree<INFORMATION>();
   }

   /**
    * Create a new instance of Tree
    * 
    * @param information
    *           Information carray (Musn't be {@code null})
    */
   public Tree(final INFORMATION information)
   {
      this(null, information);
   }

   /**
    * Collect all tree leafs and put them in a list
    * 
    * @param list
    *           List where put collected leafs
    */
   private void collectLeafs(final List<Tree<INFORMATION>> list)
   {
      if(this.branches.isEmpty() == true)
      {
         list.add(this);
         return;
      }

      for(final Tree<INFORMATION> branch : this.branches)
      {
         branch.collectLeafs(list);
      }
   }

   /**
    * Search a branch in left to right depth rule. See {@link SearchMode}, the order here is :
    * {@link SearchMode#LEFT_TO_RIGHT_DEPTH}
    * 
    * @param testFoundListener
    *           Tester of information search
    * @return Founded branch or {@code null} if not found
    */
   private Tree<INFORMATION> searchLeftToRightDepth(final TestFoundListener<INFORMATION> testFoundListener)
   {
      final Stack<Tree<INFORMATION>> stack = new Stack<Tree<INFORMATION>>();
      stack.push(this);
      Tree<INFORMATION> tree;

      while(stack.isEmpty() == false)
      {
         tree = stack.pop();

         if(testFoundListener.isElementSearched(tree.information) == true)
         {
            return tree;
         }

         for(final Tree<INFORMATION> branch : tree.branches)
         {
            stack.push(branch);
         }
      }

      return null;
   }

   /**
    * Search a branch in left to right high rule. See {@link SearchMode}, the order here is :
    * {@link SearchMode#LEFT_TO_RIGHT_HIGH}
    * 
    * @param testFoundListener
    *           Tester of information search
    * @return Founded branch or {@code null} if not found
    */
   private Tree<INFORMATION> searchLeftToRightHigh(final TestFoundListener<INFORMATION> testFoundListener)
   {
      final Queue<Tree<INFORMATION>> queue = new Queue<Tree<INFORMATION>>();
      queue.inQueue(this);
      Tree<INFORMATION> tree;

      while(queue.isEmpty() == false)
      {
         tree = queue.outQueue();

         if(testFoundListener.isElementSearched(tree.information) == true)
         {
            return tree;
         }

         for(final Tree<INFORMATION> branch : tree.branches)
         {
            queue.inQueue(branch);
         }
      }

      return null;
   }

   /**
    * Search a branch in right to left depth rule. See {@link SearchMode}, the order here is :
    * {@link SearchMode#RIGHT_TO_LEFT_DEPTH}
    * 
    * @param testFoundListener
    *           Tester of information search
    * @return Founded branch or {@code null} if not found
    */
   private Tree<INFORMATION> searchRightToLeftDepth(final TestFoundListener<INFORMATION> testFoundListener)
   {
      final Stack<Tree<INFORMATION>> stack = new Stack<Tree<INFORMATION>>();
      stack.push(this);
      Tree<INFORMATION> tree;

      while(stack.isEmpty() == false)
      {
         tree = stack.pop();

         if(testFoundListener.isElementSearched(tree.information) == true)
         {
            return tree;
         }

         for(int index = tree.branches.size() - 1; index >= 0; index--)
         {
            stack.push(tree.branches.get(index));
         }
      }

      return null;
   }

   /**
    * Search a branch in right to left high rule. See {@link SearchMode}, the order here is :
    * {@link SearchMode#RIGHT_TO_LEFT_HIGH}
    * 
    * @param testFoundListener
    *           Tester of information search
    * @return Founded branch or {@code null} if not found
    */
   private Tree<INFORMATION> searchRightToLeftHigh(final TestFoundListener<INFORMATION> testFoundListener)
   {
      final Queue<Tree<INFORMATION>> queue = new Queue<Tree<INFORMATION>>();
      queue.inQueue(this);
      Tree<INFORMATION> tree;

      while(queue.isEmpty() == false)
      {
         tree = queue.outQueue();

         if(testFoundListener.isElementSearched(tree.information) == true)
         {
            return tree;
         }

         for(int index = tree.branches.size() - 1; index >= 0; index--)
         {
            queue.inQueue(tree.branches.get(index));
         }
      }

      return null;
   }

   /**
    * Add a branch to the tree
    * 
    * @param information
    *           Information to put on the branch
    * @return Added branch
    */
   public Tree<INFORMATION> addBranch(final INFORMATION information)
   {
      if(information == null)
      {
         throw new NullPointerException("information musn't be null");
      }

      final Tree<INFORMATION> branch = new Tree<INFORMATION>(information);
      this.branches.add(branch);
      return branch;
   }

   /**
    * Add a branch to the tree if ordered way.<br>
    * This method suppose that the tree is already ordered via the given comparator. See {@link #sortBranches(Comparator)}
    * 
    * @param information
    *           Information to put on the branch
    * @param comparator
    *           Comparator to use for know the order
    * @return Added branch
    */
   public Tree<INFORMATION> addBranchSorted(final INFORMATION information, final Comparator<INFORMATION> comparator)
   {
      if(information == null)
      {
         throw new NullPointerException("information musn't be null");
      }

      final Tree<INFORMATION> branch = new Tree<INFORMATION>(information);

      Tree<INFORMATION> tree;

      int max = this.branches.size() - 1;
      if(max < 0)
      {
         this.branches.add(branch);

         return branch;
      }
      tree = this.branches.get(max);

      if(comparator.compare(information, tree.information) >= 0)
      {
         this.branches.add(branch);

         return branch;
      }

      int min = 0;
      tree = this.branches.get(min);

      if(comparator.compare(information, tree.information) <= 0)
      {
         this.branches.add(0, branch);

         return branch;
      }

      int mil, comp;

      while((min + 1) < max)
      {
         mil = (max + min) >> 1;
         tree = this.branches.get(mil);
         comp = comparator.compare(information, tree.information);

         if(comp == 0)
         {
            this.branches.add(mil, branch);

            return branch;
         }

         if(comp < 0)
         {
            max = mil;
         }
         else
         {
            min = mil;
         }
      }

      this.branches.add(max, branch);

      return branch;
   }

   /**
    * Compute the branch weight.<br>
    * More a tree have branch and its branch have branch ,and ... more the weight is high
    * 
    * @return Tree weight
    */
   public int computeWeight()
   {
      int weight = 1;

      for(final Tree<INFORMATION> branch : this.branches)
      {
         weight += branch.computeWeight();
      }

      return weight;
   }

   /**
    * Exchange 2 branch
    * 
    * @param index1
    *           Index of branch 1
    * @param index2
    *           Index of branch 2
    */
   public void exchangeBranch(final int index1, final int index2)
   {
      final Tree<INFORMATION> tree1 = this.branches.get(index1);
      final Tree<INFORMATION> tree2 = this.branches.get(index2);

      this.branches.set(index1, tree2);
      this.branches.set(index2, tree1);
   }

   /**
    * Obtain a branch
    * 
    * @param index
    *           Branch index
    * @return The branch
    */
   public Tree<INFORMATION> getBranch(final int index)
   {
      return this.branches.get(index);
   }

   /**
    * List of branches
    * 
    * @return List of branches
    */
   public EnumerationIterator<Tree<INFORMATION>> getBranches()
   {
      return new EnumerationIterator<Tree<INFORMATION>>(this.branches.iterator());
   }

   /**
    * Compute the index of this tree in its trunk parent.<br>
    * If the tree is a main trunk (have no parent), -1 is return
    * 
    * @return Index of the tree in its trunk or -1 if tree have no trunk (Because it's a main trunk)
    */
   public int getIndexInTrunk()
   {
      if(this.trunk == null)
      {
         return -1;
      }

      final int size = this.trunk.branches.size();
      for(int index = 0; index < size; index++)
      {
         if(this.trunk.branches.get(index) == this)
         {
            return index;
         }
      }

      throw new RuntimeException("Impossible to go there");
   }

   /**
    * Carried information
    * 
    * @return Carried information
    */
   public INFORMATION getInformation()
   {
      return this.information;
   }

   /**
    * Get tree main trunk.<br>
    * The main trunk is the tree who carries all other trees, the tree without trunk parent
    * 
    * @return Main trunk
    */
   public Tree<INFORMATION> getMainTrunk()
   {
      Tree<INFORMATION> tree = this;

      while(tree.trunk != null)
      {
         tree = tree.trunk;
      }

      return tree;
   }

   /**
    * Trunk parent
    * 
    * @return Trunk parent
    */
   public Tree<INFORMATION> getTrunk()
   {
      return this.trunk;
   }

   /**
    * Indicates if the tree is a leaf.<br>
    * In other words, that tree didn't have any branch
    * 
    * @return {@code true} if the tree is a leaf
    */
   public boolean isLeaf()
   {
      return this.branches.isEmpty();
   }

   /**
    * Collect all the tree leaf, no matter how depth they are
    * 
    * @return List of leafs
    */
   public List<Tree<INFORMATION>> listOfLeafs()
   {
      final ArrayList<Tree<INFORMATION>> leafs = new ArrayList<Tree<INFORMATION>>();

      this.collectLeafs(leafs);

      return Collections.unmodifiableList(leafs);
   }

   /**
    * Number branch of the tree
    * 
    * @return Number branch of the tree
    */
   public int numberOfBranch()
   {
      return this.branches.size();
   }

   /**
    * Remove a branch
    * 
    * @param index
    *           Branch index
    * @return Removed branch
    */
   public Tree<INFORMATION> removeBranch(final int index)
   {
      return this.branches.remove(index);
   }

   /**
    * Search a branch by its information
    * 
    * @param testFoundListener
    *           Tester to know if an information is the searched one
    * @param searchMode
    *           Way to search in the tree. See {@link SearchMode}
    * @param remove
    *           Indicates if the searched branch have to be removed from its trunk
    * @return The searched branch or {@code null} if not found
    */
   public Tree<INFORMATION> searchBranch(final TestFoundListener<INFORMATION> testFoundListener, final SearchMode searchMode, final boolean remove)
   {
      if(testFoundListener == null)
      {
         throw new NullPointerException("testFoundListener musn't be null");
      }

      if(searchMode == null)
      {
         throw new NullPointerException("searchMode musn't be null");
      }

      Tree<INFORMATION> branch = null;

      switch(searchMode)
      {
         case LEFT_TO_RIGHT_DEPTH:
            branch = this.searchLeftToRightDepth(testFoundListener);
         case LEFT_TO_RIGHT_HIGH:
            branch = this.searchLeftToRightHigh(testFoundListener);
         case RIGHT_TO_LEFT_DEPTH:
            branch = this.searchRightToLeftDepth(testFoundListener);
         case RIGHT_TO_LEFT_HIGH:
            branch = this.searchRightToLeftHigh(testFoundListener);
      }

      if((remove == true) && (branch != null) && (branch.trunk != null))
      {
         branch.trunk.branches.remove(branch);
      }

      return branch;
   }

   /**
    * Sort branches of the tree
    * 
    * @param comparator
    *           Comparator to know the order
    */
   public void sortBranches(final Comparator<INFORMATION> comparator)
   {
      if(comparator == null)
      {
         throw new NullPointerException("comparator musn't be null");
      }

      this.comparatorTree.comparator = comparator;

      Collections.sort(this.branches, this.comparatorTree);
   }
}