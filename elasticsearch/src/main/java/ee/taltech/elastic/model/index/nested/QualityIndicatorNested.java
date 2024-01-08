package ee.taltech.elastic.model.index.nested;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.metadata.MetadataQualityIndicator;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
public class QualityIndicatorNested {

    @Field(type = FieldType.Keyword, index = false)
    private String name;

    @Field(type = FieldType.Text, index = false)
    private String value;

    public static QualityIndicatorNested createQualityIndicatorNested(MetadataQualityIndicator metadataQualityIndicator) {
        QualityIndicatorNested qualityIndicatorNested = new QualityIndicatorNested();
        qualityIndicatorNested.setName(metadataQualityIndicator.name());
        qualityIndicatorNested.setValue(metadataQualityIndicator.label());
        return qualityIndicatorNested;
    }
}
