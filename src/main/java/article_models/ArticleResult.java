package article_models;

/**
 * @author Mohamad Baalbaki
 */
public class ArticleResult {
    private String uri;
    private String url;
    private String title;
    private Source source;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "ArticleResult{" +
                "uri='" + uri + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", source=" + source +
                '}';
    }
}
