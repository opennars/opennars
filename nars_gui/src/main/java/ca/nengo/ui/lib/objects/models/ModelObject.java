package ca.nengo.ui.lib.objects.models;

import ca.nengo.ui.actions.RemoveModelAction;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.util.menus.PopupMenuBuilder;
import ca.nengo.ui.lib.world.Interactable;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.activities.Pulsator;
import ca.nengo.ui.lib.world.elastic.ElasticObject;
import ca.nengo.ui.models.tooltips.Tooltip;
import ca.nengo.ui.models.tooltips.TooltipBuilder;

import javax.swing.*;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * A UI Object which wraps a model
 * 
 * @author Shu Wu
 */
/**
 * @author User
 */
public abstract class ModelObject<M> extends ElasticObject implements Interactable {

	/**
	 * The property name that identifies a change in this node's Model
	 */
	public static final String PROPERTY_MODEL = "uiModel";

	/**
	 * Icon for this model
	 */
	private WorldObject icon;

	/**
	 * Property Listener which listens to changes of the Icon's bounds and
	 * updates this node bounds accordingly
	 */
	private Listener iconPropertyChangeListener;

	private boolean isModelBusy = false;

	private final HashSet<ModelListener> modelListeners = new HashSet<ModelListener>();

	/**
	 * Model
	 */
	private M myModel;

	private Pulsator pulsator = null;

	/**
	 * Create a UI Wrapper around a Model
	 * 
	 * @param model
	 *            Model
	 */
	public ModelObject(M model) {
		super();
		//Util.Assert(model != null);

		setModel(model);
	}

	/**
	 * @param model
	 *            New Model
	 */
	protected final void setModel(M model) {
        if (myModel == null && model!=null) {
            initialize();
        }

		if (myModel == model) {
			return;
		}

		if (myModel != null) {
			detachViewFromModel();
		}

		myModel = model;
		firePropertyChange(Property.MODEL_CHANGED);

		if (myModel != null) {
			attachViewToModel();
			modelUpdated();
		}
	}

	/**
	 * Attaches the UI from the model
	 */
	protected void attachViewToModel() {

	}

	/**
	 * @return Constructed Context Menu
	 */
	protected void constructMenu(PopupMenuBuilder menu) {
		if (showRemoveModelAction()) {
			ArrayList<ModelObject> arrayOfMe = new ArrayList<ModelObject>();
			arrayOfMe.add(this);
			menu.addAction(new RemoveModelAction("Remove model", arrayOfMe));
		}
	}

	protected boolean showRemoveModelAction() {
		return true;
	}

	protected void constructTooltips(TooltipBuilder builder) {
		// do nothing
	}

	/**
	 * Detaches the UI form the model
	 */
	protected void detachViewFromModel() {
		setModelBusy(false);
	}

	protected void initialize() {
		setSelectable(true);
	}

	/**
	 * Updates the UI from the model
	 */
	protected void modelUpdated() {

	}

	@Override
	protected void prepareForDestroy() {
		super.prepareForDestroy();

		detachViewFromModel();
		firePropertyChange(Property.MODEL_CHANGED);
	}

	protected void prepareToDestroyModel() {

	}

	/**
	 * @param newIcon
	 *            New Icon
	 */
	protected void setIcon(WorldObject newIcon) {
		if (icon != null) {
			icon.removePropertyChangeListener(Property.BOUNDS_CHANGED, iconPropertyChangeListener);
			icon.removeFromParent();
		}

		icon = newIcon;

		addChild(icon, 0);

		iconPropertyChangeListener = new Listener() {
			public void propertyChanged(Property event) {
				setBounds(icon.getBounds());
			}

		};
		setBounds(icon.getBounds());

		icon.addPropertyChangeListener(Property.BOUNDS_CHANGED, iconPropertyChangeListener);

	}

	public void addModelListener(ModelListener listener) {
		if (modelListeners.contains(listener)) {
			throw new InvalidParameterException();
		}
		modelListeners.add(listener);
	}

	/*
	 * destroy() + destroy the model
	 */
	public final void destroyModel() {
		for (ModelListener listener : modelListeners) {
			listener.modelDestroyStarted(getModel());
		}

		prepareToDestroyModel();

		for (WorldObject wo : getChildren()) {
			if (wo instanceof ModelObject) {
				((ModelObject) wo).destroyModel();
			}
		}

		for (ModelListener listener : modelListeners.toArray(new ModelListener[modelListeners.size()])) {
			listener.modelDestroyed(getModel());
		}

		destroy();
	}

	/**
	 * Called if this object is double clicked on
	 */
	@Override
	public void doubleClicked() {
		super.doubleClicked();
		if (getWorld() != null) {
			getWorld().zoomToObject(this);
		}
	}

	/*
	 * (non-Javadoc) This method is final. To add items to the menu, override
	 * constructMenu() instead.
	 * 
	 * @see ca.shu.ui.lib.handlers.Interactable#showContextMenu(edu.umd.cs.piccolo.event.PInputEvent)
	 */
	public final JPopupMenu getContextMenu() {
		if (isModelBusy()) {
			return null;
		} else {
			PopupMenuBuilder menu = new PopupMenuBuilder(getFullName());
			constructMenu(menu);

			return menu.toJPopupMenu();
		}
	}

	public String getFullName() {
		return getName() + " (" + getTypeName() + ')';
	}

	/**
	 * @return Icon of this node
	 */
	public WorldObject getIcon() {
		return icon;
	}

	/**
	 * @return Model
	 */
	public M getModel() {
		return myModel;
	}

	@Override
	public final WorldObject getTooltip() {
		String toolTipTitle = getFullName();

		TooltipBuilder tooltipBuilder = new TooltipBuilder(toolTipTitle);
		if (isModelBusy()) {

			tooltipBuilder.addTitle("Currently busy");

		} else {

			constructTooltips(tooltipBuilder);
		}

		return new Tooltip(tooltipBuilder);
	}

	/**
	 * @return What this type of Model is called
	 */
	public abstract String getTypeName();

	public boolean isModelBusy() {
		return isModelBusy;
	}

	public void removeModelListener(ModelListener listener) {
		if (!modelListeners.contains(listener)) {
			throw new InvalidParameterException();
		}
		modelListeners.remove(listener);
	}

	/**
	 * @param isBusy
	 *            Whether the model is currently busy. If it is busy, the object
	 *            will not be interactable.
	 */
	public void setModelBusy(boolean isBusy) {
		if (isModelBusy != isBusy) {
			isModelBusy = isBusy;

			if (isModelBusy) {
				Util.Assert(pulsator == null,
						"Previous pulsator has not been disposed of properly);");
				pulsator = new Pulsator(this);
			} else {
				if (pulsator != null) {
					pulsator.finish();
					pulsator = null;
				}
			}

		}
	}

	static public interface ModelListener {
		public void modelDestroyed(Object model);

		public void modelDestroyStarted(Object model);
	}

}
