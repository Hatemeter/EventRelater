package api;

import article_models.ArticleResult;
import article_models.Articles;
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

import java.io.*;
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
    private static LinkedHashMap<Integer, LinkedHashMap<Double, String>> breakingEventsInfo = null;
    private double minimumSentiment = 0.0; //the minimum sentiment of an event (minimum being most negative => most important event for us)
    private int minimumSentimentIndex = -1; //index of the minimum sentiment of an event
    private static String articleResponseStr = null; //the article response json string
    private static JsonElement rootJsonTree = null; //the root json tree
    private static JsonObject rootJsonObject = null; //the same tree but as a json object
    private static JsonElement jsonElement = null; //the articles json element
    private static JsonObject articlesJsonObject = null; //the articles json object
    private static Articles articles = null; //the articles pojo
    private static boolean articleFoundFlag = false; //flag for the found article
    private static boolean conceptFoundFlag = false; //flag for the found concept
    private static int tempSize = 0; //temporary variable that will hold the value of the size of the sentiments list
    private static String language = null; //variable that denotes the language we are working in
    private static BufferedWriter writer = null; //buffered write for italian and french articles text files
    private static int hashmapIndex = 0; //temporary hashmap index
    private static ArrayList<String> breakingEvents = null; //final events arraylist
    private static int globalHashmapIndex = 0;
    private static String articleInfo = null;

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.print("Kindly enter the language:");
        try {
            language = scan.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.print("Kindly enter the date of the Islamophobic hate speech peak: ");
        try {
            date = scan.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties"); //load the file that has the aforementioned global variables
            prop.load(input);
            apiKey = prop.getProperty("apiKey"); //get the api key
            //For the events
            if (language.equals("eng")) {
                monitoringPageUri = prop.getProperty("englishMonitoringPageUri"); //get the english page uri
            } else if (language.equals("ita")) {
                monitoringPageUri = prop.getProperty("italianMonitoringPageUri"); //get the italian page uri
                writer = new BufferedWriter(new FileWriter("/home/baalbaki/IdeaProjects/EventRelater/italianarticles.txt")); //create the italian articles file for later sentiment analysis
                writer.write("");
            } else if (language.equals("fra")) {
                monitoringPageUri = prop.getProperty("frenchMonitoringPageUri"); //get the french page uri
                writer = new BufferedWriter(new FileWriter("/home/baalbaki/IdeaProjects/EventRelater/frencharticles.txt")); //create the french articles file for later sentiment analysis
                writer.write("");
            }
            baseEventsForTopicPageUrl = prop.getProperty("baseEventsForTopicPageUrl"); //get the topic page url
            constructedEventsApiUrl = baseEventsForTopicPageUrl + "apiKey=" + apiKey + "&uri=" + monitoringPageUri + "&eventsCount=200"; //constructed the api url with a maximum allowed event count per api call of 200
            //For the article from the corresponding event
            baseGetEventUrl = prop.getProperty("baseGetEventUrl"); //get the base event url
            log.info(constructedEventsApiUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SpringApplication.run(EventRelater.class); //run the springboot application
        // TODO: 24/06/19 Store the already computed results in a database for faster and cheaper access later on
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
            //ENG: When this api call finds an event, it around 30 tokens => 67.28 code runs => 1 CODE RUN COSTS 0.42 EUROS (budget after discount is 28.26 EUROS/MONTH) => THIS CODE CAN BE RAN A MAX OF 2.24 TIMES PER DAY => 2 times
            //ITA: Around 5 tokens per search
            //FR: Around 20 tokens per search

            breakingEventsInfo = new LinkedHashMap<>();
            breakingEvents = new ArrayList<>();
            System.out.println();
            Gson gson = new Gson(); //here I am using gson insteand of spring because I didn't know how to parse the root json object with a weird name in spring
            JsonParser parser = new JsonParser();
            //log.info("Length: " + Integer.toString(eventResponse.getEvents().getResults().length));
            for (int i = 0; i < eventResponse.getEvents().getResults().length; i++) //loop on the number of returned events
            {
                conceptFoundFlag = false;
                for (int j = 0; j < eventResponse.getEvents().getResults()[i].getConcepts().length; j++) { //loop on the concepts
                    if (language.equals("eng") && eventResponse.getEvents().getResults()[i].getEventDate().equals(date) && eventResponse.getEvents().getResults()[i].getSentiment() < 0 && !eventResponse.getEvents().getResults()[i].getConcepts()[j].getLabel().getEng().toLowerCase().contains("trump") && !eventResponse.getEvents().getResults()[i].getTitle().getEng().toLowerCase().contains("trump") && (eventResponse.getEvents().getResults()[i].getConcepts()[j].getLabel().getEng().contains("Islam") || eventResponse.getEvents().getResults()[i].getConcepts()[j].getLabel().getEng().contains("Muslim"))) { //if the found event's date matches the inputted one and its sentiment is negative and it's not related to trump and it is remotely related to Islam
                        conceptFoundFlag = true; //concept was found
                        log.info("entered index event: " + i + " entered index concept: " + j);
                        constructedGetArticleFromEventUrl = baseGetEventUrl + "apiKey=" + apiKey + "&eventUri=" + eventResponse.getEvents().getResults()[i].getUri() + "&resultType=articles&includeSourceLocation=true"; //construct the api url that will be responsible of getting the article from the event through the event's uri
                        log.info(constructedGetArticleFromEventUrl);
                        System.out.println(eventResponse.getEvents().getResults()[i].getTitle().getEng() + ": " + eventResponse.getEvents().getResults()[i].getSentiment());
                        System.out.println();
                        articleResponseStr = restTemplate.getForObject(constructedGetArticleFromEventUrl, String.class); //created the object articleResponse that now contains the returned json object because since the eventUri might be different, the json object returned will be different
                        rootJsonTree = parser.parse(articleResponseStr); //parsing the json string
                        rootJsonObject = rootJsonTree.getAsJsonObject();
                        for (String key : rootJsonObject.keySet()) {
                            jsonElement = rootJsonObject.get(key);
                            articlesJsonObject = jsonElement.getAsJsonObject();
                            articles = gson.fromJson(articlesJsonObject.get("articles"), Articles.class);
                        }

                        for (ArticleResult result : articles.getResults()) //loop over the event's corresponding articles
                        {
                            if (result.getSource() != null && result.getSource().getLocation() != null && result.getSource().getLocation().getLabel() != null && result.getSource().getLocation().getLabel().getEng() != null && !result.getSource().getLocation().getLabel().getEng().isEmpty() && (result.getSource().getLocation().getLabel().getEng().equals("United Kingdom") || (result.getSource().getLocation().getCountry() != null && result.getSource().getLocation().getCountry().getLabel() != null && result.getSource().getLocation().getCountry().getLabel().getEng().equals("United Kingdom"))) && result.getSource().getTitle() != null && eventResponse.getEvents() != null && result.getTitle().equals(eventResponse.getEvents().getResults()[i].getTitle().getEng())) { //if the article's source was from the United Kingdom and the title of the article is equal to the title of the aforementioned event
                                //log.info("title: " + breakingEventsTitles.get(temp2));
                                //breakingEventsSentiments.add(eventResponse.getEvents().getResults()[i].getSentiment()); //add the sentiment to the list
                                LinkedHashMap<Double, String> articleInfo = new LinkedHashMap<>();
                                System.out.println("This article was found: " + eventResponse.getEvents().getResults()[i].getSentiment() + ": " + eventResponse.getEvents().getResults()[i].getTitle().getEng() + " / " + result.getUrl() + " / Source: " + result.getSource().getTitle());
                                articleInfo.put(eventResponse.getEvents().getResults()[i].getSentiment(), eventResponse.getEvents().getResults()[i].getTitle().getEng() + " / " + result.getUrl() + " / Source: " + result.getSource().getTitle());
                                breakingEventsInfo.put(hashmapIndex, articleInfo);
                                articleFoundFlag = true; //article was found
                                //log.info("sentiment: " + Double.toString(breakingEventsSentiments.get(temp2)));
                                hashmapIndex++;
                                tempSize++; //the size of the breakingEventsSentiments array increased by 1
                            }
                            if (articleFoundFlag) {
                                articleFoundFlag = false;
                                break;
                            }

                        }

                    } //if concepts end

                    else if (language.equals("ita") && eventResponse.getEvents().getResults()[i].getEventDate().equals(date) && (eventResponse.getEvents().getResults()[i].getConcepts()[j].getLabel().getEng().contains("Islam") || eventResponse.getEvents().getResults()[i].getConcepts()[j].getLabel().getEng().contains("Muslim"))) { //if the language is italian
                        conceptFoundFlag = true; //concept was found
                        log.info("entered index event: " + i + " entered index concept: " + j);
                        constructedGetArticleFromEventUrl = baseGetEventUrl + "apiKey=" + apiKey + "&eventUri=" + eventResponse.getEvents().getResults()[i].getUri() + "&resultType=articles&includeSourceLocation=true"; //construct the api url that will be responsible of getting the article from the event through the event's uri
                        log.info(constructedGetArticleFromEventUrl);
                        //System.out.println(eventResponse.getEvents().getResults()[i].getTitle().getIta());
                        System.out.println();
                        articleResponseStr = restTemplate.getForObject(constructedGetArticleFromEventUrl, String.class); //created the object articleResponse that now contains the returned json object because since the eventUri might be different, the json object returned will be different
                        rootJsonTree = parser.parse(articleResponseStr);
                        rootJsonObject = rootJsonTree.getAsJsonObject();
                        for (String key : rootJsonObject.keySet()) {
                            jsonElement = rootJsonObject.get(key);
                            articlesJsonObject = jsonElement.getAsJsonObject();
                            articles = gson.fromJson(articlesJsonObject.get("articles"), Articles.class);
                        }

                        //write file
                        //writer.write(str);
                        //writer.close();

                        /*String str2 = "World";
                        writer = new BufferedWriter(new FileWriter("/home/baalbaki/IdeaProjects/EventRelater/italianarticles.txt", true));
                        writer.append("\n"+str2);
                        writer.close();*/
//*************************
                        for (ArticleResult result : articles.getResults()) //loop over the event's corresponding articles
                        {
                            //System.out.println(result.getTitle());
                            //System.out.println(eventResponse.getEvents().getResults()[i].getTitle().getIta());
                            //System.out.println(result.getSource().toString());
                            //System.out.println()

                            if (result.getSource() != null && result.getSource().getLocation() != null && result.getSource().getLocation().getLabel() != null && result.getSource().getLocation().getLabel().getEng() != null && !result.getSource().getLocation().getLabel().getEng().isEmpty() && result.getSource().getLocation().getCountry()!=null && !result.getSource().getLocation().getLabel().getEng().isEmpty() && result.getSource().getLocation().getCountry().getLabel()!=null && !result.getSource().getLocation().getLabel().getEng().isEmpty() && result.getSource().getLocation().getCountry().getLabel().getEng()!=null && (result.getSource().getLocation().getLabel().getEng().equals("Italy") || (result.getSource().getLocation().getCountry()!=null && result.getSource().getLocation().getCountry().getLabel().getEng().equals("Italy"))) && result.getSource().getTitle() != null && eventResponse.getEvents() != null && result.getTitle().equals(eventResponse.getEvents().getResults()[i].getTitle().getIta())) { //if the article's source was from Italy and the title of the article is equal to the title of the aforementioned event
                                breakingEvents.add(eventResponse.getEvents().getResults()[i].getTitle().getIta()+". "+result.getBody()); //add the title to the list
                                System.out.println(eventResponse.getEvents().getResults()[i].getTitle().getIta()+". "+result.getBody());
                                writer.append(eventResponse.getEvents().getResults()[i].getTitle().getIta().trim().replaceAll("\n","")+". /ENDOFTITLE/. "+result.getBody().trim().replaceAll("(\n)+",". ")+" /URL/ "+result.getUrl()+" /SOURCE/ "+result.getSource().getTitle()+" /ENDOFARTICLE/"+"\n");
                                articleFoundFlag = true; //article was found
                            }
                            if (articleFoundFlag) {
                                articleFoundFlag = false;
                                break;
                            }

                        }

                    } //if concepts end

                    else if (language.equals("fra") && eventResponse.getEvents().getResults()[i].getEventDate().equals(date) && (eventResponse.getEvents().getResults()[i].getConcepts()[j].getLabel().getEng().contains("Islam") || eventResponse.getEvents().getResults()[i].getConcepts()[j].getLabel().getEng().contains("Muslim"))) { //if the found event's date matches the inputted one and its sentiment is negative and it's not related to trump and it is remotely related to Islam
                        conceptFoundFlag = true; //concept was found
                        log.info("entered index event: " + i + " entered index concept: " + j);
                        constructedGetArticleFromEventUrl = baseGetEventUrl + "apiKey=" + apiKey + "&eventUri=" + eventResponse.getEvents().getResults()[i].getUri() + "&resultType=articles&includeSourceLocation=true"; //construct the api url that will be responsible of getting the article from the event through the event's uri
                        log.info(constructedGetArticleFromEventUrl);
                        //System.out.println(eventResponse.getEvents().getResults()[i].getTitle().getIta());
                        System.out.println();
                        articleResponseStr = restTemplate.getForObject(constructedGetArticleFromEventUrl, String.class); //created the object articleResponse that now contains the returned json object because since the eventUri might be different, the json object returned will be different
                        rootJsonTree = parser.parse(articleResponseStr);
                        rootJsonObject = rootJsonTree.getAsJsonObject();
                        for (String key : rootJsonObject.keySet()) {
                            jsonElement = rootJsonObject.get(key);
                            articlesJsonObject = jsonElement.getAsJsonObject();
                            articles = gson.fromJson(articlesJsonObject.get("articles"), Articles.class);
                        }

                        //write file
                        //writer.write(str);
                        //writer.close();

                        /*String str2 = "World";
                        writer = new BufferedWriter(new FileWriter("/home/baalbaki/IdeaProjects/EventRelater/italianarticles.txt", true));
                        writer.append("\n"+str2);
                        writer.close();*/
//**************************
                        for (ArticleResult result : articles.getResults()) //loop over the event's corresponding articles
                        {
                            //System.out.println(result.getTitle());
                            //System.out.println(eventResponse.getEvents().getResults()[i].getTitle().getIta());
                            //System.out.println(result.getSource().toString());
                            //System.out.println();
                            if (result.getSource() != null && result.getSource().getLocation() != null && result.getSource().getLocation().getLabel() != null && result.getSource().getLocation().getLabel().getEng() != null && !result.getSource().getLocation().getLabel().getEng().isEmpty() && result.getSource().getLocation().getCountry()!=null && !result.getSource().getLocation().getLabel().getEng().isEmpty() && result.getSource().getLocation().getCountry().getLabel()!=null && !result.getSource().getLocation().getLabel().getEng().isEmpty() && result.getSource().getLocation().getCountry().getLabel().getEng()!=null && (result.getSource().getLocation().getLabel().getEng().equals("France") || (result.getSource().getLocation().getCountry()!=null && result.getSource().getLocation().getCountry().getLabel().getEng().equals("France"))) && result.getSource().getTitle() != null && eventResponse.getEvents() != null && result.getTitle().equals(eventResponse.getEvents().getResults()[i].getTitle().getFra())) { //if the article's source was from the United Kingdom and the title of the article is equal to the title of the aforementioned event
                                breakingEvents.add(eventResponse.getEvents().getResults()[i].getTitle().getFra()+". "+result.getBody()); //add the title to the list
                                System.out.println(eventResponse.getEvents().getResults()[i].getTitle().getFra()+". "+result.getBody());
                                writer.append(eventResponse.getEvents().getResults()[i].getTitle().getFra().trim().replaceAll("\n","")+". /ENDOFTITLE/. "+result.getBody().trim().replaceAll("(\n)+",". ")+" /URL/ "+result.getUrl()+" /SOURCE/ "+result.getSource().getTitle()+" /ENDOFARTICLE/"+"\n");
                                articleFoundFlag = true; //article was found
                            }
                            if (articleFoundFlag) {
                                articleFoundFlag = false;
                                break;
                            }

                        }

                    } //if concepts end
                    if (conceptFoundFlag) {
                        conceptFoundFlag = false;
                        break;
                    }
                }//concepts loop end
            }//events loop end

            if (language.equals("ita") || language.equals("fra"))
                writer.close();

            if (language.equals("eng")) {
                if (tempSize == 0) {
                    System.out.println("UK newspapers did not report any relevant breaking events that might have sparked Islamophobic hashtags on this day.");
                    System.exit(0);
                } else if (tempSize == 1) {
                    System.out.println("UK newspapers reported only 1 relevant breaking event that might have sparked Islamophobic hashtags on this day: ");
                    System.out.println();
                    for (Map.Entry<Integer, LinkedHashMap<Double, String>> indexAndInfo : breakingEventsInfo.entrySet()) {
                        LinkedHashMap<Double, String> fullInfo = indexAndInfo.getValue();
                        for (Map.Entry<Double, String> sentimentAndInfo : fullInfo.entrySet()) {
                            System.out.println(sentimentAndInfo.getKey() + ": " + sentimentAndInfo.getValue());
                        }
                    }
                    System.exit(0);
                } else if (tempSize >= 2) {

                    for (int i = 0; i < tempSize; i++) //we want to print only the 3 most breaking ones
                    {
                        Iterator<Map.Entry<Integer, LinkedHashMap<Double, String>>> mainIterator = breakingEventsInfo.entrySet().iterator();
                        while (mainIterator.hasNext()) {
                            Map.Entry<Integer, LinkedHashMap<Double, String>> mainEntry = mainIterator.next();
                            LinkedHashMap<Double, String> insideHashmap = mainEntry.getValue();
                            Map.Entry<Double, String> min = Collections.min(insideHashmap.entrySet(),
                                    Comparator.comparing(Map.Entry::getKey));
                            minimumSentiment = min.getKey();
                            articleInfo = min.getValue();
                            breakingEvents.add(minimumSentiment + ": " + articleInfo);
                            mainIterator.remove();
                        }

                    }
                }
            }//loop and calculate minimum again in the remaining list

            breakingEvents.sort(null);
            Collections.reverse(breakingEvents);

            if (tempSize == 2) {
                System.out.println();
                System.out.println("UK newspapers reported 2 relevant breaking events that might have sparked Islamophobic hashtags on this day: ");
                System.out.println();
                for (int i = 0; i < tempSize; i++) {
                    System.out.println(breakingEvents.get(i)); //print the corresponding events
                }
                System.exit(0);
            } else if (tempSize >= 3) {
                tempSize = 3;
                System.out.println();
                System.out.println("UK newspapers reported 3 relevant breaking events that might have sparked Islamophobic hashtags on this day: ");
                System.out.println();
                for (int i = 0; i < tempSize; i++) {
                    System.out.println(breakingEvents.get(i)); //print the corresponding events
                }
                System.exit(0);
            } else if (language.equals("ita") || language.equals("fra")) {
                System.exit(0);
            }

        };
    }
}