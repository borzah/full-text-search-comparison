package ee.taltech.elastic.model.index.nested;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.metadata.MetadataOtherMaterial;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
public class OtherMaterialNested {

    @Field(type = FieldType.Text, index = false)
    private String title;

    @Field(type = FieldType.Keyword, index = false)
    private String url;

    public static OtherMaterialNested createOtherMaterialNested(MetadataOtherMaterial metadataOtherMaterial) {
        OtherMaterialNested otherMaterialNested = new OtherMaterialNested();
        otherMaterialNested.setTitle(metadataOtherMaterial.title());
        otherMaterialNested.setUrl(metadataOtherMaterial.url());
        return otherMaterialNested;
    }
}
