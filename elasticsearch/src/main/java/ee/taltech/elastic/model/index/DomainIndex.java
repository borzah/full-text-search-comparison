package ee.taltech.elastic.model.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.index.nested.SubdomainNested;
import ee.taltech.elastic.model.metadata.MetadataDomain;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Document(indexName = "domain")
@Setting(settingPath = "/elastic_settings.json")
@Getter
@Setter
@JsonInclude(NON_NULL)
public class DomainIndex {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Text, index = false)
    private String label;

    @Field(type = FieldType.Nested)
    private List<SubdomainNested> subdomains;


    public static DomainIndex createDomainIndex(MetadataDomain domain) {
        DomainIndex domainIndex = new DomainIndex();
        domainIndex.setId(domain.id());
        domainIndex.setLabel(domain.label());
        domainIndex.setSubdomains(domain.subdomains().stream().map(SubdomainNested::createSubdomainNested).toList());
        return domainIndex;
    }
}
