package ca.nengo.ui.lib;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Customized split pane implementation which holds main and an auxillary
 * Container which can be hidden /shown.
 * 
 * @author Shu Wu
 */
public class AuxillarySplitPane extends JSplitPane {

    private static final long serialVersionUID = 1L;

    public static final int MINIMUM_WIDTH = 300;
    public static final int MINIMUM_HEIGHT = 200;
    
    public static enum Orientation {
    	Top, Bottom, Left, Right
    }

    private static int getJSplitPaneOrientation(Orientation orientation) {
        if (orientation == Orientation.Left || orientation == Orientation.Right) {
            return JSplitPane.HORIZONTAL_SPLIT;
        } else {
            return JSplitPane.VERTICAL_SPLIT;
        }
    }

    private int auxPanelVisibleSize;
    private final String auxTitle;
    private JPanel auxPanelWr;
    private boolean resizable;

    private final Container mainPanel;

    private final Orientation orientation;
    private final Dimension minimumSize;
    private final boolean showTitle;
    
    public AuxillarySplitPane(Container mainPanel, Container auxPanel, String auxTitle,
            Orientation orientation) {
    	this(mainPanel, auxPanel, auxTitle, orientation,
    			new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT), true);
    }

    /**
     * @param mainPanel TODO
     * @param auxPanel TODO
     * @param auxTitle TODO
     * @param orientation TODO
     */
    public AuxillarySplitPane(Container mainPanel, Container auxPanel, String auxTitle,
            Orientation orientation, Dimension minsize, boolean showTitle) {
        super(getJSplitPaneOrientation(orientation));
        this.mainPanel = mainPanel;
        this.auxTitle = auxTitle;
        this.orientation = orientation;
        this.minimumSize = minsize;
        this.showTitle = showTitle;
        this.resizable = true;

        this.addComponentListener(new ComponentListener() {
            public void componentHidden(ComponentEvent e) {
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentResized(ComponentEvent e) {
                updateAuxSize();
            }

            public void componentShown(ComponentEvent e) {
            }

        });

        init(auxPanel);
    }
    
    private void init(Container auxillaryPanel) {
        NengoStyle.applyStyle(this);
        setOneTouchExpandable(true);
        setBorder(null);

        setAuxPane(auxillaryPanel, auxTitle);

        setAuxVisible(false);
    }
    
    /**
     * @param auxPane TODO
     * @param title TODO
     */
    public void setAuxPane(Container auxPane, String title) {
        this.auxPanelWr = createAuxPanelWrapper(auxPane, title);

        if (auxPane != null) {
            setAuxVisible(true, true);
        } else {
            setAuxVisible(false);
        }

        if (orientation == Orientation.Left) {
            setLeftComponent(auxPanelWr);
            setRightComponent(mainPanel);
        } else if (orientation == Orientation.Right) {
            setLeftComponent(mainPanel);
            setRightComponent(auxPanelWr);
        } else if (orientation == Orientation.Bottom) {
            setTopComponent(mainPanel);
            setBottomComponent(auxPanelWr);
        } else if (orientation == Orientation.Top) {
        	setTopComponent(auxPanelWr);
        	setBottomComponent(mainPanel);
        }
    }

    private JPanel createAuxPanelWrapper(final Container auxPanel, String title) {
        /*
         * Initialize auxillary panel
         */
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (auxPanel!=null) {
                    auxPanel.requestFocusInWindow();
                }
            }
        });

//        NengoStyle.applyStyle(leftPanel);

        if (showTitle) {
            /*
             * Create auxillary panel's title bar
             */
            JPanel titleBar = new JPanel();
            titleBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
            NengoStyle.applyStyle(titleBar);
            titleBar.setBackground(NengoStyle.COLOR_BACKGROUND2);
            titleBar.setOpaque(true);
            titleBar.setLayout(new BorderLayout());
        	
	        JLabel titleLabel = new JLabel(title);
	
	        titleLabel.setFont(NengoStyle.FONT_BIG);
	        NengoStyle.applyStyle(titleLabel);
	        titleLabel.setBackground(NengoStyle.COLOR_BACKGROUND2);
	        titleLabel.setOpaque(true);
	
	        String hideButtonTxt = " << ";
	        if (orientation == Orientation.Right) {
	            hideButtonTxt = " >> ";
	        }
	        JLabel hideButton = new JLabel(hideButtonTxt);
	        NengoStyle.applyStyle(hideButton);
	        hideButton.setBackground(NengoStyle.COLOR_BACKGROUND2);
	        hideButton.setOpaque(true);
	
	        /*
	         * Keep in this order, Swing puts items added first on top. We want the
	         * button to be on top
	         */
	        titleBar.add(hideButton, BorderLayout.EAST);
	        titleBar.add(titleLabel, BorderLayout.WEST);
	        
	        hideButton.addMouseListener(new HideButtonListener(hideButton));
	
	        leftPanel.add(titleBar, BorderLayout.NORTH);
        }

        leftPanel.setMinimumSize(minimumSize);

        if (auxPanel != null) {
//            NengoStyle.applyStyle(auxPanel);
            leftPanel.add(auxPanel, BorderLayout.CENTER);
        }

        return leftPanel;
    }
    
    /**
     * Is the auxillary panel resizable
     */
    public boolean isResizable() {
    	return resizable;
    }
    
    /**
     * Set whether the auxillary panel is resizable
     */
    public void setResizable(boolean newResizable) {
    	if (newResizable != isResizable()) {
    		resizable = newResizable;
    		setProperDividerSize(isAuxVisible(), newResizable);
    		if (!newResizable) {
    			setDividerLocation(0);
    		}
    	}
    }

    /**
     * Get the auxillary panel title
     */
    public String getAuxTitle() {
        return auxTitle;
    }

    /**
     * Get the panel holding the auxillary pane
     */
    public JPanel getAuxPaneWrapper() {
        return auxPanelWr;
    }

    /**
     * Is the auxillary panel visible
     */
    public boolean isAuxVisible() {
        return auxPanelWr.isVisible();
    }

    /**
     * Set whether the auxillary panel is visible
     */
    public void setAuxVisible(boolean isVisible) {
        setAuxVisible(isVisible, false);
    }

    /**
     * Set whether the auxillary panel is visible
     * @param isVisible Whether the panel should be made visible
     * @param resetDividerLocation Whether to reset the panel size to the default minimum
     */
    public void setAuxVisible(boolean isVisible, boolean resetDividerLocation) {
        if (isVisible) {
            int minAuxSize = getMinAuxSize();
            if (auxPanelVisibleSize < minAuxSize || resetDividerLocation) {
            	setAuxPanelSize(minAuxSize);
            } else {
            	setAuxPanelSize(auxPanelVisibleSize);
            }
            
            setProperDividerSize(isVisible, isResizable());
            
            if (!auxPanelWr.isVisible()) {
                auxPanelWr.requestFocus();
                auxPanelWr.setVisible(true);
            }
            auxPanelWr.requestFocusInWindow();
        } else {
            auxPanelWr.setVisible(false);
            setProperDividerSize(isVisible, isResizable());
        }
    }
    
    /**
     * Set the divider size, given whether the panel will be visible or resizable
     */
    private void setProperDividerSize(boolean newVisible, boolean newResizable) {
    	if (newVisible && newResizable)
    		setDividerSize(2);
    	else
    		setDividerSize(0);
    }
    
    private void updateAuxSize() {
    	setAuxPanelSize(auxPanelVisibleSize);
    }

    @Override
    public void setDividerLocation(int location) {
        super.setDividerLocation(location);
        int newAuxPanelSize = flipLocation(location);
	    if (newAuxPanelSize >= getMinAuxSize()) {
	    	auxPanelVisibleSize = newAuxPanelSize;
	    }
    }
    
    public void setAuxPanelSize(int size) {
    	setDividerLocation(flipLocation(size));
    }
    
    /**
     * Return the size in the split direction
     * @return
     */
    private int getSplitDim() {
        if (isVerticalOrientation())
        	return getHeight();
        else
        	return getWidth();
    }
    
    /**
     * Turn a location into an auxPanelSize, or vice versa
     */
    private int flipLocation(int location) {
	    if (isFlippedOrientation()) {
	    	return getSplitDim() - location;
		} else {
			return location;
		}
    }
    
    /**
     * Get the minimum size of the auxPanel, in the split direction
     */
    private int getMinAuxSize() {
        if (isVerticalOrientation()) {
            return (int)auxPanelWr.getMinimumSize().getHeight();
        } else {
            return (int)auxPanelWr.getMinimumSize().getWidth();
        }
    }

    /**
     * Whether this is a vertical-split pane (one item on top of another)
     */
    private boolean isVerticalOrientation() {
    	return (getJSplitPaneOrientation(orientation) == 
    			JSplitPane.VERTICAL_SPLIT);
    }

    /**
     * Whether the orientation requires flipping the location (right or bottom)
     */
    private boolean isFlippedOrientation() {
    	return (orientation == Orientation.Bottom || 
    			orientation == Orientation.Right);
    }

    class HideButtonListener implements MouseListener {
        private final Container hideButton;

        public HideButtonListener(Container hideButton) {
            super();
            this.hideButton = hideButton;
        }

        public void mouseClicked(MouseEvent e) {
            setAuxVisible(false);
        }

        public void mouseEntered(MouseEvent e) {
            hideButton.setBackground(NengoStyle.COLOR_FOREGROUND2);
        }

        public void mouseExited(MouseEvent e) {
            hideButton.setBackground(null);
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

    }

}