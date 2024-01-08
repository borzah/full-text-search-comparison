package ee.taltech.elastic.repository;

import ee.taltech.elastic.model.index.DomainIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DomainRepository extends ElasticsearchRepository<DomainIndex, String> {
}
