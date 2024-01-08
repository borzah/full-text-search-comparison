package ee.taltech.elastic.model.index.nested;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@AllArgsConstructor
public class CompressedVariableNested implements VariableNested {

    @Id
    @Field(type = FieldType.Keyword, index = false)
    private String id;

    @Field(type = FieldType.Keyword)
    private String unitTypeId;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String label;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword, index = false)
    private String representationType;

    @Field(type = FieldType.Text)
    private String representedVariableLabel;

    @Field(type = FieldType.Text)
    private String conceptualVariableLabel;

    @Field(type = FieldType.Keyword, index = false)
    private String logicalRecordId;
}
