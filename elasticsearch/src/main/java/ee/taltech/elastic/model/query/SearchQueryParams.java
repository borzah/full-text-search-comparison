package ee.taltech.elastic.model.query;

import ee.taltech.elastic.validator.SearchValidationConstraints;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchQueryParams {

    @Size(max = SearchValidationConstraints.MAX_SEARCH_VALUE_LENGTH)
    private String searchValue;
    @Size(max = SearchValidationConstraints.MAX_DOMAIN_FILTER_LENGTH)
    private String domainId;
    @Size(max = SearchValidationConstraints.MAX_DOMAIN_FILTER_LENGTH)
    private String subdomainId;
    @Size(max = SearchValidationConstraints.MAX_DOMAIN_FILTER_LENGTH)
    private String seriesId;
    @Min(SearchValidationConstraints.MIN_TEMPORAL_COVERAGE_YEAR)
    @Max(SearchValidationConstraints.MAX_TEMPORAL_COVERAGE_YEAR)
    private Integer beginDate;
    @Min(SearchValidationConstraints.MIN_TEMPORAL_COVERAGE_YEAR)
    @Max(SearchValidationConstraints.MAX_TEMPORAL_COVERAGE_YEAR)
    private Integer endDate;
    @PositiveOrZero
    @Max(SearchValidationConstraints.MAX_CURRENT_PAGE_VALUE)
    private Integer page = 0;
}
