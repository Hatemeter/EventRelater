package eu.fbk.dh.EventRelater.article_models;

import eu.fbk.dh.EventRelater.common_models.Location;

/**
 * @author Mohamad Baalbaki
 */
public class Source {
    private String title;
    private Location location;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Source{" +
                "title='" + title + '\'' +
                ", location=" + location +
                '}';
    }
}
