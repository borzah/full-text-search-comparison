package ee.taltech.elastic.model.index.nested;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.metadata.MetadataLogicalRecord;
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
public class LogicalRecordNested {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String label;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Long, index = false)
    private Long numberOfVariables;

    @Field(type = FieldType.Nested)
    private List<QualityIndicatorNested> qualityIndicators;

    @Field(type = FieldType.Nested)
    private List<StudyVariableNested> variables;

    public static LogicalRecordNested createLogicalRecordNested(MetadataLogicalRecord metadataLogicalRecord) {
        LogicalRecordNested logicalRecordNested = new LogicalRecordNested();
        logicalRecordNested.setId(metadataLogicalRecord.id());
        logicalRecordNested.setName(metadataLogicalRecord.name());
        logicalRecordNested.setLabel(metadataLogicalRecord.label());
        logicalRecordNested.setDescription(metadataLogicalRecord.description());
        logicalRecordNested.setNumberOfVariables((long) metadataLogicalRecord.variables().size());
        logicalRecordNested.setQualityIndicators(metadataLogicalRecord.qualityIndicators().stream().map(QualityIndicatorNested::createQualityIndicatorNested).toList());
        logicalRecordNested.setVariables(metadataLogicalRecord.variables().stream().map(StudyVariableNested::createStudyVariableNested).toList());
        return logicalRecordNested;
    }
}
