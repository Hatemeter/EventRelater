package event_models;

import common_models.Location;

/**
 * @author Mohamad Baalbaki
 */
public class EventResult {
    private String eventDate;
    private Title title;
    private Location location;
    private Summary summary;
    private double sentiment;
    private String uri;
    private ConceptItem[] concepts;

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public double getSentiment() {
        return sentiment;
    }

    public void setSentiment(double sentiment) {
        this.sentiment = sentiment;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public ConceptItem[] getConcepts() {
        return concepts;
    }

    public void setConcepts(ConceptItem[] concepts) {
        this.concepts = concepts;
    }
}

