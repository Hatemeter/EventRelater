package api;

import article_models.ArticleResult;
import article_models.Articles;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Mohamad Baalbaki
 */
@SpringBootApplication
public class EventRelater {
    private static final Logger log = LoggerFactory.getLogger(EventRelater.class); //create a logger file to be able to write logs
    private static String apiKey = null; //my api key in config.properties file
    private static String baseEventsForTopicPageUrl = null; //the base events for topic page api url
    private static String baseGetEventUrl = null; //the base get event api url
    private static String constructedEventsApiUrl = null; //the api url I am going to construct through the api key and base events api url
    private static String constructedGetArticleFromEventUrl = null; //the api url I am going to construct to get the article from the event
    private static String date = null; //the date of the event that you want to search for
    private static String monitoringPageUri = null; //the uri of our monitoring page that was trained with tweets
    private static ArrayList<String> breakingEventsTitles = null; //list that will contain the titles of the final events
    private static ArrayList<Double> breakingEventsSentiments = null; ///list that will contain the sentiments of the final events (same indices as previous list)
    private static ArrayList<String> breakingEvents = null; //list that will concatenate the titles and sentiments
    private static ArrayList<String> articlesUrls = null; //list that will contain the corresponding articles' urls
    private static ArrayList<String> articleSourceNames = null; //list that will contain the corresponding articles' source name (example: The Guardian)
    private double minimumSentiment = 0.0; //the minimum sentiment of an event (minimum being most negative => most important event for us)
    private int minimumSentimentIndex = -1; //index of the minimum sentiment of an event
    private static String articleResponseStr = null; //the article response json string
    private static JsonElement rootJsonTree = null; //the root json tree
    private static JsonObject rootJsonObject = null; //the same tree but as a json object
    private static JsonElement jsonElement = null; //the articles json element
    private static JsonObject articlesJsonObject = null; //the articles json object
    private static Articles articles = null; //the articles pojo
    private static boolean foundFlag = false; //flag
    private static boolean foundFlag2=false;
    private static int temp = 0; //temporary variable that will hold the value of the size of the sentiments list
    private static int temp2=0;

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.print("Kindly enter the date of the Islamophobic hate speech peak: ");
        date = scan.next();
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties"); //load the file that has the aforementioned global variables
            prop.load(input);
            apiKey = prop.getProperty("apiKey"); //get the api key
            //For the events
            monitoringPageUri = prop.getProperty("monitoringPageUri"); //get the uri
            baseEventsForTopicPageUrl = prop.getProperty("baseEventsForTopicPageUrl"); //get the topic page url
            constructedEventsApiUrl = baseEventsForTopicPageUrl + "apiKey=" + apiKey + "&uri=" + monitoringPageUri + "&eventsCount=200"; //constructed the api url with a maximum allowed event count per api call of 200
            //For the article from the corresponding event
            baseGetEventUrl = prop.getProperty("baseGetEventUrl"); //get the base event url
            log.info(constructedEventsApiUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SpringApplication.run(EventRelater.class); //run the springboot application
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) { //create a resttemplate object that is responsible for getting the json object from the url
        return builder.build();
    }

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) throws Exception { //used to return the json object and store it in an object of type EventResponse
        return args -> {
            EventResponse eventResponse = restTemplate.getForObject(constructedEventsApiUrl, EventResponse.class); //created the object eventResponse that now contains the returned json object
            //When this api call doesn't find an event, 20 tokens are spent
            //When this api call finds an event, it uses 34 tokens => 58.82 code runs => 1 CODE RUN COSTS 0.48 EUROS (budget after discount is 28.29 EUROS/MONTH) => THIS CODE CAN BE RAN A MAX OF 1 TIMES PER DAY
            breakingEvents = new ArrayList<>();
            breakingEventsTitles = new ArrayList<>();
            breakingEventsSentiments = new ArrayList<>();
            articlesUrls = new ArrayList<>();
            articleSourceNames = new ArrayList<>();
            System.out.println();
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            //log.info("Length: " + Integer.toString(eventResponse.getEvents().getResults().length));
            for (int i = 0; i < eventResponse.getEvents().getResults().length; i++) //loop on the number of returned events
            {
                foundFlag2=false;
                for(int j=0; j < eventResponse.getEvents().getResults()[i].getConcepts().length; j++) {
                    if (eventResponse.getEvents().getResults()[i].getEventDate().equals(date) && eventResponse.getEvents().getResults()[i].getSentiment() < 0 && !eventResponse.getEvents().getResults()[i].getTitle().getEng().toLowerCase().contains("trump") && (eventResponse.getEvents().getResults()[i].getConcepts()[j].getLabel().getEng().contains("Islam") || eventResponse.getEvents().getResults()[i].getConcepts()[j].getLabel().getEng().contains("Muslim"))) { //if the found event's date matches the inputted one and its sentiment is negative
                        foundFlag2=true;
                        log.info("entered index event: "+i+" entered index concept: "+j);
                        constructedGetArticleFromEventUrl = baseGetEventUrl + "apiKey=" + apiKey + "&eventUri=" + eventResponse.getEvents().getResults()[i].getUri() + "&resultType=articles&includeSourceLocation=true"; //construct the api url that will be responsible of getting the article from the event through the event's uri
                        log.info(constructedGetArticleFromEventUrl);
                        System.out.println(eventResponse.getEvents().getResults()[i].getTitle().getEng() + ": " + eventResponse.getEvents().getResults()[i].getSentiment());
                        System.out.println();
                        articleResponseStr = restTemplate.getForObject(constructedGetArticleFromEventUrl, String.class); //created the object articleResponse that now contains the returned json object because since the eventUri might be different, the json object returned will be different
                        rootJsonTree = parser.parse(articleResponseStr);
                        rootJsonObject = rootJsonTree.getAsJsonObject();
                        for (String key : rootJsonObject.keySet()) {
                            jsonElement = rootJsonObject.get(key);
                            articlesJsonObject = jsonElement.getAsJsonObject();
                            articles = gson.fromJson(articlesJsonObject.get("articles"), Articles.class);
                        }

                        for (ArticleResult result : articles.getResults()) //loop over the event's corresponding articles
                        {
                            if (result.getSource() != null && result.getSource().getLocation() != null && result.getSource().getLocation().getLabel() != null && result.getSource().getLocation().getLabel().getEng() != null && !result.getSource().getLocation().getLabel().getEng().isEmpty() && result.getSource().getLocation().getLabel().getEng().equals("United Kingdom") && result.getSource().getTitle() != null && eventResponse.getEvents() != null && result.getTitle().equals(eventResponse.getEvents().getResults()[i].getTitle().printTitle())) { //if the article's source was from the United Kingdom and the title of the article is equal to the title of the aforementioned event
                                breakingEventsTitles.add(eventResponse.getEvents().getResults()[i].getTitle().printTitle()); //add the title to the list
                                //log.info("title: " + breakingEventsTitles.get(temp2));
                                breakingEventsSentiments.add(eventResponse.getEvents().getResults()[i].getSentiment()); //add the sentiment to the list
                                articlesUrls.add(result.getUrl());
                                articleSourceNames.add(result.getSource().getTitle());
                                foundFlag = true;
                                //log.info("sentiment: " + Double.toString(breakingEventsSentiments.get(temp2)));
                                temp++; //the size of the breakingEventsSentiments array increased by 1
                                //temp2++;
                            }
                            if (foundFlag) {
                                foundFlag = false;
                                break;
                            }

                        }

                    } //if concepts end
                    if(foundFlag2){
                        foundFlag2=false;
                        break;
                    }
                }//concepts loop end
            }//events loop end
            if(temp==0){
                System.out.println("UK newspapers did not report any relevant breaking events that might have sparked Islamophobic hashtags on this day.");
                System.exit(0);
            }
            else if(temp==1){
                System.out.println("UK newspapers reported only 1 relevant breaking event that might have sparked Islamophobic hashtags on this day: ");
                System.out.println();
                System.out.println(breakingEventsTitles.get(0) + ": " + breakingEventsSentiments.get(0) + " / " + articlesUrls.get(0) + " / Source: " + articleSourceNames.get(0));
                System.exit(0);
            }
            else if(temp>=2){
                for (int i = 0; i < temp; i++) //we want to print only the 3 most breaking ones
                {
                    minimumSentiment = Collections.min(breakingEventsSentiments); //calculate the minimum sentiment
                    //log.info("minimum: "+Double.toString(minimumSentiment));
                    minimumSentimentIndex = breakingEventsSentiments.indexOf(minimumSentiment); //get its index
                    //log.info("index: "+minimumSentimentIndex);
                    breakingEvents.add(breakingEventsTitles.get(minimumSentimentIndex) + ": " + minimumSentiment + " / " + articlesUrls.get(minimumSentimentIndex) + " / Source: " + articleSourceNames.get(minimumSentimentIndex)); //concatenate the title of the event with the minimum sentiment with the url with the source and add them to the list
                    breakingEventsTitles.remove(minimumSentimentIndex); //remove the title of the event with the minimum index because we used already used it
                    breakingEventsSentiments.remove(minimumSentimentIndex); //remove the sentiment of the event with the minimum index because we already used it
                    articlesUrls.remove(minimumSentimentIndex); //remove the url at the minimum index because we already used it
                    articleSourceNames.remove(minimumSentimentIndex);
                } //loop and calculate minimum again in the remaining list
                if(temp==2){
                    System.out.println();
                    System.out.println("UK newspapers reported 2 relevant breaking events that might have sparked Islamophobic hashtags on this day: ");
                    System.out.println();
                    for (int i = 0; i < temp; i++) {
                        System.out.println(breakingEvents.get(i)); //print the corresponding events
                    }
                    System.exit(0);
                }
                else if(temp>=3){
                    temp=3;
                    System.out.println();
                    System.out.println("UK newspapers reported 3 relevant breaking events that might have sparked Islamophobic hashtags on this day: ");
                    System.out.println();
                    for (int i = 0; i < temp; i++) {
                        System.out.println(breakingEvents.get(i)); //print the corresponding events
                    }
                    System.exit(0);
                }
            }

        };
    }
}