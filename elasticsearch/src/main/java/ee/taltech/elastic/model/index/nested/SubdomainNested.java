package ee.taltech.elastic.model.index.nested;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.metadata.MetadataSubdomain;
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
public class SubdomainNested {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Text, index = false)
    private String label;

    @Field(type = FieldType.Nested)
    private List<SeriesNested> series;

    public static SubdomainNested createSubdomainNested(MetadataSubdomain metadataSubdomain) {
        SubdomainNested subdomainNested = new SubdomainNested();
        subdomainNested.setId(metadataSubdomain.id());
        subdomainNested.setLabel(metadataSubdomain.label());
        subdomainNested.setSeries(metadataSubdomain.series().stream().map(SeriesNested::createSeriesNested).toList());
        return subdomainNested;
    }
}
