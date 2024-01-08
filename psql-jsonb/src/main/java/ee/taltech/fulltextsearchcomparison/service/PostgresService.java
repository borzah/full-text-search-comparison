package ee.taltech.fulltextsearchcomparison.service;

import ee.taltech.fulltextsearchcomparison.dto.SearchQuery;
import ee.taltech.fulltextsearchcomparison.dto.SearchResult;
import ee.taltech.fulltextsearchcomparison.dto.StudySearchHitDto;
import ee.taltech.fulltextsearchcomparison.metadata.Metadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostgresService {

    private final PostgresSavingService savingService;
    private final StudySearchRepository studySearchRepository;

    public void saveData(Metadata metadata) {
        savingService.saveMetadataJson(metadata);
    }

    public void saveDataSeparateTables(Metadata metadata) {
        savingService.saveMetadataJsonSeparateTables(metadata);
    }

    public SearchResult searchStudies(SearchQuery searchQuery) {
        return studySearchRepository.searchStudies(searchQuery);
    }
}
