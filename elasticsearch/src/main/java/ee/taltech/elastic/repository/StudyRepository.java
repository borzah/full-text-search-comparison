package ee.taltech.elastic.repository;

import ee.taltech.elastic.model.index.StudyIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface StudyRepository extends ElasticsearchRepository<StudyIndex, String> {
}
