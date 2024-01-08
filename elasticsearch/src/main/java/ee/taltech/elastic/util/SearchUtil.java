package ee.taltech.elastic.util;

import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class SearchUtil {

    private static final int PAGINATION_PAGE_SIZE = 10;
    private static final int INITIAL_PAGE = 0;

    public static Pageable getSearchQueryPageable(Integer page) {
        int currentPage = page == null || page <= 0 ? INITIAL_PAGE : page - 1;
        return PageRequest.of(currentPage, PAGINATION_PAGE_SIZE);
    }
}
