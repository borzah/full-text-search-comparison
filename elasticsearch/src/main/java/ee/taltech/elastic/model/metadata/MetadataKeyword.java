package ee.taltech.elastic.model.metadata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MetadataKeyword(String name) {

    @JsonCreator
    public MetadataKeyword(@JsonProperty("name") String name) {
        this.name = name;
    }
}
