package ee.taltech.fulltextsearchcomparison.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CategoryModel {

    private UUID id;

    private UUID codeListId;

    private String label;

    private String description;

    private String codeValue;

}
