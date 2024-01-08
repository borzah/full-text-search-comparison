package ee.taltech.elastic.repository;

import ee.taltech.elastic.model.index.CompressedStudyIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CompressedStudyRepository extends ElasticsearchRepository<CompressedStudyIndex, String> {
}
