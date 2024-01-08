package ee.taltech.elastic.model.index.nested;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.metadata.MetadataCategory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
public class CategoryNested {

    @Field(type = FieldType.Text, index = false)
    private String label;

    @Field(type = FieldType.Text, index = false)
    private String description;

    @Field(type = FieldType.Keyword, index = false)
    private String codeValue;

    public static CategoryNested createCategoryNested(MetadataCategory metadataCategory) {
        CategoryNested categoryNested = new CategoryNested();
        categoryNested.setLabel(metadataCategory.label());
        categoryNested.setDescription(metadataCategory.description());
        categoryNested.setCodeValue(metadataCategory.codeValue());
        return categoryNested;
    }
}
