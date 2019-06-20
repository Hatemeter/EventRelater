package common_models;

/**
 * @author Mohamad Baalbaki
 */
public class Country {
    private Label label;

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "Country{" +
                "label=" + label +
                '}';
    }
}
