package ee.taltech.fulltextsearchcomparison.metadata;

import java.time.LocalDate;

public record MetadataTemporalCoverage(
        LocalDate beginDate,
        LocalDate endDate
) { }
