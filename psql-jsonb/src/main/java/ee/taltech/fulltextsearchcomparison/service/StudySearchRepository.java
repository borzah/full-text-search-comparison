package ee.taltech.fulltextsearchcomparison.service;

import ee.taltech.fulltextsearchcomparison.dto.SearchQuery;
import ee.taltech.fulltextsearchcomparison.dto.SearchResult;
import ee.taltech.fulltextsearchcomparison.dto.StudySearchHitDto;
import ee.taltech.fulltextsearchcomparison.dto.StudySearchRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class StudySearchRepository {

    private static final String SELECT = """
            SELECT DISTINCT studies->>'id' AS study_id,
                            studies->>'seriesTitle' AS series_title,
                            studies->>'universeLabel' AS universe_label,
                            studies->>'title' AS title,
                            studies->>'purpose' AS purpose,
                            studies->>'referenceArea' AS reference_area,
                            studies->>'timeCoverage' AS time_coverage
                    FROM domain_json,
                         jsonb_array_elements(data->'domains') AS domains,
                         jsonb_array_elements(domains->'subdomains') AS subdomains,
                         jsonb_array_elements(subdomains->'series') AS series,
                         jsonb_array_elements(series->'studies') AS studies,
                         jsonb_array_elements(studies->'dataFiles') AS dataFiles,
                         jsonb_array_elements(dataFiles->'subjects') AS subjects,
                         jsonb_array_elements(dataFiles->'keywords') AS keywords,
                         jsonb_array_elements(dataFiles->'logicalRecords') AS logicalRecords,
                         jsonb_array_elements(logicalRecords->'variables') AS variables,
                         jsonb_array_elements(variables->'concepts') AS concepts
                    WHERE
                          studies->>'seriesTitle' ILIKE :search
                       OR studies->>'universeLabel' ILIKE :search
                       OR studies->>'title' ILIKE :search
                       OR studies->>'summary' ILIKE :search
                       OR studies->>'purpose' ILIKE :search
                       OR studies->>'studyCode' ILIKE :search
                       OR studies->>'sectorCoverage' ILIKE :search
                       OR studies->>'otherDissemination' ILIKE :search
                       OR studies->>'documentationOnMethodology' ILIKE :search
                       OR studies->>'geographicalComparability' ILIKE :search
                       OR studies->>'comparabilityOverTime' ILIKE :search
                       OR studies->>'sourceData' ILIKE :search
                       OR studies->>'frequencyOfDataCollection' ILIKE :search
                       OR studies->>'dataCollection' ILIKE :search
                       OR studies->>'dataValidation' ILIKE :search
                       OR studies->>'dataCompilation' ILIKE :search
                       OR dataFiles->>'title' ILIKE :search
                       OR keywords->>'name' ILIKE :search
                       OR subjects->>'name' ILIKE :search
                       OR logicalRecords->>'name' ILIKE :search
                       OR logicalRecords->>'label' ILIKE :search
                       OR logicalRecords->>'description' ILIKE :search
                       OR variables->>'name' ILIKE :search
                       OR variables->>'label' ILIKE :search
                       OR variables->>'description' ILIKE :search
                       OR variables->>'representedVariableLabel' ILIKE :search
                       OR variables->>'conceptualVariableLabel' ILIKE :search
                       OR concepts->>'label' ILIKE :search;
            """;


    private final NamedParameterJdbcTemplate npJdbcTemplate;
    private final StudySearchRowMapper studySearchRowMapper;

    public SearchResult searchStudies(SearchQuery searchQuery) {
        Map<String, Object> params = getQueryParams(searchQuery);
        List<StudySearchHitDto> result = npJdbcTemplate.query(SELECT, params, studySearchRowMapper);
        return new SearchResult(result.size(), result);
    }

    private Map<String, Object> getQueryParams(SearchQuery searchQuery) {
        Map<String, Object> params = new HashMap<>();
        if (hasText(searchQuery.getSearchValue())) {
            params.put("search", "%" + searchQuery.getSearchValue() + "%");
        }
        return params;
    }
}
