package eu.fbk.dh.EventRelater.event_models;

/**
 * @author Mohamad Baalbaki
 */
public class Summary {
    private String ita;
    private String eng;

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

    @Override
    public String toString() {
        return "Summary{" +
                "ita='" + ita + '\'' +
                ", eng='" + eng + '\'' +
                '}';
    }
}
