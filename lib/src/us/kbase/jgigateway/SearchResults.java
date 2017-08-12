
package us.kbase.jgigateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * <p>Original spec-file type: SearchResults</p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "doc_data"
})
public class SearchResults {

    @JsonProperty("doc_data")
    private List<Map<String, String>> docData;
    private Map<java.lang.String, Object> additionalProperties = new HashMap<java.lang.String, Object>();

    @JsonProperty("doc_data")
    public List<Map<String, String>> getDocData() {
        return docData;
    }

    @JsonProperty("doc_data")
    public void setDocData(List<Map<String, String>> docData) {
        this.docData = docData;
    }

    public SearchResults withDocData(List<Map<String, String>> docData) {
        this.docData = docData;
        return this;
    }

    @JsonAnyGetter
    public Map<java.lang.String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(java.lang.String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public java.lang.String toString() {
        return ((((("SearchResults"+" [docData=")+ docData)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
