package ee.taltech.fulltextsearchcomparison.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ConceptModel {

    private UUID id;

    private UUID variableId;

    private String label;

}
