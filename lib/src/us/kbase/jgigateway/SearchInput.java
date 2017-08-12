
package us.kbase.jgigateway;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * <p>Original spec-file type: SearchInput</p>
 * <pre>
 * search_jgi searches the JGI service for matches against the
 * search_string
 * Other parameters
 * @optional limit
 * @optional page
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "search_string",
    "limit",
    "page"
})
public class SearchInput {

    @JsonProperty("search_string")
    private String searchString;
    @JsonProperty("limit")
    private Long limit;
    @JsonProperty("page")
    private Long page;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("search_string")
    public String getSearchString() {
        return searchString;
    }

    @JsonProperty("search_string")
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public SearchInput withSearchString(String searchString) {
        this.searchString = searchString;
        return this;
    }

    @JsonProperty("limit")
    public Long getLimit() {
        return limit;
    }

    @JsonProperty("limit")
    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public SearchInput withLimit(Long limit) {
        this.limit = limit;
        return this;
    }

    @JsonProperty("page")
    public Long getPage() {
        return page;
    }

    @JsonProperty("page")
    public void setPage(Long page) {
        this.page = page;
    }

    public SearchInput withPage(Long page) {
        this.page = page;
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
        return ((((((((("SearchInput"+" [searchString=")+ searchString)+", limit=")+ limit)+", page=")+ page)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
