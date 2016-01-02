package jhelp.engine.event;

import jhelp.engine.JHelpSceneRenderer;
import jhelp.engine.Node;
import jhelp.engine.PickUVlistener;
import jhelp.engine.twoD.Object2D;
import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.list.Pair;
import jhelp.util.math.UtilMath;
import jhelp.util.xml.DynamicWriteXML;
import jhelp.xml.ExceptionParseXML;
import jhelp.xml.ExceptionXML;
import jhelp.xml.ParseXMLlistener;
import jhelp.xml.ParserXML;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Sensitive area
 * 
 * @author JHelp
 */
public class SensitiveArea
{
   /**
    * Event manager (Object 2D, Node and pick UV listener)
    * 
    * @author JHelp
    */
   class EventManager
         implements Object2DListener, NodeListener, PickUVlistener
   {
      /** Indicates if action have to be played */
      private boolean doAction;
      /** Indicates if pick is finished */
      private boolean finishPick;

      /**
       * Create a new instance of EventManager
       */
      EventManager()
      {
      }

      /**
       * Called when mouse clicked on a Node <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param node
       *           Node clicked
       * @param leftButton
       *           Left button down status
       * @param rightButton
       *           Right button down status
       * @see NodeListener#mouseClick(jhelp.engine.Node, boolean, boolean)
       */
      @Override
      public void mouseClick(final Node node, final boolean leftButton, final boolean rightButton)
      {
         this.finishPick = true;
         this.doAction = true;
         SensitiveArea.this.pickOn(node);
      }

      /**
       * Called when mouse clicked on a Object 2D <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param object2d
       *           Clicked object 2D
       * @param x
       *           X position
       * @param y
       *           Y position
       * @param leftButton
       *           Left button down status
       * @param rightButton
       *           Right button down status
       * @see jhelp.engine.event.Object2DListener#mouseClick(Object2D, int, int, boolean, boolean)
       */
      @Override
      public void mouseClick(final Object2D object2d, final int x, final int y, final boolean leftButton, final boolean rightButton)
      {
         SensitiveArea.this.actionAt(x, y, true);
      }

      /**
       * Called when mouse dragged on a Object 2D <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param object2d
       *           Dragged object 2D
       * @param x
       *           X position
       * @param y
       *           Y position
       * @param leftButton
       *           Left button down status
       * @param rightButton
       *           Right button down status
       * @see jhelp.engine.event.Object2DListener#mouseDrag(Object2D, int, int, boolean, boolean)
       */
      @Override
      public void mouseDrag(final Object2D object2d, final int x, final int y, final boolean leftButton, final boolean rightButton)
      {
         SensitiveArea.this.actionAt(x, y, false);
      }

      /**
       * Called when mouse enter on Node <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param node
       *           Entered node
       * @see NodeListener#mouseEnter(jhelp.engine.Node)
       */
      @Override
      public void mouseEnter(final Node node)
      {
         this.finishPick = false;
         this.doAction = false;
         SensitiveArea.this.pickOn(node);
      }

      /**
       * Called when mouse enter on Object 2D <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param object2d
       *           Entered Object 2D
       * @param x
       *           Mouse X
       * @param y
       *           Mouse Y
       * @see jhelp.engine.event.Object2DListener#mouseEnter(Object2D, int, int)
       */
      @Override
      public void mouseEnter(final Object2D object2d, final int x, final int y)
      {
         SensitiveArea.this.actionAt(x, y, false);
      }

      /**
       * Called when mouse exit from Node <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param node
       *           Exited node
       * @see NodeListener#mouseExit(jhelp.engine.Node)
       */
      @Override
      public void mouseExit(final Node node)
      {
         this.finishPick = true;
         this.doAction = false;
         SensitiveArea.this.pickOn(node);
      }

      /**
       * Called when mouse exit from Object 2D <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param object2d
       *           Exited Object 2D
       * @param x
       *           Mouse X
       * @param y
       *           Mouse Y
       * @see jhelp.engine.event.Object2DListener#mouseExit(Object2D, int, int)
       */
      @Override
      public void mouseExit(final Object2D object2d, final int x, final int y)
      {
         SensitiveArea.this.actionAt(x, y, false);
      }

      /**
       * /** Called when mouse moved on a Object 2D <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param object2d
       *           Clicked object 2D
       * @param x
       *           X position
       * @param y
       *           Y position
       * @see jhelp.engine.event.Object2DListener#mouseMove(Object2D, int, int)
       */
      @Override
      public void mouseMove(final Object2D object2d, final int x, final int y)
      {
         SensitiveArea.this.actionAt(x, y, false);
      }

      /**
       * Called when UV picked
       * 
       * @param u
       *           U picked
       * @param v
       *           V picked
       * @param node
       *           Node picked
       * @see PickUVlistener#pickUV(int, int, Node)
       */
      @Override
      public void pickUV(final int u, final int v, final Node node)
      {
         final int x = (u * SensitiveArea.this.width) >> 8;
         final int y = (v * SensitiveArea.this.height) >> 8;
         SensitiveArea.this.actionAt(x, y, this.doAction);

         if(this.finishPick == true)
         {
            SensitiveArea.this.finshPick(node);
         }
      }
   }

   /**
    * Listener of parsing sentive area XML representation
    * 
    * @author JHelp
    */
   static class LoadListener
         implements ParseXMLlistener
   {
      /** Created area */
      SensitiveArea sensitiveArea;

      /**
       * Create a new instance of LoadListener
       */
      LoadListener()
      {
      }

      /**
       * Called when comment find in XML file <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param comment
       *           Comment found
       * @see jhelp.xml.ParseXMLlistener#commentFind(String)
       */
      @Override
      public void commentFind(final String comment)
      {
      }

      /**
       * Called when a end of markup meet <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param markupName
       *           Closed markup
       * @throws ExceptionXML
       *            If close that markup now is invalid
       * @see jhelp.xml.ParseXMLlistener#endMarkup(String)
       */
      @Override
      public void endMarkup(final String markupName) throws ExceptionXML
      {
      }

      /**
       * Called when parsing is finished <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @throws ExceptionXML
       *            If end parsing now is invalid
       * @see jhelp.xml.ParseXMLlistener#endParse()
       */
      @Override
      public void endParse() throws ExceptionXML
      {
      }

      /**
       * Called when a fatal exception happen while parsing <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param exceptionParseXML
       *           Exception happen
       * @see jhelp.xml.ParseXMLlistener#exceptionForceEndParse(jhelp.xml.ExceptionParseXML)
       */
      @Override
      public void exceptionForceEndParse(final ExceptionParseXML exceptionParseXML)
      {
         Debug.printException(exceptionParseXML, "Parsing area failed");
      }

      /**
       * Called when markup start meet <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param markupName
       *           Opened markup name
       * @param parameters
       *           Markup parameters
       * @throws ExceptionXML
       *            If the markup shouldn't open yet, or unexpected, or one of parameters missing or invalid
       * @see jhelp.xml.ParseXMLlistener#startMakup(String, Hashtable)
       */
      @Override
      public void startMakup(final String markupName, final Hashtable<String, String> parameters) throws ExceptionXML
      {
         if(SensitiveArea.MARKUP_MAIN.equals(markupName) == true)
         {
            if(this.sensitiveArea != null)
            {
               throw new ExceptionXML("Meet a second " + SensitiveArea.MARKUP_MAIN + " inside the XML");
            }

            this.sensitiveArea = SensitiveArea.createFromMarkup(markupName, parameters);
            return;
         }

         if(this.sensitiveArea == null)
         {
            throw new ExceptionXML("The " + SensitiveArea.MARKUP_MAIN + " must be the first markup, be we meet " + markupName + " before it");
         }

         if(SensitiveArea.MARKUP_AREA.endsWith(markupName) == true)
         {
            this.sensitiveArea.putArea(markupName, parameters);
            return;
         }

         Debug.println(DebugLevel.WARNING, "Don't know what to do with markup ", markupName);
      }

      /**
       * Called when parsing start <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @see jhelp.xml.ParseXMLlistener#startParse()
       */
      @Override
      public void startParse()
      {
      }

      /**
       * Called when text found <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param text
       *           Text read
       * @throws ExceptionXML
       *            If text is invalid
       * @see jhelp.xml.ParseXMLlistener#textFind(String)
       */
      @Override
      public void textFind(final String text) throws ExceptionXML
      {
      }
   }

   /**
    * Represents an area
    * 
    * @author JHelp
    */
   public class Area
   {
      /** Developer additional information */
      private Object developperInformation;
      /** Area height */
      int            height;
      /** Area ID */
      final int      id;
      /** Area width */
      int            width;
      /** Area up left corner X */
      int            x;
      /** Area up left corner */
      int            y;

      /**
       * Create a new instance of Area
       * 
       * @param id
       *           Area ID
       * @param x
       *           Area up left corner X
       * @param y
       *           Area up left corner Y
       * @param width
       *           Area width
       * @param height
       *           Area height
       */
      Area(final int id, final int x, final int y, final int width, final int height)
      {
         this.id = id;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
      }

      /**
       * Area bounds
       * 
       * @return Area bounds
       */
      public Rectangle getBounds()
      {
         return new Rectangle(this.x, this.y, this.width, this.height);
      }

      /**
       * Developer information
       * 
       * @return Developer information
       */
      public Object getDevelopperInformation()
      {
         return this.developperInformation;
      }

      /**
       * Area ID
       * 
       * @return Area ID
       */
      public int getID()
      {
         return this.id;
      }

      /**
       * Indicates if given position inside the area
       * 
       * @param x
       *           Position X
       * @param y
       *           Position Y
       * @return {@code true} if given position inside the area
       */
      public boolean isInside(final int x, final int y)
      {
         return (x >= this.x) && (y >= this.y) && (x < (this.x + this.width)) && (y < (this.y + this.height));
      }

      /**
       * Move area to given point with relative position
       * 
       * @param x
       *           Point X
       * @param y
       *           Point Y
       * @param position
       *           Relative position
       */
      public void move(final int x, final int y, final Position position)
      {
         int x1 = this.x;
         int y1 = this.y;
         int x2 = (this.x + this.width) - 1;
         int y2 = (this.y + this.height) - 1;
         int vx = x - ((x1 + x2) >> 1);
         int vy = y - ((y1 + y2) >> 1);

         if((vx + x1) < 0)
         {
            vx = -x1;
         }

         if((vx + x2) >= SensitiveArea.this.width)
         {
            vx = SensitiveArea.this.width - x2 - 1;
         }

         if((vy + y1) < 0)
         {
            vy = -y1;
         }

         if((vy + y2) >= SensitiveArea.this.height)
         {
            vy = SensitiveArea.this.height - y2 - 1;
         }

         switch(position)
         {
            case DOWN:
               y2 = y;
            break;
            case INSIDE:
               x1 += vx;
               x2 += vx;
               y1 += vy;
               y2 += vy;
            break;
            case LEFT:
               x1 = x;
            break;
            case LEFT_DOWN:
               x1 = x;
               y2 = y;
            break;
            case LEFT_UP:
               x1 = x;
               y1 = y;
            break;
            case OUTSIDE:
            break;
            case RIGHT:
               x2 = x;
            break;
            case RIGHT_DOWN:
               x2 = x;
               y2 = y;
            break;
            case RIGHT_UP:
               x2 = x;
               y1 = y;
            break;
            case UP:
               y1 = y;
            break;
         }

         x1 = UtilMath.limit(x1, 0, SensitiveArea.this.width - 1);
         y1 = UtilMath.limit(y1, 0, SensitiveArea.this.height - 1);
         x2 = UtilMath.limit(x2, 0, SensitiveArea.this.width - 1);
         y2 = UtilMath.limit(y2, 0, SensitiveArea.this.height - 1);

         this.x = Math.min(x1, x2);
         this.y = Math.min(y1, y2);
         this.width = Math.abs(x1 - x2) + 1;
         this.height = Math.abs(y1 - y2) + 1;
      }

      /**
       * Compute relative position of a point
       * 
       * @param x
       *           Point X
       * @param y
       *           Point Y
       * @param near
       *           Distance to consider close
       * @return Relative position
       */
      public Position obtainPosition(final int x, final int y, int near)
      {
         final int xx = (this.x + this.width) - 1;
         final int yy = (this.y + this.height) - 1;
         near = Math.max(near, 0);

         if(Math.abs(x - this.x) <= near)
         {
            if(Math.abs(y - this.y) <= near)
            {
               return Position.LEFT_UP;
            }

            if(Math.abs(y - yy) <= near)
            {
               return Position.LEFT_DOWN;
            }

            if((y >= this.y) && (y < (this.y + this.height)))
            {
               return Position.LEFT;
            }
         }

         if(Math.abs(x - xx) <= near)
         {
            if(Math.abs(y - this.y) <= near)
            {
               return Position.RIGHT_UP;
            }

            if(Math.abs(y - yy) <= near)
            {
               return Position.RIGHT_DOWN;
            }

            if((y >= this.y) && (y < (this.y + this.height)))
            {
               return Position.RIGHT;
            }
         }

         if((x >= this.x) && (x < (this.x + this.width)))
         {
            if(Math.abs(y - this.y) <= near)
            {
               return Position.UP;
            }

            if(Math.abs(y - yy) <= near)
            {
               return Position.DOWN;
            }
         }

         if((x >= this.x) && (x < (this.x + this.width)) && (y >= this.y) && (y < (this.y + this.height)))
         {
            return Position.INSIDE;
         }

         return Position.OUTSIDE;
      }

      /**
       * Modify developer information
       * 
       * @param developperInformation
       *           New developer information
       */
      public void setDevelopperInformation(final Object developperInformation)
      {
         this.developperInformation = developperInformation;
      }
   }

   /**
    * Relative position
    * 
    * @author JHelp
    */
   public static enum Position
   {
      /** Area down border */
      DOWN,
      /** Inside the area */
      INSIDE,
      /** Area left border */
      LEFT,
      /** Area left-down corner */
      LEFT_DOWN,
      /** Area left border */
      LEFT_UP,
      /** Outside the area */
      OUTSIDE,
      /** Area right border */
      RIGHT,
      /** Area right-down corner */
      RIGHT_DOWN,
      /** Area right-up corner */
      RIGHT_UP,
      /** Area up border */
      UP
   }

   /** Height parameter */
   private static final String PARAMETER_HEIGHT = "height";
   /** ID parameter */
   private static final String PARAMETER_ID     = "id";
   /** Width parameter */
   private static final String PARAMETER_WIDTH  = "width";
   /** X parameter */
   private static final String PARAMETER_X      = "x";
   /** Y parameter */
   private static final String PARAMETER_Y      = "y";
   /*** Area markup */
   public static final String  MARKUP_AREA      = "jhelp_engine_event_SensitiveArea_Area";
   /** Main markup */
   public static final String  MARKUP_MAIN      = "jhelp_engine_event_SensitiveArea";

   /**
    * Parse XML markup parameters to build an area
    * 
    * @param markup
    *           Markup name
    * @param parameters
    *           Markup parameters
    * @return Parsed area
    * @throws ExceptionXML
    *            If markup is not an area markup OR one parameter invalid or missing
    */
   public static SensitiveArea createFromMarkup(final String markup, final Hashtable<String, String> parameters) throws ExceptionXML
   {
      if(SensitiveArea.MARKUP_MAIN.equals(markup) == false)
      {
         throw new ExceptionXML("The markup must be " + SensitiveArea.MARKUP_MAIN + " not " + markup);
      }

      final int width = ParserXML.obtainInteger(markup, parameters, SensitiveArea.PARAMETER_WIDTH, true, 0);
      final int height = ParserXML.obtainInteger(markup, parameters, SensitiveArea.PARAMETER_HEIGHT, true, 0);

      return new SensitiveArea(width, height);
   }

   /**
    * Load area from stream
    * 
    * @param inputStream
    *           Stream to read
    * @return Read area
    * @throws ExceptionParseXML
    *            If stream not contains valis area
    */
   public static SensitiveArea load(final InputStream inputStream) throws ExceptionParseXML
   {
      final ParserXML parserXML = new ParserXML();
      final LoadListener loadListener = new LoadListener();
      parserXML.parse(loadListener, inputStream);
      return loadListener.sensitiveArea;
   }

   /** Areas list */
   private final List<Area>                  areas;
   /** Developer information */
   private Object                            developperInformation;
   /** Event manager */
   private final EventManager                eventManager;
   /** Last created area ID */
   private int                               lastArea;
   /** Scene renderer parent */
   private JHelpSceneRenderer                sceneRenderer;
   /** Listeners of areas */
   private final List<SensitiveAreaListener> sensitiveAreaListeners;
   /** Total height */
   final int                                 height;
   /** Total width */
   final int                                 width;

   /***
    * Create a new instance of SensitiveArea
    * 
    * @param width
    *           Total width
    * @param height
    *           Total height
    */
   public SensitiveArea(final int width, final int height)
   {
      this.width = width;
      this.height = height;
      this.sensitiveAreaListeners = new ArrayList<SensitiveAreaListener>();
      this.areas = new ArrayList<Area>();
      this.lastArea = -1;
      this.eventManager = new EventManager();
   }

   /**
    * Called when pick finished
    * 
    * @param node
    *           Picked node
    */
   void finshPick(final Node node)
   {
      node.pickUVlistener = null;
      this.sceneRenderer.disablePickUV();
   }

   /**
    * Called when node picked
    * 
    * @param node
    *           Picked node
    */
   void pickOn(final Node node)
   {
      node.pickUVlistener = this.eventManager;
      this.sceneRenderer.setPickUVnode(node);
   }

   /**
    * Signal to listeners a click on an area
    * 
    * @param area
    *           Area ID
    */
   protected void fireSensitiveClick(final int area)
   {
      synchronized(this.sensitiveAreaListeners)
      {
         for(final SensitiveAreaListener sensitiveAreaListener : this.sensitiveAreaListeners)
         {
            sensitiveAreaListener.senstiveClick(area);
         }
      }
   }

   /**
    * Signal to listeners an enter on an area
    * 
    * @param area
    *           Area ID
    */
   protected void fireSensitiveEnter(final int area)
   {
      synchronized(this.sensitiveAreaListeners)
      {
         for(final SensitiveAreaListener sensitiveAreaListener : this.sensitiveAreaListeners)
         {
            sensitiveAreaListener.senstiveEnter(area);
         }
      }
   }

   /**
    * Signal to listeners an exit from an area
    * 
    * @param area
    *           Area ID
    */
   protected void fireSensitiveExit(final int area)
   {
      synchronized(this.sensitiveAreaListeners)
      {
         for(final SensitiveAreaListener sensitiveAreaListener : this.sensitiveAreaListeners)
         {
            sensitiveAreaListener.senstiveExit(area);
         }
      }
   }

   /**
    * Play an action for a given position
    * 
    * @param x
    *           Position X
    * @param y
    *           Position Y
    * @param doAction
    *           Indicates if click should be fire
    */
   public void actionAt(final int x, final int y, final boolean doAction)
   {
      int actualArea = -1;
      Area over = null;

      synchronized(this.areas)
      {
         for(final Area area : this.areas)
         {
            if(area.isInside(x, y) == true)
            {
               over = area;
               break;
            }
         }
      }

      if(over != null)
      {
         actualArea = over.id;
      }

      if(this.lastArea != actualArea)
      {
         if(this.lastArea >= 0)
         {
            this.fireSensitiveExit(this.lastArea);
         }

         if(actualArea >= 0)
         {
            this.fireSensitiveEnter(actualArea);
         }
      }

      this.lastArea = actualArea;

      if((doAction == true) && (actualArea >= 0))
      {
         this.fireSensitiveClick(actualArea);
      }
   }

   /**
    * Obtain an area
    * 
    * @param index
    *           Area index
    * @return Area
    */
   public Area getArea(final int index)
   {
      return this.areas.get(index);
   }

   /**
    * Developer information
    * 
    * @return Developer information
    */
   public Object getDevelopperInformation()
   {
      return this.developperInformation;
   }

   /**
    * Total height
    * 
    * @return Total height
    */
   public int getHeight()
   {
      return this.height;
   }

   /**
    * Obtain a node listener to register
    * 
    * @param sceneRenderer
    *           Scene renderer parent
    * @return Node listener to register
    */
   public NodeListener getNodeListener(final JHelpSceneRenderer sceneRenderer)
   {
      if(sceneRenderer == null)
      {
         throw new NullPointerException("sceneRenderer musn't be null");
      }

      this.sceneRenderer = sceneRenderer;
      return this.eventManager;
   }

   /**
    * Obtain object 2D listener
    * 
    * @return Object 2D listener
    */
   public Object2DListener getObject2DListener()
   {
      return this.eventManager;
   }

   /**
    * Total width
    * 
    * @return Total width
    */
   public int getWidth()
   {
      return this.width;
   }

   /**
    * Get the next free ID
    * 
    * @return Next free ID
    */
   public int nextFreeID()
   {
      int id = 0;

      for(final Area area : this.areas)
      {
         id = Math.max(id, area.id + 1);
      }

      return id;
   }

   /**
    * Number of area
    * 
    * @return Number of area
    */
   public int numberOfArea()
   {
      return this.areas.size();
   }

   /**
    * Obtain an area from a position
    * 
    * @param x
    *           Position X
    * @param y
    *           Position Y
    * @param near
    *           Distance to consider close
    * @return Couple of relative position and the area
    */
   public Pair<Position, Area> obtainArea(final int x, final int y, final int near)
   {
      Position position;

      for(final Area area : this.areas)
      {
         position = area.obtainPosition(x, y, near);

         if(position != Position.OUTSIDE)
         {
            return new Pair<Position, Area>(position, area);
         }
      }

      return new Pair<Position, Area>(Position.OUTSIDE, null);
   }

   /**
    * Obtain area by ID
    * 
    * @param areaID
    *           Area ID
    * @return Searched area OR {@code null} if not exists
    */
   public Area obtainAreaByID(final int areaID)
   {
      synchronized(this.areas)
      {
         for(final Area area : this.areas)
         {
            if(area.id == areaID)
            {
               return area;
            }
         }
      }

      return null;
   }

   /**
    * Create/update an area
    * 
    * @param areaID
    *           Area ID
    * @param x
    *           Area up-left corner X
    * @param y
    *           Area up-left corner T
    * @param width
    *           Area width
    * @param height
    *           Area height
    */
   public void putArea(final int areaID, final int x, final int y, final int width, final int height)
   {
      if(areaID < 0)
      {
         throw new IllegalArgumentException("area ID musn't be negative");
      }

      if((width <= 0) || (height <= 0))
      {
         return;
      }

      synchronized(this.areas)
      {
         final Area area = this.obtainAreaByID(areaID);

         if(area == null)
         {
            this.areas.add(new Area(areaID, x, y, width, height));
         }
         else
         {
            area.x = x;
            area.y = y;
            area.width = width;
            area.height = height;
         }

      }
   }

   /**
    * Create/update an area from markup parameters
    * 
    * @param markup
    *           Markup name
    * @param parameters
    *           Markup parameters
    * @throws ExceptionXML
    *            If markup not an area
    */
   public void putArea(final String markup, final Hashtable<String, String> parameters) throws ExceptionXML
   {
      if(SensitiveArea.MARKUP_AREA.equals(markup) == false)
      {
         throw new ExceptionXML("The markup must be " + SensitiveArea.MARKUP_AREA + " not " + markup);
      }

      final int id = ParserXML.obtainInteger(markup, parameters, SensitiveArea.PARAMETER_ID, true, 0);
      final int x = ParserXML.obtainInteger(markup, parameters, SensitiveArea.PARAMETER_X, true, 0);
      final int y = ParserXML.obtainInteger(markup, parameters, SensitiveArea.PARAMETER_Y, true, 0);
      final int width = ParserXML.obtainInteger(markup, parameters, SensitiveArea.PARAMETER_WIDTH, true, 0);
      final int height = ParserXML.obtainInteger(markup, parameters, SensitiveArea.PARAMETER_HEIGHT, true, 0);
      this.putArea(id, x, y, width, height);
   }

   /**
    * Register a sensitive area listener
    * 
    * @param sensitiveAreaListener
    *           Listener to register
    */
   public void registerSensitiveAreaListener(final SensitiveAreaListener sensitiveAreaListener)
   {
      if(sensitiveAreaListener == null)
      {
         throw new NullPointerException("sensitiveAreaListener musn't be null");
      }

      synchronized(this.sensitiveAreaListeners)
      {
         if(this.sensitiveAreaListeners.contains(sensitiveAreaListener) == false)
         {
            this.sensitiveAreaListeners.add(sensitiveAreaListener);
         }
      }
   }

   /**
    * Remove an area
    * 
    * @param area
    *           Area to remove
    */
   public void removeArea(final Area area)
   {
      synchronized(this.areas)
      {
         this.areas.remove(area);
      }
   }

   /**
    * Remove an area
    * 
    * @param area
    *           Area ID to remove
    */
   public void removeArea(final int area)
   {
      synchronized(this.areas)
      {
         this.areas.remove(this.obtainAreaByID(area));
      }
   }

   /**
    * Save the sensitive area in XML stream
    * 
    * @param dynamicWriteXML
    *           XML stream where write
    * @throws IOException
    *            On writing issue
    */
   public void save(final DynamicWriteXML dynamicWriteXML) throws IOException
   {
      dynamicWriteXML.openMarkup(SensitiveArea.MARKUP_MAIN);
      dynamicWriteXML.appendParameter(SensitiveArea.PARAMETER_WIDTH, this.width);
      dynamicWriteXML.appendParameter(SensitiveArea.PARAMETER_HEIGHT, this.height);

      synchronized(this.areas)
      {
         for(final Area area : this.areas)
         {
            dynamicWriteXML.openMarkup(SensitiveArea.MARKUP_AREA);
            dynamicWriteXML.appendParameter(SensitiveArea.PARAMETER_ID, area.id);
            dynamicWriteXML.appendParameter(SensitiveArea.PARAMETER_X, area.x);
            dynamicWriteXML.appendParameter(SensitiveArea.PARAMETER_Y, area.y);
            dynamicWriteXML.appendParameter(SensitiveArea.PARAMETER_WIDTH, area.width);
            dynamicWriteXML.appendParameter(SensitiveArea.PARAMETER_HEIGHT, area.height);
            dynamicWriteXML.closeMarkup();
         }
      }

      dynamicWriteXML.closeMarkup();
   }

   /**
    * Save sensitive area in stream.<br>
    * Stream not close, caller have to do it
    * 
    * @param outputStream
    *           Stream where write
    * @throws IOException
    *            On writing issue
    */
   public void save(final OutputStream outputStream) throws IOException
   {
      final DynamicWriteXML dynamicWriteXML = new DynamicWriteXML(outputStream);
      this.save(dynamicWriteXML);
   }

   /**
    * Define developer information
    * 
    * @param developperInformation
    *           Developer information
    */
   public void setDevelopperInformation(final Object developperInformation)
   {
      this.developperInformation = developperInformation;
   }

   /**
    * Unregister a sensitive area listener
    * 
    * @param sensitiveAreaListener
    *           Listener to unregister
    */
   public void unregisterSensitiveAreaListener(final SensitiveAreaListener sensitiveAreaListener)
   {
      synchronized(this.sensitiveAreaListeners)
      {
         this.sensitiveAreaListeners.remove(sensitiveAreaListener);
      }
   }
}