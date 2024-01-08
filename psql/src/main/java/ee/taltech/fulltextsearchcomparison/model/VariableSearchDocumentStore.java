package ee.taltech.fulltextsearchcomparison.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class VariableSearchDocumentStore {

    private UUID variableId;
    private UUID studyId;
    private UUID logicalRecordId;
    private String name;
    private String label;
    private String representationType;
    private String variableSearchDocument;
}
