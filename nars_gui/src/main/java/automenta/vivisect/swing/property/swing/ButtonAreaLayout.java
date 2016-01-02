/**
 * L2FProd Common v9.2 License.
 *
 * Copyright 2005 - 2009 L2FProd.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package automenta.vivisect.swing.property.swing;

import java.awt.*;

/**
 * ButtonAreaLayout. <br>
 *  
 */
public final class ButtonAreaLayout implements LayoutManager {

  private final int gap;

  public ButtonAreaLayout(int gap) {
    this.gap = gap;
  }

  @Override
  public void addLayoutComponent(String string, Component comp) {
  }

  @Override
  public void layoutContainer(Container container) {
    Insets insets = container.getInsets();
    Component[] children = container.getComponents();

    // calculate the max width
    int maxWidth = 0;
    int maxHeight = 0;
    int visibleCount = 0;
    Dimension componentPreferredSize;

    for (Component aChildren1 : children) {
      if (aChildren1.isVisible()) {
        componentPreferredSize = aChildren1.getPreferredSize();
        maxWidth = Math.max(maxWidth, componentPreferredSize.width);
        maxHeight = Math.max(maxHeight, componentPreferredSize.height);
        visibleCount++;
      }
    }

    int usedWidth = maxWidth * visibleCount + gap * (visibleCount - 1);
    
    visibleCount = 0;
    for (Component aChildren : children) {
      if (aChildren.isVisible()) {
        aChildren.setBounds(
                container.getWidth()
                        - insets.right
                        - usedWidth
                        + (maxWidth + gap) * visibleCount,
                insets.top,
                maxWidth,
                maxHeight);
        visibleCount++;
      }
    }
  }

  @Override
  public Dimension minimumLayoutSize(Container c) {
    return preferredLayoutSize(c);
  }

  @Override
  public Dimension preferredLayoutSize(Container container) {
    Insets insets = container.getInsets();
    Component[] children = container.getComponents();

    // calculate the max width
    int maxWidth = 0;
    int maxHeight = 0;
    int visibleCount = 0;
    Dimension componentPreferredSize;

    for (Component aChildren : children) {
      if (aChildren.isVisible()) {
        componentPreferredSize = aChildren.getPreferredSize();
        maxWidth = Math.max(maxWidth, componentPreferredSize.width);
        maxHeight = Math.max(maxHeight, componentPreferredSize.height);
        visibleCount++;
      }
    }

    int usedWidth = maxWidth * visibleCount + gap * (visibleCount - 1);

    return new Dimension(
      insets.left + usedWidth + insets.right,
      insets.top + maxHeight + insets.bottom);
  }

  @Override
  public void removeLayoutComponent(Component c) {
  }
}
