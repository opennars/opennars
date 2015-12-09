package objenome.util.bean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class PropertyChangeListenerMock implements PropertyChangeListener {

    private List<PropertyChangeEvent> events = new ArrayList<>();

    public void propertyChange(PropertyChangeEvent evt) {
        this.events.add(evt);
    }

    public List<PropertyChangeEvent> getEvents() {
        return this.events;
    }

}