package ee.taltech.elastic.model.index.nested;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.metadata.MetadataTemporalCoverage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
public class TemporalCoverageNested {

    @Field(type = FieldType.Date)
    private LocalDate beginDate;

    @Field(type = FieldType.Date)
    private LocalDate endDate;

    public static TemporalCoverageNested createTemporalCoverageNested(MetadataTemporalCoverage metadataTemporalCoverage) {
        TemporalCoverageNested temporalCoverageNested = new TemporalCoverageNested();
        temporalCoverageNested.setBeginDate(metadataTemporalCoverage.beginDate());
        temporalCoverageNested.setEndDate(metadataTemporalCoverage.endDate());
        return temporalCoverageNested;
    }
}
