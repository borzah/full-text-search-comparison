package ee.taltech.fulltextsearchcomparison.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class QualityIndicatorModel {

    private UUID id;

    private UUID logicalRecordId;

    private UUID variableId;

    private String name;

    private String label;

}
