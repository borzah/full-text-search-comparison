package ee.taltech.elastic.repository;

import ee.taltech.elastic.model.index.CodeListIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CodeListRepository extends ElasticsearchRepository<CodeListIndex, String> {
}
