package ee.taltech.elastic.model.index.nested;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.metadata.MetadataDataFile;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
public class DataFileNested {

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Nested)
    private List<KeywordNested> keywords;

    @Field(type = FieldType.Nested)
    private List<SubjectNested> subjects;

    @Field(type = FieldType.Object)
    private TemporalCoverageNested temporalCoverage;

    @Field(type = FieldType.Nested)
    private List<LogicalRecordNested> logicalRecords;

    public static DataFileNested createDataFileNested(MetadataDataFile metadataDataFile) {
        DataFileNested dataFileNested = new DataFileNested();
        dataFileNested.setTitle(metadataDataFile.title());
        dataFileNested.setKeywords(metadataDataFile.keywords().stream().map(KeywordNested::createKeywordNested).toList());
        dataFileNested.setSubjects(metadataDataFile.subjects().stream().map(SubjectNested::createSubjectNested).toList());
        dataFileNested.setTemporalCoverage(TemporalCoverageNested.createTemporalCoverageNested(metadataDataFile.temporalCoverage()));
        dataFileNested.setLogicalRecords(metadataDataFile.logicalRecords().stream().map(LogicalRecordNested::createLogicalRecordNested).toList());
        return dataFileNested;
    }
}
