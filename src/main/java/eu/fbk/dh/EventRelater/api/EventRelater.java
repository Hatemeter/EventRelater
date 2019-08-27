package eu.fbk.dh.EventRelater.api;

import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * @author Mohamad Baalbaki
 */
public class EventRelater {
    private static final Logger log = LoggerFactory.getLogger(EventRelater.class);
    private static String language;
    private static String date;

    public EventRelater(String language, String date) {
        this.language=language;
        this.date=date;
    }

    public JsonArray getFinalEventsAndSentiments() throws IOException {
        LinkedHashMap<Integer, LinkedHashMap<Double, String>> breakingEventsInfo = null;

        Properties prop = new Properties();
        InputStream input = null;
        String monitoringPageUri = null;
        String baseEventsForTopicPageUrl = null;
        String apiKey = null;
        String baseGetEventUrl = null;
        String frenchLemmasApiKey = null;
        String frenchLemmasBaseUrl = null;
        try {
            input = getClass().getClassLoader().getResourceAsStream("apikeys.properties"); //load the file that has the aforementioned global variables
            prop.load(input);
            apiKey = prop.getProperty("apiKey"); //get the api key
            //For the events
            if (language.equals("eng")) {
                monitoringPageUri = prop.getProperty("englishMonitoringPageUri"); //get the english page uri
            } else if (language.equals("ita")) {
                monitoringPageUri = prop.getProperty("italianMonitoringPageUri"); //get the italian page uri
            } else if (language.equals("fra")) {
                monitoringPageUri = prop.getProperty("frenchMonitoringPageUri"); //get the french page uri
            }
            baseEventsForTopicPageUrl = prop.getProperty("baseEventsForTopicPageUrl"); //get the topic page url
            baseGetEventUrl = prop.getProperty("baseGetEventUrl");

            //For meaningcloud's french lemmatization api
            frenchLemmasApiKey = prop.getProperty("frenchLemmasApiKey");
            frenchLemmasBaseUrl = prop.getProperty("frenchLemmasBaseUrl");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //api handler class: construct url method
        ApiHandler apiHandler = new ApiHandler(apiKey, monitoringPageUri, baseEventsForTopicPageUrl, baseGetEventUrl, language, date, frenchLemmasApiKey, frenchLemmasBaseUrl);
        JsonArray finalEventsAndSentiments=new JsonArray();
        try {
            finalEventsAndSentiments = apiHandler.getArticlesAndSentiments();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        if (finalEventsAndSentiments != null) {
            JsonArray tempJsonArray=new JsonArray();
            if (finalEventsAndSentiments.size() >= 3) {
                for(int z=0;z<3;z++){
                    tempJsonArray.add(finalEventsAndSentiments.get(z).getAsJsonObject());
                }
                finalEventsAndSentiments=tempJsonArray;
            }

            /*for(int i=0;i<finalEventsAndSentiments.size();i++){
                System.out.println("MEN EVENT RELATER: "+finalEventsAndSentiments.get(i));
            }*/
        }

        return finalEventsAndSentiments;
    }

    public static void main(String[] args) throws IOException {
            /*EventRelater eventRelater=new EventRelater("fra","2019-07-31");
            ArrayList<String> x=new ArrayList<>();
            try {
                 x = eventRelater.getFinalEventsAndSentiments();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            for(int i=0;i<x.size();i++){
                System.out.println(x.get(i));
            }*/
    }

}