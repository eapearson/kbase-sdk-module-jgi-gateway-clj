
package us.kbase.jgigatewayeap;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * <p>Original spec-file type: DebugResults</p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "config"
})
public class DebugResults {

    @JsonProperty("config")
    private String config;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("config")
    public String getConfig() {
        return config;
    }

    @JsonProperty("config")
    public void setConfig(String config) {
        this.config = config;
    }

    public DebugResults withConfig(String config) {
        this.config = config;
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
        return ((((("DebugResults"+" [config=")+ config)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
