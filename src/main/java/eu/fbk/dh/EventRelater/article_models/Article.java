package eu.fbk.dh.EventRelater.article_models;

public class Article {
    private int articleIndex;
    private double articleSentiment;
    private String articleText;
    private String articleTitle;
    private String articleUrl;
    private String articleSourceTitle;

    public Article(int articleIndex, double articleSentiment, String articleText, String articleTitle, String articleUrl, String articleSourceTitle) {
        this.articleIndex = articleIndex;
        this.articleSentiment = articleSentiment;
        this.articleText = articleText;
        this.articleTitle=articleTitle;
        this.articleUrl=articleUrl;
        this.articleSourceTitle=articleSourceTitle;
    }

    public int getArticleIndex() {
        return articleIndex;
    }

    public void setArticleIndex(int articleIndex) {
        this.articleIndex = articleIndex;
    }

    public double getArticleSentiment() {
        return articleSentiment;
    }

    public void setArticleSentiment(double articleSentiment) {
        this.articleSentiment = articleSentiment;
    }

    public String getArticleText() {
        return articleText;
    }

    public void setArticleText(String articleText) {
        this.articleText = articleText;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public String getArticleUrl() {
        return articleUrl;
    }

    public void setArticleUrl(String articleUrl) {
        this.articleUrl = articleUrl;
    }

    public String getArticleSourceTitle() {
        return articleSourceTitle;
    }

    public void setArticleSourceTitle(String articleSourceTitle) {
        this.articleSourceTitle = articleSourceTitle;
    }
}
