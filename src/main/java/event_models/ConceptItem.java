package event_models;

import common_models.Label;

/**
 * @author Mohamad Baalbaki
 */
public class ConceptItem {
    private Label label;

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "ConceptItem{" +
                "label=" + label +
                '}';
    }
}
