package ee.taltech.fulltextsearchcomparison.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchPage<T> {

    private List<T> content;
    private Integer currentPage;
    private Integer totalPages;
    private Integer totalItems;
    private Integer pageSize;

    public static <V> SearchPage<V> convertPage(List<V> content, Integer page, Integer size, Integer totalElements) {
        SearchPage<V> searchPage = new SearchPage<>();
        searchPage.setContent(content);
        searchPage.setCurrentPage(page);
        searchPage.setPageSize(size);
        searchPage.setTotalItems(totalElements);
        searchPage.setTotalPages(getTotalPages(totalElements, size));
        return searchPage;
    }

    public static int getTotalPages(Integer count, Integer size) {
        return count % size == 0 ? (count / size) : (count / size + 1);
    }
}
