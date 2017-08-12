
package us.kbase.jgigatewayeap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import us.kbase.common.service.UObject;


/**
 * <p>Original spec-file type: SearchResults</p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "results",
    "search_elapsed_time"
})
public class SearchResults {

    @JsonProperty("results")
    private List<UObject> results;
    @JsonProperty("search_elapsed_time")
    private Long searchElapsedTime;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("results")
    public List<UObject> getResults() {
        return results;
    }

    @JsonProperty("results")
    public void setResults(List<UObject> results) {
        this.results = results;
    }

    public SearchResults withResults(List<UObject> results) {
        this.results = results;
        return this;
    }

    @JsonProperty("search_elapsed_time")
    public Long getSearchElapsedTime() {
        return searchElapsedTime;
    }

    @JsonProperty("search_elapsed_time")
    public void setSearchElapsedTime(Long searchElapsedTime) {
        this.searchElapsedTime = searchElapsedTime;
    }

    public SearchResults withSearchElapsedTime(Long searchElapsedTime) {
        this.searchElapsedTime = searchElapsedTime;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return ((((((("SearchResults"+" [results=")+ results)+", searchElapsedTime=")+ searchElapsedTime)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
