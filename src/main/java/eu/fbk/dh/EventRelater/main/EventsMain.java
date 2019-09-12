package eu.fbk.dh.EventRelater.main;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import eu.fbk.dh.EventRelater.api.EventRelater;
import eu.fbk.dh.ThreadCrawler;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.FileReader;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class EventsMain {
    private static int counterApi = 0;
    private static String lang = null;

    public static void main(String[] args) {
        //TODO Change URI of italian
        //before db check: spends 451 tokens
        //after db check: spends 262 tokens
        //crawler took 4 hours
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = EventsMain.class.getClassLoader().getResourceAsStream("config.properties");
            prop.load(input);
            String mysqlUser = prop.getProperty("mysqlUser");
            String mysqlPassword = prop.getProperty("mysqlPassword");
            String connection = prop.getProperty("connection");
            //String lang = null;
            ConcurrentHashMap<String, Set<Long>> ht_idUnique = new ConcurrentHashMap<>();

            ConcurrentHashMap<String, TreeMap<String, AtomicInteger>> ht_daysCounter = new ConcurrentHashMap<>();
            String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
            SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
            SimpleDateFormat sfOut = new SimpleDateFormat("yyyy-MM-dd");

            int counterLang=0;

            int cores = Runtime.getRuntime().availableProcessors();
            if (cores > 4) {
                cores = cores / 2;
            }

            for (int k = 0; k < 3; k++) {

                ArrayList<String> hashtagsWhoseDataWasReplaced=new ArrayList<>();

                Connection dbconn = null;
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    dbconn = DriverManager.getConnection(connection, mysqlUser, mysqlPassword);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for (int i = counterLang; i < args.length; i++) {
                    try {
                        if (i <= 40) lang = "en";
                        else if (i >= 41 && i <= 58) lang = "fr";
                        else if (i >= 59 && i <= 82) lang = "it";

                        Gson gson = new GsonBuilder().create();
                        //Path inputFile = Paths.get(args[i]);
                        String ht = args[i];
                        System.out.println(ht);
                        ThreadCrawler crawler=new ThreadCrawler( ht, "", null);
                        //inputFile.getFileName().toString().split("-")[2].replace(".json", "");
                        ExecutorService executor = Executors.newFixedThreadPool(cores);

                        executor.execute(crawler);
                        executor.shutdown();
                        try {
                            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        String filePath=crawler.getFilePath();
                        System.out.println(filePath);


                        ht_idUnique.putIfAbsent(ht, new HashSet<>());


                        JsonReader reader = new JsonReader(new FileReader(filePath)); //to read the json file
                        reader.beginArray();

                        while (reader.hasNext()) {
                            JsonObject tweet = gson.fromJson(reader, JsonObject.class);
                            long id = tweet.get("id").getAsLong();
                            if (tweet.get("lang").getAsString().equals(lang)) {
                                //System.out.println(tweet.get("created_at").toString());
                                //System.out.println(calGood.toString());
                                if (!ht_idUnique.get(ht).contains(id)) {
                                    Date creation_date = sf.parse(tweet.get("created_at").getAsString());
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(creation_date);


                                    ht_daysCounter.putIfAbsent(ht, new TreeMap<>());
                                    ht_daysCounter.get(ht).putIfAbsent(sfOut.format(cal.getTime()), new AtomicInteger(0));
                                    ht_daysCounter.get(ht).get(sfOut.format(cal.getTime())).incrementAndGet();
                                    ht_idUnique.get(ht).add(id);
                                } else {
                                    //System.out.print("Duplicate for id:" + id + "...");
                                }
                            }
                        }
                        reader.endArray();
                        reader.close();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    counterLang++;
                    if(i==24 || i==42) break;
                }

                Connection finalDbconn = dbconn;
                ht_daysCounter.forEach((s, calendarAtomicIntegerTreeMap) -> {
                    JsonObject htJson = new JsonObject();


                    DescriptiveStatistics stats = new DescriptiveStatistics();

                    JsonArray data = new JsonArray();
                    JsonArray labels = new JsonArray();
                    JsonArray events = new JsonArray();

                    calendarAtomicIntegerTreeMap.forEach((calendar, atomicInteger) -> {
                        stats.addValue(Double.parseDouble(atomicInteger.toString()));
                        data.add(Double.parseDouble(atomicInteger.toString()));
                        labels.add(calendar);
                    });


                    Double threshold = stats.getMean() + stats.getStandardDeviation();

                    htJson.add("data", data);
                    htJson.add("labels", labels);
                    htJson.addProperty("threshold", threshold);
                    htJson.add("events", events);

                    try {
                        JsonParser parser = new JsonParser();
                        JsonElement rootJsonTree = parser.parse(htJson.toString()); //parsing the json string
                        JsonObject rootJsonObject = rootJsonTree.getAsJsonObject();
                        JsonElement thresholdAsElement = rootJsonObject.get("threshold");
                        double thresholdAsDouble = thresholdAsElement.getAsDouble(); //get the threshold

                        JsonElement datesAsElement = rootJsonObject.get("labels");
                        JsonArray dates = datesAsElement.getAsJsonArray(); //get the dates
                        JsonElement peaksAsElement = rootJsonObject.get("data");
                        JsonArray peaks = peaksAsElement.getAsJsonArray(); //get the peaks
                        System.out.println();
                        System.out.println(peaks.toString() + ", Threshold: " + thresholdAsDouble);
                        JsonArray finalEventsAndSentiments = new JsonArray();
                        String dbDate = null, dbEvents = null, getDatesQuery;
                        for (int i = 0; i < dates.size(); i++) {
                            if (peaks.get(i).getAsDouble() >= thresholdAsDouble) {

                                Statement statement = null;
                                ResultSet resultSet = null;
                                try {
                                    getDatesQuery = "SELECT date,events from " + lang + "_already_seen_events";
                                    statement = finalDbconn.createStatement();
                                    resultSet = statement.executeQuery(getDatesQuery);
                                    //statement.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                while (resultSet.next()) {
                                    dbDate = resultSet.getString("date");
                                    dbEvents = resultSet.getString("events");
                                    if (dbDate.equals(dates.get(i).getAsString())) break; //found it
                                    else dbDate = null;
                                }

                                if (dbDate == null) { //if first time or not equal
                                    EventRelater eventRelater = null;
                                    if(lang.equals("en")) {
                                        eventRelater = new EventRelater("eng", dates.get(i).getAsString());
                                    }
                                    else if(lang.equals("fr")){
                                        eventRelater = new EventRelater("fra", dates.get(i).getAsString());
                                    }
                                    else if(lang.equals("it")){
                                        eventRelater = new EventRelater("ita", dates.get(i).getAsString());
                                    }
                                    finalEventsAndSentiments = eventRelater.getFinalEventsAndSentiments();
                                    if (finalEventsAndSentiments != null && finalEventsAndSentiments.size() != 0) {
                                        for (int z = 0; z < finalEventsAndSentiments.size(); z++) {
                                            System.out.println("Event: " + finalEventsAndSentiments.get(z).getAsJsonObject().get("event"));
                                        }
                                        events.add(new Gson().toJsonTree(finalEventsAndSentiments).getAsJsonArray());

                                        PreparedStatement pstmt = finalDbconn.prepareStatement("INSERT into " + lang + "_already_seen_events (date,events) VALUES (?,?);");
                                        pstmt.setString(1, dates.get(i).getAsString());
                                        pstmt.setString(2, finalEventsAndSentiments.toString());

                                        pstmt.execute();

                                    } else {
                                        JsonArray emptyArray=new JsonArray();
                                        JsonObject emptyObject=new JsonObject();
                                        emptyArray.add(emptyObject);
                                        events.add(emptyArray); //if the method returns no events
                                        PreparedStatement pstmt = finalDbconn.prepareStatement("INSERT into " + lang + "_already_seen_events (date,events) VALUES (?,?);");
                                        pstmt.setString(1, dates.get(i).getAsString());
                                        pstmt.setString(2, null);

                                        pstmt.execute();

                                    }

                                } else {
                                    System.out.println("DATE SEEN BEFORE WITH THESE EVENTS: " + dbEvents);
                                    counterApi++;
                                    System.out.println("Saved " + counterApi + " api calls");
                                    if (dbEvents != null) {

                                        JsonElement element = parser.parse(dbEvents);
                                        JsonArray tempArray = element.getAsJsonArray();

                                        events.add(new Gson().toJsonTree(tempArray).getAsJsonArray());
                                    } else {
                                        JsonArray emptyArray=new JsonArray();
                                        JsonObject emptyObject=new JsonObject();
                                        emptyArray.add(emptyObject);
                                        events.add(emptyArray); //if in db seen event is null
                                    }
                                }

                            } else {
                                JsonArray emptyArray=new JsonArray();
                                JsonObject emptyObject=new JsonObject();
                                emptyArray.add(emptyObject);
                                events.add(emptyArray); //if smaller than threshold
                            }
                        }

                        PreparedStatement pstmt = finalDbconn.prepareStatement("REPLACE INTO " + lang + "_alerts_trend VALUES (?,?)");
                        pstmt.setString(1, s);
                        pstmt.setString(2, htJson.toString());
                        System.out.println(pstmt.toString());
                        hashtagsWhoseDataWasReplaced.add(s);

                        pstmt.execute();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });

                System.out.println("Hashtags whose data was replaced: "+hashtagsWhoseDataWasReplaced.toString());

                int startinglangIndex=-1;
                int endingLangIndex=-1;

                if(lang.equals("en")) {
                    startinglangIndex=0;
                    endingLangIndex=40;
                }
                else if (lang.equals("fr")){
                    startinglangIndex=41;
                    endingLangIndex=58;
                }
                else if(lang.equals("it")){
                    startinglangIndex=59;
                    endingLangIndex=82;
                }

                for(int i=startinglangIndex; i<= endingLangIndex ; i++){
                    if(!hashtagsWhoseDataWasReplaced.contains(args[i])){
                        PreparedStatement pstmt = dbconn.prepareStatement("UPDATE "+lang+"_alerts_trend SET data = ? WHERE "+lang+"_alerts_trend.hashtag = ?");
                        pstmt.setString(1, "{\"data\":[0],\"labels\":[\"\"],\"threshold\":0.0,\"events\":[[{}]]}");
                        pstmt.setString(2, args[i]);
                        System.out.println(pstmt.toString());
                        pstmt.execute();
                    }
                }

                ht_daysCounter.clear(); //clear map for different languages
                hashtagsWhoseDataWasReplaced.clear();
                finalDbconn.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

