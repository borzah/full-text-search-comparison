package ee.taltech.elastic.model.index.nested;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.metadata.MetadataSeries;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
public class SeriesNested {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Text, index = false)
    private String title;

    public static SeriesNested createSeriesNested(MetadataSeries metadataSeries) {
        SeriesNested seriesNested = new SeriesNested();
        seriesNested.setId(metadataSeries.id());
        seriesNested.setTitle(metadataSeries.title());
        return seriesNested;
    }
}
