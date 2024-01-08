package ee.taltech.elastic.validator;

import ee.taltech.elastic.exception.BadRequestException;
import ee.taltech.elastic.model.query.SearchQueryParams;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class SearchValidator {

    private static final Validator validator;

    static {
        try (ValidatorFactory validatorFactory = buildDefaultValidatorFactory()) {
            validator = validatorFactory.getValidator();
        }
    }

    public static void validateSearchParams(SearchQueryParams searchQueryParams) {
        List<ConstraintViolation<SearchQueryParams>> errors = new ArrayList<>(validator.validate(searchQueryParams));
        Integer beginDate = searchQueryParams.getBeginDate();
        Integer endDate = searchQueryParams.getEndDate();

        if (errors.size() > 0) {
            throw new BadRequestException("Invalid search parameters", "invalid.search.parameters");
        }
        if (beginDate != null && endDate != null && beginDate > endDate) {
            throw new BadRequestException("Invalid search parameters", "invalid.search.parameters");
        }
    }
}
