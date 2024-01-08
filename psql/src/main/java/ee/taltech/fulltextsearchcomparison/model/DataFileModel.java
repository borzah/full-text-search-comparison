package ee.taltech.fulltextsearchcomparison.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class DataFileModel {

    private UUID id;

    private UUID studyId;

    private String title;

    private LocalDate beginDate;

    private LocalDate endDate;

}
