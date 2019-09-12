package eu.fbk.dh.EventRelater.sentimentanalyzers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonSyntaxException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import com.meaningcloud.*;

public class FrenchAnalyzer extends SentimentAnalyzer {
   // private static final Logger log = LoggerFactory.getLogger(FrenchAnalyzer.class); //create a logger file to be able to write logs
    private List<List<String>> lexicons;
    private ArrayList<String> negativeWordsFromLexicon;
    private String frenchLemmasApikey;
    private String frenchLemmasBaseUrl;
    private RestTemplateBuilder builder;
    private RestTemplate restTemplate;

    public FrenchAnalyzer(String frenchLemmasApikey, String frenchLemmasBaseUrl) {
        negativeWordsFromLexicon = new ArrayList<String>();
        lexicons = new ArrayList<List<String>>();
        this.frenchLemmasApikey = frenchLemmasApikey;
        this.frenchLemmasBaseUrl = frenchLemmasBaseUrl;
        builder = new RestTemplateBuilder();
        restTemplate = builder.build();
        try {
            loadLexicon();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadLexicon() throws IOException {
        //Todo: put the resources in event relater instead of frenchanalyzer
        BufferedReader br = new BufferedReader(new InputStreamReader(FrenchAnalyzer.class.getResourceAsStream("/frenchlexicon.csv")));
        String line;
        int counter = 0; //lexicon counter and flag at the same time

        while ((line = br.readLine()) != null) {
            if (counter != 0) { //if it's the first time skip it because I don't want the titles
                String[] values = line.split(";"); //split each line based on the ;
                lexicons.add(Arrays.asList(values)); //add the array to each line of the list and transform it to an arraylist of values
            }
            counter++;
        }
        for (int i = 0; i < counter - 1; i++) {
            if (lexicons.get(i).get(2).equals("negative")) negativeWordsFromLexicon.add(lexicons.get(i).get(1));
        }
        //the lexicon in french will contain only negative words
       // log.info("Testing: " + "French lexicon loaded");
    }

    public <T> T getSentiments(ArrayList<String> articlesText) { //YOU ARE HERE
        ArrayList<Integer> negativeWordsScores = new ArrayList<Integer>();
        ArrayList<Integer> positiveWordsScores = new ArrayList<Integer>();
        ArrayList<Integer> sentiments = new ArrayList<Integer>();
        Request.Language language = Request.Language.valueOf("fr".toUpperCase());

        try {
            for (int i = 0; i < articlesText.size(); i++) {
                negativeWordsScores.add(0);
                positiveWordsScores.add(0);
            }

            for (int i = 0; i < articlesText.size(); i++) { //loop over number of articles
           //     log.info("Entered article: " + i);
                ParserResponse r = null;
                try {
                    r = ParserRequest
                            .build(frenchLemmasApikey, language)
                            .withText(articlesText.get(i).trim())
                            .send();
                } catch (JsonSyntaxException | NumberFormatException e) {
                    //don't want the "expected an int error to print"
                }

                try {
                    List<ParserResponse.Lemma> lemmas = r.lemmatize();
                    for (ParserResponse.Lemma lemma : lemmas) {
                        // System.out.println("Lemma:  " + lemma.getLemma());
                        //System.out.println("Tag:  " + lemma.getTag());
                        for (int z = 0; z < negativeWordsFromLexicon.size(); z++) {
                            if (negativeWordsFromLexicon.get(z).equals(lemma.getLemma())) {
                                negativeWordsScores.set(i, negativeWordsScores.get(i) + 1);
                                System.out.println("Negative: " + lemma.getLemma() + ": " + z);
                                break;
                            }
                        }
                    }
                }
                catch (NullPointerException e){
                    //don't want the null pointer exception to print
                }



                System.out.println(i + ": " + negativeWordsScores.get(i));
                sentiments.add(i, negativeWordsScores.get(i)); //article index and its score
                /*HttpResponse<String> httpResponse = Unirest.post("https://api.meaningcloud.com/parser-2.0")
                        .header("content-type", "application/x-www-form-urlencoded")
                        .body("key=X&lang=fr&txt=Y")
                        .asString();*/

                //// String constructedUrl;
                //String[] words = articlesText.get(i).split("\\s+");

                //if(words.length<=50000) { //this is the limit of the api
                ////constructedUrl = frenchLemmasBaseUrl + "key=" + frenchLemmasApikey + "&lang=fr&txt=" + articlesText.get(i).trim();
                // }
                //else{
                    /*int sum=0;
                    int spaceCounter=0;
                    for(int k=0;k<words.length;k++) {
                        sum = sum + words[k].length()+spaceCounter;
                        spaceCounter++;
                    }
                    int lastIndex=sum;
                    constructedUrl = frenchLemmasBaseUrl + "key=" + frenchLemmasApikey + "&lang=fr&txt=" + articlesText.get(i).substring(0,lastIndex).trim();
                }*/
                //// System.out.println(constructedUrl);
                ////   Response response = restTemplate.getForObject(constructedUrl, Response.class);
//                Response response = null;
               /* System.out.println(response.toString());
                for (int j = 0; j < response.getToken_list().length; j++) { //loop over number of sentences
                    if (response.getToken_list()[j].getType().equals("sentence")) {
                        //log.info("Entered sentence: " + j);

                        try {
                            for (int k = 0; k < response.getToken_list()[j].getToken_list()[0].getToken_list().length; k++) { //loop over number of tokens in each sentence
                                //log.info("Entered token: " + k);
                                if (response.getToken_list()[j].getToken_list()[0].getToken_list()[k].getAnalysis_list() != null) {
                                    //System.out.println("Got lemma: "+response.getToken_list()[j].getToken_list()[0].getToken_list()[k].getAnalysis_list()[0].getLemma()); //prints the lemmas
                                    for (int z = 0; z < negativeWordsFromLexicon.size(); z++) {
                                        if (negativeWordsFromLexicon.get(z).equals(response.getToken_list()[j].getToken_list()[0].getToken_list()[k].getAnalysis_list()[0].getLemma())) {
                                            negativeWordsScores.set(i, negativeWordsScores.get(i) + 1);
                                            System.out.println("Negative: " + response.getToken_list()[j].getToken_list()[0].getToken_list()[k].getAnalysis_list()[0].getLemma()+": "+z);
                                        }
                                    }

                                }
                            }
                        } catch (Exception e) {
                            // ignored
                        }
                    }
                }*/

                //System.out.println("neg: "+negativeWordsScores.get(0));


//            Iterator<Map.Entry<String, Integer>> iterator = negativeFrenchArticlesAndScores.entrySet().iterator();
//            while (iterator.hasNext()) {
//                Map.Entry<String, Integer> entry = iterator.next();
//                if (entry.getValue() < 7) iterator.remove();
//            }
//
//            int temp = negativeFrenchArticlesAndScores.size();
//            int maximumNegativeScore = 0;
//            for (int i = 0; i < temp; i++) {
//
//                iterator = negativeFrenchArticlesAndScores.entrySet().iterator();
//                while (iterator.hasNext()) {
//                    Map.Entry<String, Integer> entry = iterator.next();
//                    Map.Entry<String, Integer> maxEntry = Collections.max(negativeFrenchArticlesAndScores.entrySet(), Comparator.comparing(Map.Entry::getValue));
//                    maximumNegativeScore = maxEntry.getValue(); //calculate the minimum sentiment
//                    finalNegativeArticles.add(StringUtils.substringBefore(maxEntry.getKey(), " /ENDOFTITLE/") + " - Negative Score: " + maximumNegativeScore + " / " + StringUtils.substringBetween(maxEntry.getKey(), "/URL/ ", " /SOURCE/") + " / Source: " + StringUtils.substringBetween(maxEntry.getKey(), "/SOURCE/ ", " /ENDOFARTICLE/")); //concatenate the title of the event with the minimum sentiment with the url with the source and add them to the list
//                    iterator.remove();
//                }
//
//            }
//
//            for (int i = 0; i < temp; i++) {
//                System.out.println(finalNegativeArticles.get(i));
//            }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (T) sentiments;

    }
}
