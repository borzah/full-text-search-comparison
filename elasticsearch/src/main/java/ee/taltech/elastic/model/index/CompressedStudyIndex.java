package ee.taltech.elastic.model.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.index.nested.CompressedVariableNested;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Document(indexName = "compressed_study")
@Setting(settingPath = "/elastic_settings.json")
@Getter
@Setter
@AllArgsConstructor
@JsonInclude(NON_NULL)
public class CompressedStudyIndex {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Keyword)
    private String domainId;

    @Field(type = FieldType.Keyword)
    private String subdomainId;

    @Field(type = FieldType.Keyword)
    private String seriesId;

    @Field(type = FieldType.Text, index = false)
    private String seriesTitle;

    @Field(type = FieldType.Text)
    private String universeLabel;

    @Field(type = FieldType.Text, index = false)
    private String title;

    @Field(type = FieldType.Text, index = false)
    private String purpose;

    @Field(type = FieldType.Text, index = false)
    private String referenceArea;

    @Field(type = FieldType.Text, index = false)
    private String timeCoverage;

    @Field(type = FieldType.Text)
    private String studySearchDocument;

    @Field(type = FieldType.Nested)
    private List<CompressedVariableNested> variables;
}
