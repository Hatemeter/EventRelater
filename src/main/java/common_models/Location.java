package common_models;

/**
 * @author Mohamad Baalbaki
 */
public class Location {
    private Label label; //cityname
    private Country country;

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "Location{" +
                "label=" + label +
                ", country=" + country +
                '}';
    }
}
