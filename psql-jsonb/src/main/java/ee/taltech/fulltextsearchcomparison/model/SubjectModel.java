package ee.taltech.fulltextsearchcomparison.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubjectModel {

    private UUID id;

    private UUID dataFileId;

    private String data;
}
