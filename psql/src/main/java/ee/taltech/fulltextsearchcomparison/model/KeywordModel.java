package ee.taltech.fulltextsearchcomparison.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class KeywordModel {

    private UUID id;

    private UUID dataFileId;

    private String name;

}
