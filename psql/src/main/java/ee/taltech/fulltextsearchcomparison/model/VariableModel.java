package ee.taltech.fulltextsearchcomparison.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class VariableModel {
    private UUID id;

    private UUID studyId;

    private UUID logicalRecordId;

    private UUID codeListId;

    private String unitTypeId;

    private String unitTypeLabel;

    private String name;

    private String label;

    private String description;

    private String representationType;

    private String type;

    private Boolean variableIsAWeight;

    private Boolean blankvaluesRepresentMissingvalues;

    private String missingvalues;

    private String measurementUnit;

    private String variableRole;

    private String representedVariableLabel;

    private String conceptualVariableLabel;

    private Integer percentageOfFilledEntries;

}
