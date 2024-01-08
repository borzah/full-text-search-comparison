package ee.taltech.fulltextsearchcomparison.dto;

import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.UUID;

@Component
public class StudySearchRowMapper implements RowMapper<StudySearchHitDto> {

    @SneakyThrows
    @Override
    public StudySearchHitDto mapRow(ResultSet rs, int rowNum) {
        StudySearchHitDto dto = new StudySearchHitDto();
        dto.setStudyId(UUID.fromString(rs.getString("study_id")));
        dto.setSeriesTitle(rs.getString("series_title"));
        dto.setUniverseLabel(rs.getString("universe_label"));
        dto.setTitle(rs.getString("title"));
        dto.setPurpose(rs.getString("purpose"));
        dto.setReferenceArea(rs.getString("reference_area"));
        dto.setTimeCoverage(rs.getString("time_coverage"));
        return dto;
    }
}
