package ee.taltech.fulltextsearchcomparison.service;

import ee.taltech.fulltextsearchcomparison.dto.StudySearchHitDto;
import ee.taltech.fulltextsearchcomparison.dto.SearchPage;
import ee.taltech.fulltextsearchcomparison.dto.SearchQuery;
import ee.taltech.fulltextsearchcomparison.dto.VariableSearchHitDto;
import ee.taltech.fulltextsearchcomparison.mapper.StudySearchRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ee.taltech.fulltextsearchcomparison.dto.SearchPage.convertPage;
import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class StudySearchRepository {

    private static final String SELECT = """
            WITH variables_match AS (
                SELECT variable_id AS variable_id,
                       study_id AS study_id,
                       logical_record_id AS logical_record_id,
                       ts_headline(label, websearch_to_tsquery(:search)) AS variable_label,
                       ts_headline(name, websearch_to_tsquery(:search)) AS variable_name,
                       representation_type AS representation_type,
                       ts_rank(variable_search_document, websearch_to_tsquery(:search)) AS variable_rank
                FROM variable_search_document_store
                WHERE variable_search_document @@ websearch_to_tsquery(:search)
                ),
                variables_selected AS (
                    SELECT variable_id,
                           study_id,
                           logical_record_id,
                           variable_label,
                           variable_name,
                           representation_type,
                           variable_rank
                    FROM variables_match
                    ORDER BY variable_rank DESC
                )
            SELECT                        study_id as study_id,
                                          series_title AS series_title,
                                          universe_label AS universe_label,
                                          title AS title,
                                          purpose AS purpose,
                                          reference_area AS reference_area,
                                          time_coverage AS time_coverage,
                                          ts_rank(study_search_document, websearch_to_tsquery(:search)) AS study_rank,
                                          json_agg(jsonb_build_object(
                                                  'id', variable_id,
                                                  'logicalRecordId', logical_record_id,
                                                  'label', variable_label,
                                                  'name', variable_name,
                                                  'representationType', representation_type,
                                                  'variableRank', variable_rank) ORDER BY variable_rank ) FILTER ( WHERE variable_rank > 0 ) AS variables
            FROM study_search_document_store LEFT JOIN variables_selected USING (study_id)
            WHERE study_search_document @@ websearch_to_tsquery(:search)
            GROUP BY study_id, series_title, universe_label, title, purpose, reference_area, time_coverage, ts_rank(study_search_document, websearch_to_tsquery(:search))
            ORDER BY study_rank DESC
            OFFSET :offset
            FETCH NEXT :limit ROWS ONLY;
            """;

    private static final String COUNT = """
            SELECT count(*) FROM study_search_document_store
            WHERE study_search_document @@ websearch_to_tsquery(:search);
            """;

    private final NamedParameterJdbcTemplate npJdbcTemplate;
    private final StudySearchRowMapper studySearchRowMapper;

    public SearchPage<StudySearchHitDto> searchStudies(SearchQuery searchQuery) {
        Map<String, Object> params = getQueryParams(searchQuery);
        List<StudySearchHitDto> studySearchResult = npJdbcTemplate.query(SELECT, params, studySearchRowMapper);
        Integer count = npJdbcTemplate.queryForObject(COUNT, params, Integer.class);
        List<StudySearchHitDto> result = studySearchResult.stream().map(sr -> new StudySearchHitDto(sr, getVariablesSublist(sr))).toList();
        return convertPage(result, searchQuery.getPage(), searchQuery.getSize(), count);
    }

    private static List<VariableSearchHitDto> getVariablesSublist(StudySearchHitDto sh) {
        List<VariableSearchHitDto> variableSearchHits = sh.getVariables();
        if (variableSearchHits != null) {
            return variableSearchHits.subList(0, Math.min(variableSearchHits.size(), 10));
        }
        return List.of();
    }

    private Map<String, Object> getQueryParams(SearchQuery searchQuery) {
        Map<String, Object> params = new HashMap<>();
        if (hasText(searchQuery.getSearchValue())) {
            params.put("search", searchQuery.getSearchValue());
        }
        if (searchQuery.getPage() != null && searchQuery.getSize() != null) {
            params.put("offset", searchQuery.getPage() * searchQuery.getSize());
            params.put("limit", searchQuery.getSize());
        }
        return params;
    }
}
