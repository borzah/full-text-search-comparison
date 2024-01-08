package ee.taltech.elastic.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SearchPage<T> {

    private long currentPage;
    private long totalPages;
    private long totalItems;
    private long pageSize;
    private List<T> items;

    public SearchPage(List<T> items, Pageable pageable, long totalHits) {
        this.currentPage = pageable.getPageNumber() + 1;
        this.pageSize = pageable.getPageSize();
        this.totalItems = totalHits;
        this.totalPages = this.totalItems / this.pageSize + 1;
        this.items = items;
    }
}
