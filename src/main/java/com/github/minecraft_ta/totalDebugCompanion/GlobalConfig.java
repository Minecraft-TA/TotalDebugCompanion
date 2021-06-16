package com.github.minecraft_ta.totalDebugCompanion;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

public class GlobalConfig {

    private static GlobalConfig INSTANCE;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private final Map<String, Object> properties = new HashMap<>();

    private GlobalConfig() {
        addDefaults();
    }

    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(property, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void setValue(String property, Object value) {
        pcs.firePropertyChange(property, properties.put(property, value), value);
    }

    public <T> T getValue(String property) {
        return (T) properties.get(property);
    }

    private void addDefaults() {
        properties.put("fontSize", 14f);
        properties.put("scrollMul", 1.2f);
    }

    public static GlobalConfig getInstance() {
        if (INSTANCE == null)
            INSTANCE = new GlobalConfig();

        return INSTANCE;
    }
}
