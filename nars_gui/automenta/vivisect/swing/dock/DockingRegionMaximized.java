/*
 * Copyright 2011 Mark McKay
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
package automenta.vivisect.swing.dock;

import java.awt.Component;
import javax.swing.JTabbedPane;
import automenta.vivisect.swing.dock.DockingRegionContainer.MinMaxRecord;

/**
 *
 * @author kitfox
 */
public class DockingRegionMaximized extends JTabbedPane {

    MinMaxRecord record;

    public DockingRegionMaximized(MinMaxRecord record) {
        this.record = record;
        DockingContent content = record.getContent();

        Component comp = content.getComponent();
        addTab(content.getTitle(), comp);
        int idx = indexOfComponent(comp);
        setTabComponentAt(idx, new TabbedPaneTitleMax(this, content));
    }

    public void minimize() {
        DockingContent content = record.getContent();
        record.getContainer().restoreFromMaximize(content);
    }

    public void close() {
        DockingContent content = record.getContent();
        record.getContainer().restoreFromMaximize(content);

        DockingRegionTabbed panel
                = (DockingRegionTabbed) record.getContainer()
                .getRoot().getDockingChild(record.getPath());
//        DockingRegionTabbed panel =
//                (DockingRegionTabbed)record.getPath().getLast().getDockingChild();
        panel.removeTab(content);
    }

    void restoreFromMaximize(DockingContent content) {
        record.getContainer().restoreFromMaximize(content);
    }

    void closeFromMaximize(DockingContent content) {
        record.getContainer().closeFromMaximize(content);
    }

    void floatFromMaximize(DockingContent content) {
        record.getContainer().floatFromMaximize(content);
    }

}
