package ee.taltech.elastic.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import ee.taltech.elastic.model.index.DomainIndex;
import ee.taltech.elastic.model.index.nested.SeriesNested;
import ee.taltech.elastic.model.index.nested.SubdomainNested;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(NON_NULL)
public class DomainFiltersDto {

    private String id;
    private String label;
    private List<DomainFiltersDto> subList;

    public DomainFiltersDto(DomainIndex domainIndex) {
        this.id = domainIndex.getId();
        this.label = domainIndex.getLabel();
        this.subList = domainIndex.getSubdomains().stream().map(DomainFiltersDto::new).toList();
    }

    public DomainFiltersDto(SubdomainNested subdomainNested) {
        this.id = subdomainNested.getId();
        this.label = subdomainNested.getLabel();
        this.subList = subdomainNested.getSeries().stream().map(DomainFiltersDto::new).toList();
    }

    public DomainFiltersDto(SeriesNested seriesNested) {
        this.id = seriesNested.getId();
        this.label = seriesNested.getTitle();
    }
}
