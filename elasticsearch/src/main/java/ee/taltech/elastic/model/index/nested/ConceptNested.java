package ee.taltech.elastic.model.index.nested;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.metadata.MetadataConcept;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
public class ConceptNested {

    @Field(type = FieldType.Text)
    private String label;

    public static ConceptNested createConceptNested(MetadataConcept metadataConcept) {
        ConceptNested conceptNested = new ConceptNested();
        conceptNested.setLabel(metadataConcept.label());
        return conceptNested;
    }
}
