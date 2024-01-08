package ee.taltech.elastic.model.index.nested;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.metadata.MetadataVariable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
public class StudyVariableNested implements VariableNested {

    @Id
    @Field(type = FieldType.Keyword, index = false)
    private String id;

    @Field(type = FieldType.Keyword)
    private String unitTypeId;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String label;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword, index = false)
    private String representationType;

    @Field(type = FieldType.Text)
    private String representedVariableLabel;

    @Field(type = FieldType.Text)
    private String conceptualVariableLabel;

    @Field(type = FieldType.Nested)
    private List<QualityIndicatorNested> qualityIndicators;

    @Field(type = FieldType.Nested)
    private List<ConceptNested> concepts;

    public static StudyVariableNested createStudyVariableNested(MetadataVariable metadataVariable) {
        StudyVariableNested studyVariableNested = new StudyVariableNested();
        studyVariableNested.setId(metadataVariable.id());
        studyVariableNested.setUnitTypeId(metadataVariable.unitTypeId());
        studyVariableNested.setName(metadataVariable.name());
        studyVariableNested.setLabel(metadataVariable.label());
        studyVariableNested.setDescription(metadataVariable.description());
        studyVariableNested.setRepresentationType(metadataVariable.representationType());
        studyVariableNested.setRepresentedVariableLabel(metadataVariable.representedVariableLabel());
        studyVariableNested.setConceptualVariableLabel(metadataVariable.conceptualVariableLabel());
        studyVariableNested.setConcepts(metadataVariable.concepts().stream().map(ConceptNested::createConceptNested).toList());
        studyVariableNested.setQualityIndicators(metadataVariable.qualityIndicators().stream().map(QualityIndicatorNested::createQualityIndicatorNested).toList());
        return studyVariableNested;
    }
}
