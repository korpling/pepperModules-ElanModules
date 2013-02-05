package mpi.eudico.client.util;


public class SelectableObject {
    private Object value;
    private boolean selected;
    
    public SelectableObject() {
    }
    
    public SelectableObject(Object value, boolean selected) {
        this.value = value;
        this.selected = selected;
    }
    
    public String toString() {
        if (value != null) {
            return value.toString();    
        }
        return null;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
}
