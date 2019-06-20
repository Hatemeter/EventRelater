package api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import event_models.Events;

/**
 * @author Mohamad Baalbaki
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventResponse {
    @JsonProperty
    private Events events;

    public Events getEvents() {
        return events;
    }

    public void setEvents(Events events) {
        this.events = events;
    }

    @Override
    public String toString() {
        return "EventResponse{" +
                "events=" + events +
                '}';
    }
}
