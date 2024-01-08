package ee.taltech.elastic.model.metadata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MetadataSubject(String name) {

    @JsonCreator
    public MetadataSubject(@JsonProperty("name") String name) {
        this.name = name;
    }
}
