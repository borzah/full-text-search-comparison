package ee.taltech.fulltextsearchcomparison.service;

import ee.taltech.fulltextsearchcomparison.dto.StudySearchHitDto;
import ee.taltech.fulltextsearchcomparison.metadata.Metadata;
import ee.taltech.fulltextsearchcomparison.dto.SearchPage;
import ee.taltech.fulltextsearchcomparison.dto.SearchQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostgresService {

    private final StudySearchRepository studySearchRepository;
    private final PostgresSavingService savingService;

    public void saveData(Metadata metadata) {
        savingService.saveMetadata(metadata);
    }

    public SearchPage<StudySearchHitDto> searchStudies(SearchQuery searchQuery) {
        return studySearchRepository.searchStudies(searchQuery);
    }
}
