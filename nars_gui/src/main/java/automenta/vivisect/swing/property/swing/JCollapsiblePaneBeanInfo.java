/*
 * Generated file - Do not edit!
 */
package automenta.vivisect.swing.property.swing;

import java.awt.*;
import java.beans.*;
import java.util.Vector;

/**
 * BeanInfo class for JCollapsiblePane.
 */
public class JCollapsiblePaneBeanInfo extends SimpleBeanInfo {

  /** Description of the Field */
  protected BeanDescriptor bd = new BeanDescriptor(
    JCollapsiblePane.class);
  /** Description of the Field */
  protected Image iconMono16;
  /** Description of the Field */
  protected Image iconColor16;
  /** Description of the Field */
  protected Image iconMono32;
  /** Description of the Field */
  protected Image iconColor32;

  /** Constructor for the JCollapsiblePaneBeanInfo object */
  public JCollapsiblePaneBeanInfo() throws IntrospectionException {
    // setup bean descriptor in constructor.
    bd.setName("JCollapsiblePane");

    bd.setShortDescription("A pane which hides its content with an animation.");

    bd.setValue("isContainer", Boolean.TRUE);
    bd.setValue("containerDelegate", "getContentPane");

    BeanInfo info = Introspector.getBeanInfo(getBeanDescriptor().getBeanClass()
      .getSuperclass());
    String order = info.getBeanDescriptor().getValue("propertyorder") == null?""
      :(String)info.getBeanDescriptor().getValue("propertyorder");
    PropertyDescriptor[] pd = getPropertyDescriptors();
    for (int i = 0; i != pd.length; i++) {
      if (!order.contains(pd[i].getName())) {
        order = order + (order.isEmpty() ?"":":") + pd[i].getName();
      }
    }
    getBeanDescriptor().setValue("propertyorder", order);
  }

  /**
   * Gets the additionalBeanInfo
   * 
   * @return The additionalBeanInfo value
   */
  @Override
  public BeanInfo[] getAdditionalBeanInfo() {
    Vector bi = new Vector();
    BeanInfo[] biarr = null;
    try {
      for (Class cl = JCollapsiblePane.class
        .getSuperclass(); !cl.equals(Component.class.getSuperclass()); cl = cl
        .getSuperclass()) {
        bi.addElement(Introspector.getBeanInfo(cl));
      }
      biarr = new BeanInfo[bi.size()];
      bi.copyInto(biarr);
    } catch (Exception e) {
      // Ignore it
    }
    return biarr;
  }

  /**
   * Gets the beanDescriptor
   * 
   * @return The beanDescriptor value
   */
  @Override
  public BeanDescriptor getBeanDescriptor() {
    return bd;
  }

  /**
   * Gets the defaultPropertyIndex
   * 
   * @return The defaultPropertyIndex value
   */
  @Override
  public int getDefaultPropertyIndex() {
    String defName = "";
    if (defName != null && defName.isEmpty()) { return -1; }
    PropertyDescriptor[] pd = getPropertyDescriptors();
    for (int i = 0; i < pd.length; i++) {
      if (pd[i].getName().equals(defName)) { return i; }
    }
    return -1;
  }

  /**
   * Gets the icon
   * 
   * @param type
   *          Description of the Parameter
   * @return The icon value
   */
  @Override
  public Image getIcon(int type) {
    if (type == BeanInfo.ICON_COLOR_16x16) { return iconColor16; }
    if (type == BeanInfo.ICON_MONO_16x16) { return iconMono16; }
    if (type == BeanInfo.ICON_COLOR_32x32) { return iconColor32; }
    if (type == BeanInfo.ICON_MONO_32x32) { return iconMono32; }
    return null;
  }

  /**
   * Gets the Property Descriptors
   * 
   * @return The propertyDescriptors value
   */
  @Override
  public PropertyDescriptor[] getPropertyDescriptors() {
    try {
      Vector descriptors = new Vector();
      PropertyDescriptor descriptor = null;

      try {
        descriptor = new PropertyDescriptor("animated",
                JCollapsiblePane.class);
      } catch (IntrospectionException e) {
        descriptor = new PropertyDescriptor("animated",
                JCollapsiblePane.class, "getAnimated", null);
      }

      descriptor.setPreferred(true);

      descriptor.setBound(true);

      descriptors.add(descriptor);
      try {
        descriptor = new PropertyDescriptor("collapsed",
                JCollapsiblePane.class);
      } catch (IntrospectionException e) {
        descriptor = new PropertyDescriptor("collapsed",
                JCollapsiblePane.class, "getCollapsed", null);
      }

      descriptor.setPreferred(true);

      descriptor.setBound(true);

      descriptors.add(descriptor);

      return (PropertyDescriptor[])descriptors
        .toArray(new PropertyDescriptor[descriptors.size()]);
    } catch (Exception e) {
      // do not ignore, bomb politely so use has chance to discover what went
      // wrong...
      // I know that this is suboptimal solution, but swallowing silently is
      // even worse... Propose better solution!
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Gets the methodDescriptors attribute ...
   * 
   * @return The methodDescriptors value
   */
  @Override
  public MethodDescriptor[] getMethodDescriptors() {
    return new MethodDescriptor[0];
  }

}
