package eu.fbk.dh.EventRelater.event_models;

/**
 * @author Mohamad Baalbaki
 */
public class Title {
    private String ita;
    private String eng;
    private String fra;

    public String getIta() {
        return ita;
    }

    public void setIta(String ita) {
        this.ita = ita;
    }

    public String getEng() {
        return eng;
    }

    public void setEng(String eng) {
        this.eng = eng;
    }

    public String getFra() {
        return fra;
    }

    public void setFra(String fra) {
        this.fra = fra;
    }

    @Override
    public String toString() {
        return "Title{" +
                "ita='" + ita + '\'' +
                ", eng='" + eng + '\'' +
                ", fra='" + fra + '\'' +
                '}';
    }
}
