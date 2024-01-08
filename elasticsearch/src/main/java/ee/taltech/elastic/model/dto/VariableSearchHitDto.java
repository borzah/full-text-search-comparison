package ee.taltech.elastic.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VariableSearchHitDto {

    private String id;
    private String logicalRecordId;
    private String label;
    private String name;
    private String representationType;
}
