package eu.fbk.dh.EventRelater.sentimentanalyzers;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;
import java.util.ArrayList;

public abstract class SentimentAnalyzer {
    public abstract void loadLexicon() throws IOException, InvalidFormatException; //load the negative for french and the negative and positive words for italian from lexicons

    public abstract <T> T getSentiments(ArrayList<String> articlesText);

}
