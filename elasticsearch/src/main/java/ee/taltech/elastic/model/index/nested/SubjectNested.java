package ee.taltech.elastic.model.index.nested;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.metadata.MetadataSubject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
public class SubjectNested {

    @Field(type = FieldType.Text)
    private String name;

    public static SubjectNested createSubjectNested(MetadataSubject metadataSubject) {
        SubjectNested subjectNested = new SubjectNested();
        subjectNested.setName(metadataSubject.name());
        return subjectNested;
    }
}
