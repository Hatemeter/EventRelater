package eu.fbk.dh.EventRelater.api;

import java.io.IOException;
import java.util.*;

import com.google.gson.*;
import eu.fbk.dh.EventRelater.article_models.Article;
import eu.fbk.dh.EventRelater.article_models.ArticleResult;
import eu.fbk.dh.EventRelater.article_models.Articles;
import eu.fbk.dh.EventRelater.sentimentanalyzers.FrenchAnalyzer;
import eu.fbk.dh.EventRelater.sentimentanalyzers.ItalianAnalyzer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

public class ApiHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiHandler.class);
    private String apiKey;
    private String monitoringPageUri;
    private String baseEventsForTopicPageUrl;
    private String baseGetEventUrl;
    private String language;
    private String date;
    private RestTemplateBuilder builder;
    private RestTemplate restTemplate;
    private EventResponse eventResponse;
    private String frenchLemmasApiKey;
    private String frenchLemmasBaseUrl;
    private boolean test=true;
    //When this api call doesn't find an event, 20 tokens are spent
    //ENG: When this api call finds an event, it around 30 tokens => 67.28 code runs => 1 CODE RUN COSTS 0.42 EUROS (budget after discount is 28.26 EUROS/MONTH) => THIS CODE CAN BE RAN A MAX OF 2.24 TIMES PER DAY => 2 times
    //ITA: Around 5 tokens per search
    //FR: Around 20 tokens per search


    public ApiHandler(String apiKey, String monitoringPageUri, String baseEventsForTopicPageUrl, String baseGetEventUrl, String language, String date, String frenchLemmasApiKey, String frenchLemmasBaseUrl) {
        this.apiKey = apiKey;
        this.monitoringPageUri = monitoringPageUri;
        this.baseEventsForTopicPageUrl = baseEventsForTopicPageUrl;
        this.baseGetEventUrl = baseGetEventUrl;
        this.language = language;
        this.date = date;
        this.frenchLemmasApiKey = frenchLemmasApiKey;
        this.frenchLemmasBaseUrl = frenchLemmasBaseUrl;
        builder = new RestTemplateBuilder();
        restTemplate = builder.build();
    }

     public String getConstructedEventsApiUrl() {
        String constructedEventsApiUrl = baseEventsForTopicPageUrl + "apiKey=" + apiKey + "&uri=" + monitoringPageUri + "&eventsCount=200"; //constructed the api url with a maximum allowed event count per api call of 200
        log.info(constructedEventsApiUrl);
        return constructedEventsApiUrl;
    }

    public LinkedHashMap<String, Integer> getEventUrisAndIndicesFromUrl() throws IOException {
        /*if(test){
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            File initialFile = new File("/home/baalbaki/Desktop/test.json");
            InputStream jsonFileStream = new FileInputStream(initialFile);
            eventResponse = (EventResponse) mapper.readValue(jsonFileStream, EventResponse.class);
        }*/
        eventResponse = restTemplate.getForObject(getConstructedEventsApiUrl(), EventResponse.class); //created the object eventResponse that now contains the returned json object
        //When this api call doesn't find an event, 20 tokens are spent
        //When this api call finds an event, it uses 34 tokens => 58.82 code runs => 1 CODE RUN COSTS 0.48 EUROS (budget after discount is 28.29 EUROS/MONTH) => THIS CODE CAN BE RAN A MAX OF 1 TIMES PER DAY
        //Now my algorithm only consumes 25 tokens per search instead of 34
        LinkedHashMap<String, Integer> eventUrisAndIndices = new LinkedHashMap<>();
        boolean conceptFoundFlag = false;
        System.out.println();
        log.info("Testing: Length of event uris and indices: " + Integer.toString(eventResponse.getEvents().getResults().length));
        for (int i = 0; i < eventResponse.getEvents().getResults().length; i++) { //loop on the number of returned events
            for (int j = 0; j < eventResponse.getEvents().getResults()[i].getConcepts().length; j++) {
                if ((language.equals("eng") && eventResponse.getEvents().getResults()[i].getEventDate().equals(date) && eventResponse.getEvents().getResults()[i].getSentiment() < 0 && !eventResponse.getEvents().getResults()[i].getConcepts()[j].getLabel().getEng().toLowerCase().contains("trump") && !eventResponse.getEvents().getResults()[i].getTitle().getEng().toLowerCase().contains("trump") && (eventResponse.getEvents().getResults()[i].getConcepts()[j].getLabel().getEng().contains("Islam") || eventResponse.getEvents().getResults()[i].getConcepts()[j].getLabel().getEng().contains("Muslim")))
                        || (language.equals("ita") && eventResponse.getEvents().getResults()[i].getEventDate().equals(date) && (eventResponse.getEvents().getResults()[i].getConcepts()[j].getLabel().getEng().contains("Islam") || eventResponse.getEvents().getResults()[i].getConcepts()[j].getLabel().getEng().contains("Muslim")))
                        || (language.equals("fra") && eventResponse.getEvents().getResults()[i].getEventDate().equals(date) && (eventResponse.getEvents().getResults()[i].getConcepts()[j].getLabel().getEng().contains("Islam") || eventResponse.getEvents().getResults()[i].getConcepts()[j].getLabel().getEng().contains("Muslim")))) { //if the found event's date matches the inputted one and its sentiment is negative and it's not related to trump and it is remotely related to Islam
                    conceptFoundFlag = true; //concept was found
                    log.info("Testing: Entered index event: " + i + " entered index concept: " + j);
                    log.info("Testing: Its corresponding uri: " + eventResponse.getEvents().getResults()[i].getUri());
                    eventUrisAndIndices.put(eventResponse.getEvents().getResults()[i].getUri(), i);
                    if (conceptFoundFlag) {
                        conceptFoundFlag = false;
                        break;
                    }
                }
            } //if concepts end
            //returns list of uris
        }
        return eventUrisAndIndices;
    }

    public String getConstructedArticlesFromEventUrl(String eventUri) {
        String constructedArticlesFromEventUrl = baseGetEventUrl + "apiKey=" + apiKey + "&eventUri=" + eventUri + "&resultType=articles&includeSourceLocation=true"; //construct the api url that will be responsible of getting the article from the event through the event's uri
        return constructedArticlesFromEventUrl;
    }

    public JsonArray getArticlesAndSentiments() throws IOException {
        JsonArray finalEventsAndSentiments = new JsonArray();
        ItalianAnalyzer italianAnalyzer = null;
        FrenchAnalyzer frenchAnalyzer = null;
        if (language.equals("ita")) italianAnalyzer = new ItalianAnalyzer();
        else if (language.equals("fra")) frenchAnalyzer = new FrenchAnalyzer(frenchLemmasApiKey, frenchLemmasBaseUrl);
        LinkedHashMap<String, Integer> eventUrisAndIndices = getEventUrisAndIndicesFromUrl();
        Gson gson = new Gson(); //here I am using gson instead of spring because I didn't know how to parse the root json object with a weird name in spring
        JsonParser parser = new JsonParser();
        JsonObject articlesJsonObject;
        Articles articles = null;
        int articleIndex = 0;
        ArrayList<Article> articleArrayList=new ArrayList<>();
        boolean articleFoundFlag = false;
        ArrayList<String> articlesTextList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : eventUrisAndIndices.entrySet()) {
            String constructedArticlesFromEventUrl = getConstructedArticlesFromEventUrl(entry.getKey());
            log.info("Testing: All articles from event url: " + constructedArticlesFromEventUrl);
            //String articleResponseStr = null;
            /*if(test){
                articleResponseStr=new String(Files.readAllBytes(Paths.get("/home/baalbaki/Desktop/events.json")), StandardCharsets.UTF_8);
            }*/
            String articleResponseStr = restTemplate.getForObject(constructedArticlesFromEventUrl, String.class); //created the object articleResponse that now contains the returned json object because since the eventUri might be different, the json object returned will be different
            JsonElement rootJsonTree = parser.parse(articleResponseStr); //parsing the json string
            JsonObject rootJsonObject = rootJsonTree.getAsJsonObject();
            for (String key : rootJsonObject.keySet()) {
                JsonElement jsonElement = rootJsonObject.get(key);
                articlesJsonObject = jsonElement.getAsJsonObject();
                articles = gson.fromJson(articlesJsonObject.get("articles"), Articles.class);
            }
            for (ArticleResult result : articles.getResults()) //loop over the event's corresponding articles
            {
                if (language.equals("eng")) {
                    if(result!=null && eventResponse.getEvents()!= null && result.getSource()!=null && result.getSource().getLocation()!=null && result.getSource().getLocation().getLabel()!=null && result.getSource().getLocation().getLabel().getEng()!=null && (result.getSource().getLocation().getLabel().getEng().equals("United Kingdom") || (result.getSource().getLocation().getCountry() != null && result.getSource().getLocation().getCountry().getLabel() != null && result.getSource().getLocation().getCountry().getLabel().getEng()!=null && result.getSource().getLocation().getCountry().getLabel().getEng().equals("United Kingdom")) && result.getSource().getTitle() != null && result.getTitle().equals(eventResponse.getEvents().getResults()[entry.getValue()].getTitle().getEng()))){
                        articleFoundFlag = true; //article was found
                        log.info("Testing: Relevant article was found: " + eventResponse.getEvents().getResults()[entry.getValue()].getSentiment() + ": " + eventResponse.getEvents().getResults()[entry.getValue()].getTitle().getEng() + " / " + result.getUrl() + " / Source: " + result.getSource().getTitle());
                        Article article=new Article(articleIndex,eventResponse.getEvents().getResults()[entry.getValue()].getSentiment(),result.getBody().trim().replaceAll("\n", ""),eventResponse.getEvents().getResults()[entry.getValue()].getTitle().getEng(),result.getUrl(),result.getSource().getTitle());
                        articleArrayList.add(article);
                        articleIndex++;
                    }
                } else if (language.equals("ita")) {
                    if(result!=null && eventResponse.getEvents()!= null && result.getSource()!=null && result.getSource().getLocation()!=null && result.getSource().getLocation().getLabel()!=null && result.getSource().getLocation().getLabel().getEng()!=null && (result.getSource().getLocation().getLabel().getEng().equals("Italy") || (result.getSource().getLocation().getCountry() != null && result.getSource().getLocation().getCountry().getLabel() != null && result.getSource().getLocation().getCountry().getLabel().getEng()!=null && result.getSource().getLocation().getCountry().getLabel().getEng().equals("Italy")) && result.getSource().getTitle() != null && result.getTitle().equals(eventResponse.getEvents().getResults()[entry.getValue()].getTitle().getIta()))){
                        articleFoundFlag = true; //article was found
                        log.info("Testing: Relevant article was found: " + eventResponse.getEvents().getResults()[entry.getValue()].getTitle().getIta() + ". " + result.getBody() + " / " + result.getUrl() + " / Source: " + result.getSource().getTitle());
                        articlesTextList.add(eventResponse.getEvents().getResults()[entry.getValue()].getTitle().getIta().trim().replaceAll("\n", "") + ". /ENDOFTITLE/. " + result.getBody().trim().replaceAll("(\n)+", ". ") + " /URL/ " + result.getUrl() + " /SOURCE/ " + result.getSource().getTitle() + " /ENDOFARTICLE/");
                    }
                } else if (language.equals("fra")) {
                    if(result!=null && eventResponse.getEvents()!= null && result.getSource()!=null && result.getSource().getLocation()!=null && result.getSource().getLocation().getLabel()!=null && result.getSource().getLocation().getLabel().getEng()!=null && (result.getSource().getLocation().getLabel().getEng().equals("France") || (result.getSource().getLocation().getCountry() != null && result.getSource().getLocation().getCountry().getLabel() != null && result.getSource().getLocation().getCountry().getLabel().getEng()!=null && result.getSource().getLocation().getCountry().getLabel().getEng().equals("France")) && result.getSource().getTitle() != null && result.getTitle().equals(eventResponse.getEvents().getResults()[entry.getValue()].getTitle().getFra()))){
                        articleFoundFlag = true; //article was found
                        log.info("Testing: Relevant article was found: " + eventResponse.getEvents().getResults()[entry.getValue()].getTitle().getFra() + ". " + result.getBody() + " / " + result.getUrl() + " / Source: " + result.getSource().getTitle());
                        articlesTextList.add(eventResponse.getEvents().getResults()[entry.getValue()].getTitle().getFra().trim().replaceAll("\n", "") + ". /ENDOFTITLE/. " + result.getBody().trim().replaceAll("(\n)+", ". ") + " /URL/ " + result.getUrl() + " /SOURCE/ " + result.getSource().getTitle() + " /ENDOFARTICLE/");
                    }
                }
                if (articleFoundFlag) {
                    articleFoundFlag = false;
                    break;
                }
            }
        }

        if (language.equals("ita")) {
            ArrayList<ArrayList<Integer>> sentiments = italianAnalyzer.getSentiments(articlesTextList);
            log.info("Testing: "+"Sentiments size: "+sentiments.size());
            for (int i = 0; i < sentiments.size(); i++) {
                    if (sentiments.get(i).get(0) > sentiments.get(i).get(1)) {
                        log.info(sentiments.get(i).get(0)+", "+sentiments.get(i).get(1));
                        Article article=new Article(i,(double) sentiments.get(i).get(0),articlesTextList.get(i), StringUtils.substringBefore(articlesTextList.get(i), " /ENDOFTITLE/"), StringUtils.substringBetween(articlesTextList.get(i), "/URL/ ", " /SOURCE/"), StringUtils.substringBetween(articlesTextList.get(i), "/SOURCE/ ", " /ENDOFARTICLE/"));
                        articleArrayList.add(article);
                    }
            }
        } else if (language.equals("fra")) {
            ArrayList<Integer> sentiments = new ArrayList<>();
            try {
                 sentiments = frenchAnalyzer.getSentiments(articlesTextList);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            log.info("Testing: "+"Sentiments size: "+sentiments.size());
            for (int i = 0; i < sentiments.size(); i++) {
                    if (sentiments.get(i) > 7) {
                        System.out.println("Article "+i+": "+(sentiments.get(i)+": "+StringUtils.substringBefore(articlesTextList.get(i), " /ENDOFTITLE/") + " / " + StringUtils.substringBetween(articlesTextList.get(i), "/URL/ ", " /SOURCE/") + " / Source: " + StringUtils.substringBetween(articlesTextList.get(i), "/SOURCE/ ", " /ENDOFARTICLE/")));
                        Article article=new Article(i,(double) sentiments.get(i), articlesTextList.get(i), StringUtils.substringBefore(articlesTextList.get(i), " /ENDOFTITLE/") , StringUtils.substringBetween(articlesTextList.get(i), "/URL/ ", " /SOURCE/") , StringUtils.substringBetween(articlesTextList.get(i), "/SOURCE/ ", " /ENDOFARTICLE/"));
                        articleArrayList.add(article);
                    }
                }
            /*for (Map.Entry<Integer, LinkedHashMap<Double, String>> mainEntry : breakingEventsInfo.entrySet()) {
                int index = mainEntry.getKey();
                for (Map.Entry<Double, String> insideEntry : mainEntry.getValue().entrySet()) {
                    double sentiment = insideEntry.getKey();
                    String article = insideEntry.getValue();
                    System.out.println(index+": "+sentiment+": "+article);
                    break;
                }
            }

            System.exit(0);*/




        }
        System.out.println();

        if (articleArrayList.size() == 0) {
            if (language.equals("eng"))
                System.out.println("British newspapers did not report any relevant breaking events that might have sparked Islamophobic hashtags on this day: "+date);
            else if (language.equals("ita"))
                System.out.println("Italian newspapers did not report any relevant breaking events that might have sparked Islamophobic hashtags on this day: "+date);
            else if (language.equals("fra"))
                System.out.println("French newspapers did not report any relevant breaking events that might have sparked Islamophobic hashtags on this day: "+date);
            return null;
        } else if (articleArrayList.size() == 1) {
            if (language.equals("eng"))
                System.out.println("British newspapers reported only 1 relevant breaking event that might have sparked Islamophobic hashtags on this day: "+date);
            else if (language.equals("ita"))
                System.out.println("Italian newspapers reported only 1 relevant breaking event that might have sparked Islamophobic hashtags on this day: "+date);
            else if (language.equals("fra"))
                System.out.println("French newspapers reported only 1 relevant breaking event that might have sparked Islamophobic hashtags on this day: "+date);
            System.out.println();
            JsonObject event=new JsonObject();
            event.addProperty("sentiment",articleArrayList.get(0).getArticleSentiment());
            event.addProperty("event",articleArrayList.get(0).getArticleTitle());
            event.addProperty("url",articleArrayList.get(0).getArticleUrl());
            event.addProperty("source",articleArrayList.get(0).getArticleSourceTitle());
            finalEventsAndSentiments.add(event);
        }

        else if (articleArrayList.size() >= 2) {
            int temp=articleArrayList.size();
            for(int i=0;i<temp;i++){
                if(language.equals("eng")){
                    Article minArticle=articleArrayList.parallelStream().min(Comparator.comparing(a->((Article) a).getArticleSentiment())).get();
                    JsonObject event=new JsonObject();
                    event.addProperty("sentiment",minArticle.getArticleSentiment());
                    event.addProperty("event",minArticle.getArticleTitle());
                    event.addProperty("url",minArticle.getArticleUrl());
                    event.addProperty("source",minArticle.getArticleSourceTitle());
                    finalEventsAndSentiments.add(event);
                    articleArrayList.remove(minArticle);
                }
                else if(language.equals("ita") || language.equals("fra")) {
                    Article maxArticle=articleArrayList.parallelStream().max(Comparator.comparing(a->((Article) a).getArticleSentiment())).get();
                    System.out.println("Max article: "+maxArticle.getArticleSentiment()+": "+maxArticle.getArticleText());
                    JsonObject event=new JsonObject();
                    event.addProperty("sentiment",maxArticle.getArticleSentiment());
                    event.addProperty("event",maxArticle.getArticleTitle());
                    event.addProperty("url",maxArticle.getArticleUrl());
                    event.addProperty("source",maxArticle.getArticleSourceTitle());
                    finalEventsAndSentiments.add(event);
                    articleArrayList.remove(maxArticle);
                }
            }

            /*if(language.equals("eng")){
                    Collections.sort(finalEventsAndSentiments);
                    Collections.reverse(finalEventsAndSentiments);

            }*/

            if (finalEventsAndSentiments.size() == 2) {
                System.out.println();
                if (language.equals("eng"))
                    System.out.println("British newspapers reported 2 relevant breaking events that might have sparked Islamophobic hashtags on this day: "+date);
                else if (language.equals("ita"))
                    System.out.println("Italian newspapers reported 2 relevant breaking events that might have sparked Islamophobic hashtags on this day: "+date);
                else if (language.equals("french"))
                    System.out.println("French newspapers reported 2 relevant breaking events that might have sparked Islamophobic hashtags on this day: "+date);
                System.out.println();
            } else if (finalEventsAndSentiments.size() >= 3) {
                System.out.println();
                if (language.equals("eng"))
                    System.out.println("British newspapers reported 3 relevant breaking events that might have sparked Islamophobic hashtags on this day: "+date);
                else if (language.equals("ita"))
                    System.out.println("Italian newspapers reported 3 relevant breaking events that might have sparked Islamophobic hashtags on this day: "+date);
                else if (language.equals("fra"))
                    System.out.println("French newspapers reported 3 relevant breaking events that might have sparked Islamophobic hashtags on this day: "+date);
                System.out.println();
            }
        }

        /*for(int i=0;i<finalEventsAndSentiments.size();i++){
            System.out.println("MEN API HANDLER: "+finalEventsAndSentiments.get(i));
        }*/

        return finalEventsAndSentiments;


    }

}

/*int temp = sentiments.size();
            System.out.println();
            int sentimentIndex;
            int reductions=0;
            for (int i = 0; i < temp; i++) {
                sentimentIndex=i-reductions;
                if ((sentiments.get(0).get(sentimentIndex) <= sentiments.get(1).get(sentimentIndex))) {
                    sentiments.get(0).remove(sentimentIndex);
                    sentiments.get(1).remove(sentimentIndex);
                    italianArticlesTextList.remove(sentimentIndex);
                    reductions++;
                }
                else {
                    finalEventsAndSentiments.add(sentiments.get(0).get(sentimentIndex) + ": " + StringUtils.substringBefore(italianArticlesTextList.get(sentimentIndex), " /ENDOFTITLE/") + " / " + StringUtils.substringBetween(italianArticlesTextList.get(sentimentIndex), "/URL/ ", " /SOURCE/") + " / Source: " + StringUtils.substringBetween(italianArticlesTextList.get(sentimentIndex), "/SOURCE/ ", " /ENDOFARTICLE/")); //concatenate the title of the event with the minimum sentiment with the url with the source and add them to the list
                }
            }

            if (sentiments.size() != 0 && sentiments.size()>1) {
                Collections.sort(finalEventsAndSentiments, Collections.reverseOrder());
                if (finalEventsAndSentiments.size() > 3) {
                    finalEventsAndSentiments = (ArrayList<String>) finalEventsAndSentiments.subList(0, 2);
                }
            }*/