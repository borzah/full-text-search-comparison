package ee.taltech.fulltextsearchcomparison.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class VariableSearchHitDto {

    private UUID id;
    private UUID logicalRecordId;
    private String label;
    private String name;
    private String representationType;
}
