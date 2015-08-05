// Copyright (C) 2010, 2011, 2012, 2013 GlavSoft LLC.
// All rights reserved.
//
//-------------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//-------------------------------------------------------------------------
//

package automenta.vnc.viewer.swing;

import automenta.vnc.VNCClient;
import automenta.vnc.core.SettingsChangedEvent;
import automenta.vnc.rfb.IChangeSettingsListener;
import automenta.vnc.rfb.client.KeyEventMessage;
import automenta.vnc.rfb.protocol.Protocol;
import automenta.vnc.rfb.protocol.ProtocolContext;
import automenta.vnc.rfb.protocol.ProtocolSettings;
import automenta.vnc.utils.Keymap;
import automenta.vnc.viewer.ConnectionPresenter;
import automenta.vnc.viewer.UiSettings;
import automenta.vnc.viewer.swing.gui.OptionsDialog;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SwingViewerWindow extends JPanel implements IChangeSettingsListener {
	public static final int FS_SCROLLING_ACTIVE_BORDER = 20;
	private JToggleButton zoomFitButton;
	private JToggleButton zoomFullScreenButton;
	private JButton zoomInButton;
	private JButton zoomOutButton;
	private JButton zoomAsIsButton;

    /** main panel to which everything is attached */
    JPanel panel;

	private JScrollPane scroller;
	private JFrame frame;
	private boolean forceResizable = true;
	private ButtonsBar buttonsBar;
	final Surface surface;
	private final boolean isSeparateFrame;
    private final boolean isApplet;
    private final VNCClient viewer;
    private final String connectionString;
    private final ConnectionPresenter presenter;
    private Rectangle oldContainerBounds;
	private volatile boolean isFullScreen;
	private Border oldScrollerBorder;
	private JLayeredPane lpane;
	private EmptyButtonsBarMouseAdapter buttonsBarMouseAdapter;
    private String remoteDesktopName;
    private final ProtocolSettings rfbSettings;
    private final UiSettings uiSettings;
    private final Protocol workingProtocol;

    private boolean isZoomToFitSelected;
    private List<JComponent> kbdButtons;

    public SwingViewerWindow(Protocol workingProtocol, ProtocolSettings rfbSettings, UiSettings uiSettings, Surface surface,
                             boolean isSeparateFrame, boolean isApplet, VNCClient viewer, String connectionString,
                             ConnectionPresenter presenter) {
        this.workingProtocol = workingProtocol;
        this.rfbSettings = rfbSettings;
        this.uiSettings = uiSettings;
        this.surface = surface;
        this.isSeparateFrame = isSeparateFrame;
        this.isApplet = isApplet;
        this.viewer = viewer;
        this.connectionString = connectionString;
        this.presenter = presenter;
        createContainer(surface, isApplet, viewer);


        if (uiSettings.showControls) {
            createButtonsPanel(workingProtocol, isSeparateFrame? frame: viewer);
            if (isSeparateFrame) registerResizeListener(frame);
            updateZoomButtonsState();
        }
        if (uiSettings.isFullScreen()) {
            switchOnFullscreenMode();
        }
        setSurfaceToHandleKbdFocus();
        if (isSeparateFrame) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // nop
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            frame.toFront();
                        }
                    });
                }
            }).start();
        }
	}



	private void createContainer(final Surface surface, boolean isApplet, JComponent appletWindow) {
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override
            public Dimension getSize() {
                return surface.getPreferredSize();
            }
            @Override
            public Dimension getPreferredSize() {
                return surface.getPreferredSize();
            }
        };

        panel.setBackground(Color.DARK_GRAY);
		lpane = new JLayeredPane() {
			@Override
			public Dimension getSize() {
				return surface.getPreferredSize();
			}
			@Override
			public Dimension getPreferredSize() {
				return surface.getPreferredSize();
			}
		};
		lpane.setPreferredSize(surface.getPreferredSize());
		lpane.add(surface, JLayeredPane.DEFAULT_LAYER, 0);
		panel.add(lpane);

		scroller = new JScrollPane(panel);
		if (isSeparateFrame) {
			frame = new JFrame();
			if ( ! isApplet) {
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			}
            frame.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
			Utils.setApplicationIconsForWindow(frame);
            frame.setLayout(new BorderLayout(0, 0));
            frame.add(scroller, BorderLayout.CENTER);

//			frame.pack();
            panel.setSize(surface.getPreferredSize());
            internalPack(null);
            frame.setVisible(true);
            frame.validate();
		} else {
            appletWindow.setLayout(new BorderLayout(0, 0));
            appletWindow.add(scroller, BorderLayout.CENTER);
            appletWindow.validate();
		}
	}

	public void pack() {
		final Dimension outerPanelOldSize = panel.getSize();
		panel.setSize(surface.getPreferredSize());
		if (isSeparateFrame && ! isZoomToFitSelected()) {
			internalPack(outerPanelOldSize);
		}
        if (buttonsBar != null) {
            updateZoomButtonsState();
        }
        updateWindowTitle();
	}

    public boolean isZoomToFitSelected() {
        return isZoomToFitSelected;
    }

    public void setZoomToFitSelected(boolean zoomToFitSelected) {
        isZoomToFitSelected = zoomToFitSelected;
    }

    public void setRemoteDesktopName(String name) {
        remoteDesktopName = name;
        updateWindowTitle();
    }

    private void updateWindowTitle() {
        if (isSeparateFrame) {
			frame.setTitle(remoteDesktopName + " [zoom: " + uiSettings.getScalePercentFormatted() + "%]");
		}
    }

	private void internalPack(Dimension outerPanelOldSize) {
		final Rectangle workareaRectangle = getWorkareaRectangle();
		if (workareaRectangle.equals(frame.getBounds())) {
			forceResizable = true;
		}
		final boolean isHScrollBar = scroller.getHorizontalScrollBar().isShowing() && ! forceResizable;
		final boolean isVScrollBar = scroller.getVerticalScrollBar().isShowing() && ! forceResizable;

		boolean isWidthChangeable = true;
		boolean isHeightChangeable = true;
		if (outerPanelOldSize != null && surface.oldSize != null) {
			isWidthChangeable = forceResizable ||
					(outerPanelOldSize.width == surface.oldSize.width && ! isHScrollBar);
			isHeightChangeable = forceResizable ||
					(outerPanelOldSize.height == surface.oldSize.height && ! isVScrollBar);
		}
		forceResizable = false;
		frame.validate();

		final Insets containerInsets = frame.getInsets();
		Dimension preferredSize = frame.getPreferredSize();
		Rectangle preferredRectangle = new Rectangle(frame.getLocation(), preferredSize);

		if (null == outerPanelOldSize && workareaRectangle.contains(preferredRectangle)) {
			frame.pack();
		} else {
			Dimension minDimension = new Dimension(
					containerInsets.left + containerInsets.right, containerInsets.top + containerInsets.bottom);
			if (buttonsBar != null && buttonsBar.isVisible) {
				minDimension.width += buttonsBar.getWidth();
				minDimension.height += buttonsBar.getHeight();
			}
			Dimension dim = new Dimension(preferredSize);
			Point location = frame.getLocation();
			if ( ! isWidthChangeable) {
				dim.width = frame.getWidth();
			} else {
				if (isVScrollBar) dim.width += scroller.getVerticalScrollBar().getWidth();
				if (dim.width < minDimension.width) dim.width = minDimension.width;

				int dx = location.x - workareaRectangle.x;
				if (dx < 0) {
					dx = 0;
					location.x = workareaRectangle.x;
				}
				int w = workareaRectangle.width - dx;
				if (w < dim.width) {
					int dw = dim.width - w;
					if (dw < dx) {
						location.x -= dw;
					} else {
						dim.width = workareaRectangle.width;
						location.x = workareaRectangle.x;
					}
				}
			}
			if ( ! isHeightChangeable) {
				dim.height = frame.getHeight();
			} else {

				if (isHScrollBar) dim.height += scroller.getHorizontalScrollBar().getHeight();
				if (dim.height < minDimension.height) dim.height = minDimension.height;

				int dy = location.y - workareaRectangle.y;
				if (dy < 0) {
					dy = 0;
					location.y = workareaRectangle.y;
				}
				int h = workareaRectangle.height - dy;
				if (h < dim.height) {
					int dh = dim.height - h;
					if (dh < dy) {
						location.y -= dh;
					} else {
						dim.height = workareaRectangle.height;
						location.y = workareaRectangle.y;
					}
				}
			}
			if ( ! location.equals(frame.getLocation())) {
				frame.setLocation(location);
			}
			if ( ! isFullScreen ) {
				frame.setSize(dim);
			}
		}
		scroller.revalidate();
	}

	private Rectangle getWorkareaRectangle() {
		final GraphicsConfiguration graphicsConfiguration = frame.getGraphicsConfiguration();
		final Rectangle screenBounds = graphicsConfiguration.getBounds();
		final Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);

		screenBounds.x += screenInsets.left;
		screenBounds.y += screenInsets.top;
		screenBounds.width -= screenInsets.left + screenInsets.right;
		screenBounds.height -= screenInsets.top + screenInsets.bottom;
		return screenBounds;
	}

	void addZoomButtons() {
		buttonsBar.createStrut();
		zoomOutButton = buttonsBar.createButton("zoom-out", "Zoom Out", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				zoomFitButton.setSelected(false);
                uiSettings.zoomOut();
			}
		});
		zoomInButton = buttonsBar.createButton("zoom-in", "Zoom In", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				zoomFitButton.setSelected(false);
                uiSettings.zoomIn();
			}
		});
		zoomAsIsButton = buttonsBar.createButton("zoom-100", "Zoom 100%", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				zoomFitButton.setSelected(false);
				forceResizable = false;
                uiSettings.zoomAsIs();
			}
		});

		zoomFitButton = buttonsBar.createToggleButton("zoom-fit", "Zoom to Fit Window",
				new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							setZoomToFitSelected(true);
							forceResizable = true;
							zoomToFit();
							updateZoomButtonsState();
						} else {
							setZoomToFitSelected(false);
						}
						setSurfaceToHandleKbdFocus();
					}
				});

		zoomFullScreenButton = buttonsBar.createToggleButton("zoom-fullscreen", "Full Screen",
			new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					updateZoomButtonsState();
					if (e.getStateChange() == ItemEvent.SELECTED) {
                        uiSettings.setFullScreen(switchOnFullscreenMode());
					} else {
						switchOffFullscreenMode();
                        uiSettings.setFullScreen(false);
					}
					setSurfaceToHandleKbdFocus();
				}
			});
			if ( ! isSeparateFrame) {
				zoomFullScreenButton.setEnabled(false);
				zoomFitButton.setEnabled(false);
			}
		}

    protected void setSurfaceToHandleKbdFocus() {
        if (surface != null && ! surface.requestFocusInWindow()) {
            surface.requestFocus();
        }
    }

    boolean switchOnFullscreenMode() {
		zoomFullScreenButton.setSelected(true);
		oldContainerBounds = frame.getBounds();
		setButtonsBarVisible(false);
		forceResizable = true;
		frame.dispose();
		frame.setUndecorated(true);
		frame.setResizable(false);
		frame.setVisible(true);
		try {
			frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(frame);
			isFullScreen = true;
			scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			oldScrollerBorder = scroller.getBorder();
			scroller.setBorder(new EmptyBorder(0, 0, 0, 0));
			new FullscreenBorderDetectionThread(frame).start();
		} catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).info("Cannot switch into FullScreen mode: " + ex.getMessage());
			return false;
		}
        return true;
	}

	private void switchOffFullscreenMode() {
		if (isFullScreen) {
			zoomFullScreenButton.setSelected(false);
			isFullScreen = false;
			setButtonsBarVisible(true);
			try {
				frame.dispose();
				frame.setUndecorated(false);
				frame.setResizable(true);
				frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(null);
			} catch (Exception e) {
				// nop
			}
			scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroller.setBorder(oldScrollerBorder);
			this.frame.setBounds(oldContainerBounds);
			frame.setVisible(true);
			pack();
		}
	}

	private void zoomToFit() {
		Dimension scrollerSize = scroller.getSize();
		Insets scrollerInsets = scroller.getInsets();
        uiSettings.zoomToFit(scrollerSize.width - scrollerInsets.left - scrollerInsets.right,
                scrollerSize.height - scrollerInsets.top - scrollerInsets.bottom +
                        (isFullScreen ? buttonsBar.getHeight() : 0),
                workingProtocol.getFbWidth(), workingProtocol.getFbHeight());
	}

	void registerResizeListener(Container container) {
		container.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (isZoomToFitSelected()) {
					zoomToFit();
					updateZoomButtonsState();
					updateWindowTitle();
					setSurfaceToHandleKbdFocus();
				}
			}
		});
	}

	void updateZoomButtonsState() {
		zoomOutButton.setEnabled(uiSettings.getScalePercent() > UiSettings.MIN_SCALE_PERCENT);
		zoomInButton.setEnabled(uiSettings.getScalePercent() < UiSettings.MAX_SCALE_PERCENT);
		zoomAsIsButton.setEnabled(uiSettings.getScalePercent() != 100);
	}

	public ButtonsBar createButtonsBar() {
		buttonsBar = new ButtonsBar();
		return buttonsBar;
	}

    public void setButtonsBarVisible(boolean isVisible) {
        setButtonsBarVisible(isVisible, frame);
    }

	private void setButtonsBarVisible(boolean isVisible, Container container) {
		buttonsBar.setVisible(isVisible);
		if (isVisible) {
			buttonsBar.borderOff();
			container.add(buttonsBar.bar, BorderLayout.NORTH);
            container.validate();
		} else {
			container.remove(buttonsBar.bar);
			buttonsBar.borderOn();
		}
	}

	public void setButtonsBarVisibleFS(boolean isVisible) {
		if (isVisible) {
			if ( ! buttonsBar.isVisible) {
				lpane.add(buttonsBar.bar, JLayeredPane.POPUP_LAYER, 0);
				final int bbWidth = buttonsBar.bar.getPreferredSize().width;
				buttonsBar.bar.setBounds(
						scroller.getViewport().getViewPosition().x + (scroller.getWidth() - bbWidth)/2, 0,
						bbWidth, buttonsBar.bar.getPreferredSize().height);

				// prevent mouse events to through down to Surface
				if (null == buttonsBarMouseAdapter) buttonsBarMouseAdapter = new EmptyButtonsBarMouseAdapter();
				buttonsBar.bar.addMouseListener(buttonsBarMouseAdapter);
			}
		} else {
			buttonsBar.bar.removeMouseListener(buttonsBarMouseAdapter);
			lpane.remove(buttonsBar.bar);
			lpane.repaint(buttonsBar.bar.getBounds());
		}
		buttonsBar.setVisible(isVisible);
	}

    public Surface getSurface() {
        return surface;
    }

    void close() {
        if (isSeparateFrame && frame != null) {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static class ButtonsBar {
		private static final Insets BUTTONS_MARGIN = new Insets(2, 2, 2, 2);
		private final JPanel bar;
		private boolean isVisible;

		public ButtonsBar() {
			bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));
		}

		public JButton createButton(String iconId, String tooltipText, ActionListener actionListener) {
			JButton button = new JButton(Utils.getButtonIcon(iconId));
            button.setText(iconId);
			button.setToolTipText(tooltipText);
			button.setMargin(BUTTONS_MARGIN);
			bar.add(button);
			button.addActionListener(actionListener);
			return button;
		}

		public void createStrut() {
			bar.add(Box.createHorizontalStrut(10));
		}

		public JToggleButton createToggleButton(String iconId, String tooltipText, ItemListener itemListener) {
			JToggleButton button = new JToggleButton(Utils.getButtonIcon(iconId));
			button.setToolTipText(tooltipText);
			button.setMargin(BUTTONS_MARGIN);
			bar.add(button);
			button.addItemListener(itemListener);
			return button;
		}

		public void setVisible(boolean isVisible) {
			this.isVisible = isVisible;
            if (isVisible) bar.revalidate();
		}

		public int getWidth() {
			return bar.getMinimumSize().width;
		}
		public int getHeight() {
			return bar.getMinimumSize().height;
		}

		public void borderOn() {
			bar.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		}

		public void borderOff() {
			bar.setBorder(BorderFactory.createEmptyBorder());
		}
	}

	private static class EmptyButtonsBarMouseAdapter extends MouseAdapter {
		// empty
	}

	private class FullscreenBorderDetectionThread extends Thread {
		public static final int SHOW_HIDE_BUTTONS_BAR_DELAY_IN_MILLS = 700;
        public static final int MILLIS = 100;
        private final JFrame frame;
		private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		private ScheduledFuture<?> futureForShow;
		private ScheduledFuture<?> futureForHide;
		private Point mousePoint, oldMousePoint;
		private Point viewPosition;

		public FullscreenBorderDetectionThread(JFrame frame) {
			super("FS border detector");
			this.frame = frame;
		}

		@Override
        public void run() {
			setPriority(Thread.MIN_PRIORITY);
			while(isFullScreen) {
				mousePoint = MouseInfo.getPointerInfo().getLocation();
				if (null == oldMousePoint) oldMousePoint = mousePoint;
				SwingUtilities.convertPointFromScreen(mousePoint, frame);
				viewPosition = scroller.getViewport().getViewPosition();
				processButtonsBarVisibility();

				boolean needScrolling = processVScroll() || processHScroll();
				oldMousePoint = mousePoint;
				if (needScrolling) {
					cancelShowExecutor();
					setButtonsBarVisibleFS(false);
					makeScrolling(viewPosition);
				}
				try {
                    Thread.sleep(MILLIS);
                } catch (Exception e) {
					// nop
				}
			}
		}

		private boolean processHScroll() {
			if (mousePoint.x < FS_SCROLLING_ACTIVE_BORDER) {
				if (viewPosition.x > 0) {
					int delta = FS_SCROLLING_ACTIVE_BORDER - mousePoint.x;
					if (mousePoint.y != oldMousePoint.y) delta *= 2; // speedify scrolling on mouse moving
					viewPosition.x -= delta;
					if (viewPosition.x < 0) viewPosition.x = 0;
					return true;
				}
			} else if (mousePoint.x > (frame.getWidth() - FS_SCROLLING_ACTIVE_BORDER)) {
				final Rectangle viewRect = scroller.getViewport().getViewRect();
				final int right = viewRect.width + viewRect.x;
				if (right < panel.getSize().width) {
					int delta = FS_SCROLLING_ACTIVE_BORDER - (frame.getWidth() - mousePoint.x);
					if (mousePoint.y != oldMousePoint.y) delta *= 2; // speedify scrolling on mouse moving
					viewPosition.x += delta;
					if (viewPosition.x + viewRect.width > panel.getSize().width) viewPosition.x =
							panel.getSize().width - viewRect.width;
					return true;
				}
			}
			return false;
		}

		private boolean processVScroll() {
			if (mousePoint.y < FS_SCROLLING_ACTIVE_BORDER) {
				if (viewPosition.y > 0) {
					int delta = FS_SCROLLING_ACTIVE_BORDER - mousePoint.y;
					if (mousePoint.x != oldMousePoint.x) delta *= 2; // speedify scrolling on mouse moving
					viewPosition.y -= delta;
					if (viewPosition.y < 0) viewPosition.y = 0;
					return true;
				}
			} else if (mousePoint.y > (frame.getHeight() - FS_SCROLLING_ACTIVE_BORDER)) {
				final Rectangle viewRect = scroller.getViewport().getViewRect();
				final int bottom = viewRect.height + viewRect.y;
				if (bottom < panel.getSize().height) {
					int delta = FS_SCROLLING_ACTIVE_BORDER - (frame.getHeight() - mousePoint.y);
					if (mousePoint.x != oldMousePoint.x) delta *= 2; // speedify scrolling on mouse moving
					viewPosition.y += delta;
					if (viewPosition.y + viewRect.height > panel.getSize().height) viewPosition.y =
							panel.getSize().height - viewRect.height;
					return true;
				}
			}
			return false;
		}

		private void processButtonsBarVisibility() {
			if (mousePoint.y < 1) {
				cancelHideExecutor();
				// show buttons bar after delay
				if (! buttonsBar.isVisible && (null == futureForShow || futureForShow.isDone())) {
					futureForShow = scheduler.schedule(new Runnable() {
						@Override
						public void run() {
							showButtonsBar();
						}
					}, SHOW_HIDE_BUTTONS_BAR_DELAY_IN_MILLS, TimeUnit.MILLISECONDS);
				}
			} else {
				cancelShowExecutor();
			}
			if (buttonsBar.isVisible && mousePoint.y <= buttonsBar.getHeight()) {
				cancelHideExecutor();
			}
			if (buttonsBar.isVisible && mousePoint.y > buttonsBar.getHeight()) {
				// hide buttons bar after delay
				if (null == futureForHide || futureForHide.isDone()) {
					futureForHide = scheduler.schedule(new Runnable() {
						@Override
						public void run() {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									setButtonsBarVisibleFS(false);
									SwingViewerWindow.this.frame.validate();
								}
							});
						}
					}, SHOW_HIDE_BUTTONS_BAR_DELAY_IN_MILLS, TimeUnit.MILLISECONDS);
				}
			}
		}

		private void cancelHideExecutor() {
			cancelExecutor(futureForHide);
		}
		private void cancelShowExecutor() {
			cancelExecutor(futureForShow);
		}

		private void cancelExecutor(ScheduledFuture<?> future) {
			if (future != null && ! future.isDone()) {
				future.cancel(true);
			}
		}

		private void makeScrolling(final Point viewPosition) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					scroller.getViewport().setViewPosition(viewPosition);
					final Point mousePosition = surface.getMousePosition();
					if (mousePosition != null) {
						final MouseEvent mouseEvent = new MouseEvent(frame, 0, 0, 0,
								mousePosition.x, mousePosition.y, 0, false);
						for (MouseMotionListener mml : surface.getMouseMotionListeners()) {
							mml.mouseMoved(mouseEvent);
						}
					}
				}
			});
		}

		private void showButtonsBar() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setButtonsBarVisibleFS(true);
				}
			});
		}
	}

    protected void createButtonsPanel(final ProtocolContext context, Container container) {
        final ButtonsBar buttonsBar = createButtonsBar();

        buttonsBar.createButton("options", "Set Options", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOptionsDialog();
                setSurfaceToHandleKbdFocus();
            }
        });

        buttonsBar.createButton("info", "Show connection info", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showConnectionInfoMessage();
                setSurfaceToHandleKbdFocus();
            }
        });

        buttonsBar.createStrut();

        buttonsBar.createButton("refresh", "Refresh screen", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                context.sendRefreshMessage();
                setSurfaceToHandleKbdFocus();
            }
        });

        addZoomButtons();

        kbdButtons = new LinkedList<>();

        buttonsBar.createStrut();

        JButton ctrlAltDelButton = buttonsBar.createButton("ctrl-alt-del", "Send 'Ctrl-Alt-Del'", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendCtrlAltDel(context);
                setSurfaceToHandleKbdFocus();
            }
        });
        kbdButtons.add(ctrlAltDelButton);

        JButton winButton = buttonsBar.createButton("win", "Send 'Win' key as 'Ctrl-Esc'", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendWinKey(context);
                setSurfaceToHandleKbdFocus();
            }
        });
        kbdButtons.add(winButton);

        JToggleButton ctrlButton = buttonsBar.createToggleButton("ctrl", "Ctrl Lock",
                new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            context.sendMessage(new KeyEventMessage(Keymap.K_CTRL_LEFT, true));
                        } else {
                            context.sendMessage(new KeyEventMessage(Keymap.K_CTRL_LEFT, false));
                        }
                        setSurfaceToHandleKbdFocus();
                    }
                });
        kbdButtons.add(ctrlButton);

        JToggleButton altButton = buttonsBar.createToggleButton("alt", "Alt Lock",
                new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            context.sendMessage(new KeyEventMessage(Keymap.K_ALT_LEFT, true));
                        } else {
                            context.sendMessage(new KeyEventMessage(Keymap.K_ALT_LEFT, false));
                        }
                        setSurfaceToHandleKbdFocus();
                    }
                });
        kbdButtons.add(altButton);

        ModifierButtonEventListener modifierButtonListener = new ModifierButtonEventListener();
        modifierButtonListener.addButton(KeyEvent.VK_CONTROL, ctrlButton);
        modifierButtonListener.addButton(KeyEvent.VK_ALT, altButton);
        surface.addModifierListener(modifierButtonListener);

//		JButton fileTransferButton = new JButton(Utils.getButtonIcon("file-transfer"));
//		fileTransferButton.setMargin(buttonsMargin);
//		buttonBar.add(fileTransferButton);

        buttonsBar.createStrut();

        buttonsBar.createButton("close", isApplet ? "Disconnect" : "Close", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (frame != null) {
                    frame.setVisible(false);
                    frame.dispose();
                }
                presenter.setNeedReconnection(false);
                presenter.cancelConnection();
                viewer.closeApp();
            }
        }).setAlignmentX(JComponent.RIGHT_ALIGNMENT);

        setButtonsBarVisible(true, container);
    }

    private static void sendCtrlAltDel(ProtocolContext context) {
        context.sendMessage(new KeyEventMessage(Keymap.K_CTRL_LEFT, true));
        context.sendMessage(new KeyEventMessage(Keymap.K_ALT_LEFT, true));
        context.sendMessage(new KeyEventMessage(Keymap.K_DELETE, true));
        context.sendMessage(new KeyEventMessage(Keymap.K_DELETE, false));
        context.sendMessage(new KeyEventMessage(Keymap.K_ALT_LEFT, false));
        context.sendMessage(new KeyEventMessage(Keymap.K_CTRL_LEFT, false));
    }

    private static void sendWinKey(ProtocolContext context) {
        context.sendMessage(new KeyEventMessage(Keymap.K_CTRL_LEFT, true));
        context.sendMessage(new KeyEventMessage(Keymap.K_ESCAPE, true));
        context.sendMessage(new KeyEventMessage(Keymap.K_ESCAPE, false));
        context.sendMessage(new KeyEventMessage(Keymap.K_CTRL_LEFT, false));
    }

    @Override
    public void settingsChanged(SettingsChangedEvent e) {
        if (ProtocolSettings.isRfbSettingsChangedFired(e)) {
            ProtocolSettings settings = (ProtocolSettings) e.getSource();
            setEnabledKbdButtons( ! settings.isViewOnly());
        }
    }

    private void setEnabledKbdButtons(boolean enabled) {
        if (kbdButtons != null) {
            for (JComponent b : kbdButtons) {
                b.setEnabled(enabled);
            }
        }
    }

    private void showOptionsDialog() {
        OptionsDialog optionsDialog = new OptionsDialog(frame);
        optionsDialog.initControlsFromSettings(rfbSettings, uiSettings, false);
        optionsDialog.setVisible(true);
        presenter.saveHistory();
    }

    private void showConnectionInfoMessage() {
        StringBuilder message = new StringBuilder();
        message.append("TightVNC Viewer v.").append(VNCClient.ver()).append("\n\n");
        message.append("Connected to: ").append(remoteDesktopName).append('\n');
        message.append("Host: ").append(connectionString).append("\n\n");

        message.append("Desktop geometry: ")
                .append(String.valueOf(surface.getWidth()))
                .append(" \u00D7 ") // multiplication sign
                .append(String.valueOf(surface.getHeight())).append('\n');
        message.append("Color format: ")
                .append(String.valueOf(Math.round(Math.pow(2, workingProtocol.getPixelFormat().depth))))
                .append(" colors (")
                .append(String.valueOf(workingProtocol.getPixelFormat().depth))
                .append(" bits)\n");
        message.append("Current protocol version: ")
                .append(workingProtocol.getProtocolVersion());
        if (workingProtocol.isTight()) {
            message.append("tight");
        }
        message.append('\n');

        JOptionPane infoPane = new JOptionPane(message.toString(), JOptionPane.INFORMATION_MESSAGE);
        final JDialog infoDialog = infoPane.createDialog(frame, "VNC connection info");
        infoDialog.setModalityType(Dialog.ModalityType.MODELESS);
        infoDialog.setVisible(true);
    }
}