package ee.taltech.fulltextsearchcomparison.dto;

import java.util.List;

public record SearchResult(
        long total,
        List<StudySearchHitDto> result
) { }
