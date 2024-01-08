package ee.taltech.elastic.model.metadata;

import java.time.LocalDate;

public record MetadataTemporalCoverage(
        LocalDate beginDate,
        LocalDate endDate
) { }
