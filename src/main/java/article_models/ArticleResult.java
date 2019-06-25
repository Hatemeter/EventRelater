package article_models;

import event_models.ConceptItem;

import java.util.Arrays;

/**
 * @author Mohamad Baalbaki
 */
public class ArticleResult {
    private String uri;
    private String url;
    private String title;
    private Source source;
    private ConceptItem[] concepts;
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

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

    public ConceptItem[] getConcepts() {
        return concepts;
    }

    public void setConcepts(ConceptItem[] concepts) {
        this.concepts = concepts;
    }

    @Override
    public String toString() {
        return "ArticleResult{" +
                "uri='" + uri + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", source=" + source +
                ", concepts=" + Arrays.toString(concepts) +
                ", body='" + body + '\'' +
                '}';
    }
}
