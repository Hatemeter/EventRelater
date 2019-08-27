package eu.fbk.dh.EventRelater.event_models;

import java.util.Arrays;

/**
 * @author Mohamad Baalbaki
 */
public class Events {
    private EventResult[] results;
    private long totalResults;
    private long page;
    private long count;
    private long pages;

    public EventResult[] getResults() {
        return results;
    }

    public void setResults(EventResult[] results) {
        this.results = results;
    }

    public long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }

    @Override
    public String toString() {
        return "Events{" +
                "results=" + Arrays.toString(results) +
                ", totalResults=" + totalResults +
                ", page=" + page +
                ", count=" + count +
                ", pages=" + pages +
                '}';
    }
}
