package ee.taltech.fulltextsearchcomparison.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataFileModel {

    private UUID id;

    private UUID studyId;

    private String data;

}
