package ee.taltech.elastic.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class LogicalRecordSearchHitDto {

    private String id;
    private String label;
    private String studyReferenceArea;
    private String studyTimeCoverage;
    private String description;
    private String databaseUrl;
    private Long numberOfTextVariables;
    private Long numberOfCodeVariables;
    private Long numberOfNumericVariables;
    private Long numberOfDateVariables;
    private Long numberOfEntries;
    private List<VariableSearchHitDto> variables;
}
