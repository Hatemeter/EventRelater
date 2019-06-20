package event_models;

/**
 * @author Mohamad Baalbaki
 */
public class Title {
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
        return "Title{" +
                "ita='" + ita + '\'' +
                ", eng='" + eng + '\'' +
                '}';
    }

    public String printTitle(){
        return eng;
    }
}
