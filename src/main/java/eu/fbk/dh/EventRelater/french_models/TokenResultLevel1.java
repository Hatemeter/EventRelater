package eu.fbk.dh.EventRelater.french_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenResultLevel1 {
    @JsonProperty
    private TokenResultLevel2[] token_list;
    @JsonProperty
    private String type;

    public TokenResultLevel2[] getToken_list() {
        return token_list;
    }

    public void setToken_list(TokenResultLevel2[] token_list) {
        this.token_list = token_list;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "TokenResultLevel1{" +
                "token_list=" + Arrays.toString(token_list) +
                ", type='" + type + '\'' +
                '}';
    }
}
