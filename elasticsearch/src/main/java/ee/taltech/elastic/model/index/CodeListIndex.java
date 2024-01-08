package ee.taltech.elastic.model.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.index.nested.CategoryNested;
import ee.taltech.elastic.model.metadata.MetadataCodeList;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Document(indexName = "codelist")
@Getter
@Setter
@JsonInclude(NON_NULL)
public class CodeListIndex {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Text, index = false)
    private String label;

    @Field(type = FieldType.Text, index = false)
    private String description;

    @Field(type = FieldType.Object)
    private List<CategoryNested> categories;

    public static CodeListIndex createCodeListIndex(MetadataCodeList metadataCodeList) {
        CodeListIndex codeListIndex = new CodeListIndex();
        codeListIndex.setId(metadataCodeList.id());
        codeListIndex.setLabel(metadataCodeList.label());
        codeListIndex.setDescription(metadataCodeList.description());
        codeListIndex.setCategories(metadataCodeList.categories().stream().map(CategoryNested::createCategoryNested).toList());
        return codeListIndex;
    }
}
