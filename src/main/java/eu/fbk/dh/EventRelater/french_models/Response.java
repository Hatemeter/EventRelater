package eu.fbk.dh.EventRelater.french_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {
@JsonProperty
    private TokenResultLevel1[] token_list;

    public TokenResultLevel1[] getToken_list() {
        return token_list;
    }

    public void setToken_list(TokenResultLevel1[] token_list) {
        this.token_list = token_list;
    }

    @Override
    public String toString() {
        return "Response{" +
                "token_list=" + Arrays.toString(token_list) +
                '}';
    }
}
