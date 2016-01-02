package objenome.util.bean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class PropertyChangeListenerMock implements PropertyChangeListener {

    private final List<PropertyChangeEvent> events = new ArrayList<>();

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        events.add(evt);
    }

    public List<PropertyChangeEvent> getEvents() {
        return events;
    }

}