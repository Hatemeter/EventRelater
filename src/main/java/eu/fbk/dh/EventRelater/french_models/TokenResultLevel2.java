package eu.fbk.dh.EventRelater.french_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenResultLevel2 {
    @JsonProperty
    private TokenResultLevel3[] token_list;

    public TokenResultLevel3[] getToken_list() {
        return token_list;
    }

    public void setToken_list(TokenResultLevel3[] token_list) {
        this.token_list = token_list;
    }

    @Override
    public String toString() {
        return "TokenResultLevel2{" +
                "token_list=" + Arrays.toString(token_list) +
                '}';
    }
}
