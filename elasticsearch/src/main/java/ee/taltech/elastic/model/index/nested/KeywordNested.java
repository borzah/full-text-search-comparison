package ee.taltech.elastic.model.index.nested;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.metadata.MetadataKeyword;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
public class KeywordNested {

    @Field(type = FieldType.Text)
    private String name;

    public static KeywordNested createKeywordNested(MetadataKeyword metadataKeyword) {
        KeywordNested keywordNested = new KeywordNested();
        keywordNested.setName(metadataKeyword.name());
        return keywordNested;
    }
}
