package ee.taltech.fulltextsearchcomparison.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.fulltextsearchcomparison.dto.StudySearchHitDto;
import ee.taltech.fulltextsearchcomparison.dto.VariableSearchHitDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StudySearchRowMapper implements RowMapper<StudySearchHitDto> {

    private static final TypeReference<List<VariableSearchHitDto>> VARIABLE_TYPE = new TypeReference<>(){};
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public StudySearchHitDto mapRow(ResultSet rs, int rowNum) {
        StudySearchHitDto dto = new StudySearchHitDto();
        dto.setStudyId(rs.getObject("study_id", UUID.class));
        dto.setSeriesTitle(rs.getString("series_title"));
        dto.setUniverseLabel(rs.getString("universe_label"));
        dto.setTitle(rs.getString("title"));
        dto.setPurpose(rs.getString("purpose"));
        dto.setReferenceArea(rs.getString("reference_area"));
        dto.setTimeCoverage(rs.getString("time_coverage"));
        String variables = rs.getString("variables");
        if (variables != null) {
            dto.setVariables(objectMapper.readValue(variables, VARIABLE_TYPE));
        }
        return dto;
    }
}
