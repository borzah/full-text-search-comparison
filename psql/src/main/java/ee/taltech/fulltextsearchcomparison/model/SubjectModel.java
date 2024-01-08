package ee.taltech.fulltextsearchcomparison.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SubjectModel {

    private UUID id;

    private UUID dataFileId;

    private String name;

}
