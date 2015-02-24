///*
// *
// *  TODO: give range on 'scale to extrema' some headroom.
// *
// *   think about rounding to time step
// * */
//
//package ca.nengo.plot;
//
////import java.awt.BorderLayout;
////import java.awt.GridBagLayout;
////import java.awt.GridBagConstraints;
////import java.awt.Image;
//
//import ca.nengo.util.Probe;
//import com.jeta.forms.components.panel.FormPanel;
//import com.jeta.forms.gui.common.FormException;
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.chart.plot.XYPlot;
//import org.jfree.chart.renderer.xy.XYItemRenderer;
//import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;
//
//import javax.swing.*;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.geom.Ellipse2D;
//import java.lang.reflect.InvocationHandler;
//import java.lang.reflect.Method;
//import java.lang.reflect.Proxy;
//import java.util.TimerTask;
//
////import java.io.IOException;
////import javax.swing.JFrame;
////import javax.imageio.ImageIO;
//
//public class Scope {
//	private Probe _probe;
//	private float[][] _probeValues;
//
//	private java.util.Timer _workerTimer = new java.util.Timer();
//	private JSlider _slider;
//
//	/** non-null when playing (or fast-forwarding, or rewinding). */
//	private volatile SimpleRecurController _curRecurController;
//	private int _curTime = 0;
//	private int _timeStep = 100, _trailLength = 20;
//	private static final int Y_AXIS_SCALING_SEEN=1, Y_AXIS_SCALING_VISIBLE=2, Y_AXIS_SCALING_CUSTOM=3;
//	private int _yAxisScaling = Y_AXIS_SCALING_SEEN;
//	/** [min, max].  only used when _yAxisScaling == Y_AXIS_SCALING_SEEN. */
//	private float[] _yAxisSeenMinMax;
//	/** [min, max].  only used when _yAxisScaling == Y_AXIS_SCALING_CUSTOM. */
//	private double[] _yAxisCustomMinMax = new double[]{-1, 1};
//	private Color[] _dimensionColors;
//
//	private JPanel _graphPanel;
//	private FormPanel _ctrlPanel;
//
//	public Scope(Probe probe_) {
//		assert probe_ != null;
//		_probe = probe_;
//		_probeValues = _probe.getData().getValues();
//		resetMinMaxSeen();
//		initDimensionColorsToSomeDefaults();
//		try {
//			SwingUtilities.invokeAndWait(new Runnable() {
//				public void run() {
//					_graphPanel = new JPanel();
//					initCtrlPanel();
//					showChartPanelForCurTime(true);
//				}
//			});
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//
//		// hack for pack(): (TODO: fix)
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//		}
//	}
//
//	private void initDimensionColorsToSomeDefaults() {
//		assert _probeValues!=null;
//		_dimensionColors = new Color[numDimensions()];
//		for(int i=0; i<_dimensionColors.length; ++i) {
//			final Color[] basicColors = { Color.RED, Color.GREEN, Color.BLUE, Color.CYAN,
//					Color.MAGENTA, Color.ORANGE, Color.YELLOW, Color.PINK,
//					Color.BLACK, Color.GRAY };
//			_dimensionColors[i] = basicColors[i % basicColors.length];
//		}
//	}
//
//	private void resetMinMaxSeen() {
//		_yAxisSeenMinMax = new float[] { Float.MAX_VALUE, Float.MIN_VALUE };
//	}
//
//	private void initCtrlPanel() {
//		try {
//			_ctrlPanel = new FormPanel(getClass().getResourceAsStream(
//					"ScopeControls.jfrm"));
//		} catch (FormException e) {
//			throw new RuntimeException(e);
//		}
//
//		JButton playButton = (JButton) (_ctrlPanel
//				.getComponentByName("play.button"));
//		playButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				pause();
//				_curRecurController = new SimpleRecurController();
//				move(0, 1, false, _curRecurController, true);
//			}
//		});
//
//		JButton pauseButton = (JButton) (_ctrlPanel
//				.getComponentByName("pause.button"));
//		pauseButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				pause();
//			}
//		});
//
//		JButton stepBackButton = (JButton) (_ctrlPanel
//				.getComponentByName("step.back.button"));
//		stepBackButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				move(0, -1, false, null, true);
//			}
//		});
//		JButton stepForwardButton = (JButton) (_ctrlPanel
//				.getComponentByName("step.forward.button"));
//		stepForwardButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				move(0, 1, false, null, true);
//			}
//		});
//
//		JButton goToStartButton = (JButton) (_ctrlPanel
//				.getComponentByName("go.to.start.button"));
//		goToStartButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				move(0, 0, true, null, true);
//			}
//		});
//		JButton goToEndButton = (JButton) (_ctrlPanel
//				.getComponentByName("go.to.end.button"));
//		goToEndButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				move(0, _probeValues.length - 1, true, null, true);
//			}
//		});
//
//		JButton fastForwardButton = (JButton) (_ctrlPanel
//				.getComponentByName("fast.forward.button"));
//		fastForwardButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				pause();
//				_curRecurController = new SimpleRecurController();
//				move(0, 3, false, _curRecurController, true);
//			}
//		});
//		JButton rewindButton = (JButton) (_ctrlPanel
//				.getComponentByName("rewind.button"));
//		rewindButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				pause();
//				_curRecurController = new SimpleRecurController();
//				move(0, -3, false, _curRecurController, true);
//			}
//		});
//
//
//		_slider = (JSlider) (_ctrlPanel.getComponentByName("slider"));
//		_slider.setMinimum(0);
//		_slider.setMaximum(_probeValues.length - 1);
//		_slider.setValue(0);
//		_slider.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				// rounding down to time step:
//				int newTime = (_slider.getValue() / _timeStep) * _timeStep;
//				move(0, newTime, true, null, false);
//			}
//		});
//
//		final JRadioButton scaleToMinMaxSeenButton = (JRadioButton) (_ctrlPanel
//				.getComponentByName("range.seen.so.far.button"));
//		scaleToMinMaxSeenButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				_yAxisScaling = Y_AXIS_SCALING_SEEN;
//				resetMinMaxSeen();
//				if(!isPlaying()) {
//					showChartPanelForCurTime(true);
//				}
//			}
//		});
//		final JRadioButton scaleToVisibleDataPointsButton = (JRadioButton) (_ctrlPanel
//				.getComponentByName("range.visible.button"));
//		scaleToVisibleDataPointsButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				_yAxisScaling = Y_AXIS_SCALING_VISIBLE;
//				if(!isPlaying()) {
//					showChartPanelForCurTime(true);
//				}
//			}
//		});
//		final JRadioButton scaleCustomButton = (JRadioButton) (_ctrlPanel
//				.getComponentByName("range.custom.button"));
//		scaleCustomButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				_yAxisScaling = Y_AXIS_SCALING_CUSTOM;
//				if(!isPlaying()) {
//					showChartPanelForCurTime(true);
//				}
//			}
//		});
//		ButtonGroup bg = new ButtonGroup();
//		bg.add(scaleToMinMaxSeenButton);
//		bg.add(scaleToVisibleDataPointsButton);
//		bg.add(scaleCustomButton);
//		if(_yAxisScaling == Y_AXIS_SCALING_SEEN) {
//			scaleToMinMaxSeenButton.setSelected(true);
//		} else if(_yAxisScaling == Y_AXIS_SCALING_VISIBLE) {
//			scaleToVisibleDataPointsButton.setSelected(true);
//		} else if(_yAxisScaling == Y_AXIS_SCALING_CUSTOM) {
//			scaleCustomButton.setSelected(true);
//		}
//
//		final JSpinner[] rangeCustomSpinners = {
//			(JSpinner)(_ctrlPanel.getComponentByName("range.custom.min.spinner")),
//			(JSpinner)(_ctrlPanel.getComponentByName("range.custom.max.spinner"))};
//		rangeCustomSpinners[0].setModel(
//			new SpinnerNumberModel(_yAxisCustomMinMax[0], null, null, 0.1));
//		rangeCustomSpinners[0].addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				_yAxisCustomMinMax[0] = (Double) (rangeCustomSpinners[0].getValue());
//				if (!isPlaying()) {
//					showChartPanelForCurTime(true);
//				}
//			}
//		});
//		rangeCustomSpinners[1].setModel(
//			new SpinnerNumberModel(_yAxisCustomMinMax[1], null, null, 0.1));
//		rangeCustomSpinners[1].addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				_yAxisCustomMinMax[1] = (Double) (rangeCustomSpinners[1].getValue());
//				if (!isPlaying()) {
//					showChartPanelForCurTime(true);
//				}
//			}
//		});
//
//		final JSpinner timeStepSpinner
//			= (JSpinner)(_ctrlPanel.getComponentByName("time.step.spinner"));
//		timeStepSpinner.setModel(new SpinnerNumberModel(_timeStep, 1, Integer.MAX_VALUE, 1));
//		timeStepSpinner.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				_timeStep = (Integer) (timeStepSpinner.getValue());
//				if (!isPlaying()) {
//					showChartPanelForCurTime(true);
//				}
//			}
//		});
//
//		final JSpinner trailLengthSpinner
//			= (JSpinner)(_ctrlPanel.getComponentByName("trail.length.spinner"));
//		trailLengthSpinner.setModel(new SpinnerNumberModel(_trailLength, 0, Integer.MAX_VALUE, 1));
//		trailLengthSpinner.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				_trailLength = (Integer) (trailLengthSpinner.getValue());
//				if (!isPlaying()) {
//					showChartPanelForCurTime(true);
//				}
//			}
//		});
//
//		final JButton changeColorsButton
//			= (JButton)(_ctrlPanel.getComponentByName("change.colors.button"));
//		changeColorsButton.addMouseListener(new MouseAdapter() {
//			public void mouseClicked(MouseEvent e) {
//				makeColorChangeDimensionChooserPopupMenu().show(changeColorsButton, e.getX(), e.getY());
//			}
//		});
//	}
//
//	private JPopupMenu makeColorChangeDimensionChooserPopupMenu() {
//		JPopupMenu popupMenu = new JPopupMenu();
//		for(int i=0; i<numDimensions(); ++i) {
//			JMenuItem menuItem = new JMenuItem("dimension "+i,
//					new SolidColorIcon(_dimensionColors[i], 10, 10));
//			menuItem.addActionListener(new ColorChangeDimensionChooserPopupMenuItemListener(i));
//			popupMenu.add(menuItem);
//		}
//		return popupMenu;
//	}
//
//	private class ColorChangeDimensionChooserPopupMenuItemListener implements ActionListener {
//		private int _dimensionNum;
//
//		ColorChangeDimensionChooserPopupMenuItemListener(int dimensionNum_) {
//			_dimensionNum = dimensionNum_;
//		}
//
//		public void actionPerformed(ActionEvent e) {
//			Color newColor = JColorChooser.showDialog(_ctrlPanel,
//					"Choose Color for Dimension", _dimensionColors[_dimensionNum]);
//			if(newColor != null) {
//				_dimensionColors[_dimensionNum] = newColor;
//				if (!isPlaying()) {
//					showChartPanelForCurTime(true);
//				}
//			}
//		}
//	}
//
//	/**
//	 * this is not probably not accurate. but then it is not used for anything
//	 * that requires it to be accurate.
//	 */
//	private boolean isPlaying() {
//		return _curRecurController != null;
//	}
//
//	private void pause() {
//		if (_curRecurController != null) {
//			_curRecurController._recur = false;
//			_curRecurController = null;
//		}
//	}
//
//	private static interface RecurController {
//		public boolean shouldRecur();
//	}
//
//	private static class SimpleRecurController implements RecurController {
//		public volatile boolean _recur = true;
//
//		public boolean shouldRecur() {
//			return _recur;
//		}
//	}
//
//	/**
//	 * @param x_
//	 *            if posIsAbsoluteAsOpposedToRelative_ is true then this is an
//	 *            absolute location (index in _probeValues) else this is
//	 *            relative, units = multiples of _timeStep, and represents a
//	 *            'delta' in a _probeValues index. (also, can be negative).
//	 */
//	private void move(final int delayTimeMillis_, final int x_,
//			final boolean posIsAbsoluteAsOpposedToRelative_,
//			final RecurController recurController_,
//			final boolean adjustSliderToMatch_) {
//		TimerTask t = new TimerTask() {
//			public void run() {
//				int wouldBeCurTime = (posIsAbsoluteAsOpposedToRelative_ ? x_
//						: _curTime + x_ * _timeStep);
//				wouldBeCurTime = reinIn(wouldBeCurTime, 0,
//						_probeValues.length - 1);
//				if (wouldBeCurTime != _curTime) {
//					if (recurController_ != null
//							&& recurController_.shouldRecur()) {
//						move(100, x_, posIsAbsoluteAsOpposedToRelative_,
//								recurController_, adjustSliderToMatch_);
//					}
//					_curTime = wouldBeCurTime;
//					showChartPanelForCurTime(false);
//					if (adjustSliderToMatch_) {
//						SwingUtilities.invokeLater(new Runnable() {
//							public void run() {
//								setValueNoFire(_slider, _curTime);
//							}
//						});
//					}
//				} else {
//					_curRecurController = null;
//				}
//			}
//		};
//		_workerTimer.schedule(t, delayTimeMillis_);
//	}
//
//	private static void setValueNoFire(JSlider slider_, int value_) {
//		ChangeListener[] listeners = slider_.getChangeListeners();
//		for (int i = 0; i < listeners.length; ++i) {
//			slider_.removeChangeListener(listeners[i]);
//		}
//		slider_.setValue(value_);
//		for (int i = 0; i < listeners.length; ++i) {
//			slider_.addChangeListener(listeners[i]);
//		}
//	}
//
//	private void showChartPanelForCurTime(boolean invokeLater_) {
//		showChartPanel(getChartPanelForCurTime(), invokeLater_);
//	}
//
//	private void showChartPanel(final JPanel panel_, boolean invokeLater_) {
//		Runnable r = new Runnable() {
//			public void run() {
//				_graphPanel.removeAll();
//				_graphPanel.add(panel_);
//				_graphPanel.validate();
//			}
//		};
//		if (invokeLater_) {
//			SwingUtilities.invokeLater(r);
//		} else {
//			try {
//				SwingUtilities.invokeAndWait(r);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//	}
//
//	private int numDimensions() {
//		return _probeValues[0].length;
//	}
//
//	private ChartPanel getChartPanelForCurTime() {
//		XYSeriesCollection dataset = new XYSeriesCollection();
//		for (int dim = 0; dim < numDimensions(); ++dim) {
//			XYSeries series = new XYSeries("dimension " + dim);
//			for (int trailPosTime = _curTime; trailPosTime >= _curTime
//					- _trailLength * _timeStep; trailPosTime -= _timeStep) {
//				if (trailPosTime >= 0) {
//					series.add(trailPosTime / 1000.0,
//							_probeValues[trailPosTime][dim]);
//					if (_yAxisScaling == Y_AXIS_SCALING_SEEN) {
//						updateMinMaxSeen(_probeValues[trailPosTime][dim]);
//					}
//				} else {
//					series.add(trailPosTime / 1000.0, null);
//				}
//			}
//			dataset.addSeries(series);
//		}
//		JFreeChart chart = ChartFactory.createScatterPlot("Function", "Input",
//				"Output", dataset, PlotOrientation.VERTICAL, false, false,
//				false);
//		if (_yAxisScaling == Y_AXIS_SCALING_SEEN) {
//			chart.getXYPlot().getRangeAxis().setRange(_yAxisSeenMinMax[0], _yAxisSeenMinMax[1]);
//		} else if(_yAxisScaling == Y_AXIS_SCALING_CUSTOM) {
//			chart.getXYPlot().getRangeAxis().setRange(_yAxisCustomMinMax[0], _yAxisCustomMinMax[1]);
//		}
//		animPlotVectorSetupRenderer(chart.getXYPlot(), _trailLength);
//		return new ChartPanel(chart);
//	}
//
//	private void updateMinMaxSeen(float v_) {
//		_yAxisSeenMinMax[0] = Math.min(_yAxisSeenMinMax[0], v_);
//		_yAxisSeenMinMax[1] = Math.max(_yAxisSeenMinMax[1], v_);
//	}
//
//	private void animPlotVectorSetupRenderer(XYPlot plot,
//			final int trailLength) {
//		final XYLineAndShapeRenderer origRenderer = (XYLineAndShapeRenderer) (plot
//				.getRenderer());
//
//		for (int i = 0; i <= trailLength; ++i) {
//			origRenderer.setSeriesShape(i, new Ellipse2D.Float(-3, -3, 6, 6));
//		}
//
//		InvocationHandler handler = new InvocationHandler() {
//			public Object invoke(Object proxy, Method method, Object[] args)
//					throws Throwable {
//				if (method.getName().equals("drawItem")) {
//					int series = (Integer) (args[8]), item = (Integer) (args[9]);
//					Color color = _dimensionColors[series];
//					if(trailLength>0) {
//						color = fadeToWhite(color, (trailLength - item)/(float)trailLength);
//					}
//					origRenderer.setSeriesPaint(series, color, false);
//				}
//				return method.invoke(origRenderer, args);
//			}
//		};
//		XYItemRenderer wrappingRenderer = (XYItemRenderer) Proxy
//				.newProxyInstance(ScopeExample.class.getClassLoader(),
//						new Class[] { XYItemRenderer.class }, handler);
//
//		plot.setRenderer(wrappingRenderer);
//	}
//
//	private static Color fadeToWhite(Color c, float percent) {
//		return new Color(fadeColorCompToWhite(c.getRed(), percent),
//				fadeColorCompToWhite(c.getGreen(), percent),
//				fadeColorCompToWhite(c.getBlue(), percent));
//	}
//
//	private static int fadeColorCompToWhite(int comp, float percent) {
//		return reinIn((int) (comp + (255 - comp) * percent), 0, 255);
//	}
//
//	private static int reinIn(int x_, int lowerBound_, int upperBound_) {
//		if (x_ < lowerBound_) {
//			return lowerBound_;
//		} else if (x_ > upperBound_) {
//			return upperBound_;
//		} else {
//			return x_;
//		}
//	}
//
//	public JPanel getGraphPanel() {
//		return _graphPanel;
//	}
//
//	public JPanel getCtrlPanel() {
//		return _ctrlPanel;
//	}
//
//}
