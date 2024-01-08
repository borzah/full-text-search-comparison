package ee.taltech.fulltextsearchcomparison.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CodeListModel {

    private UUID id;

    private String label;

    private String description;

}
