package eu.fbk.dh.EventRelater.common_models;

/**
 * @author Mohamad Baalbaki
 */
public class Label {
    private String eng;

    public String getEng() {
        return eng;
    }

    public void setEng(String eng) {
        this.eng = eng;
    }

    @Override
    public String toString() {
        return "Label{" +
                "eng='" + eng + '\'' +
                '}';
    }
}
