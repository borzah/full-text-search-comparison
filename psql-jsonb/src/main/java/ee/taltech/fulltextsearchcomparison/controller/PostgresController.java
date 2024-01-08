package ee.taltech.fulltextsearchcomparison.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.fulltextsearchcomparison.dto.SearchQuery;
import ee.taltech.fulltextsearchcomparison.dto.SearchResult;
import ee.taltech.fulltextsearchcomparison.dto.StudySearchHitDto;
import ee.taltech.fulltextsearchcomparison.metadata.Metadata;
import ee.taltech.fulltextsearchcomparison.service.PostgresService;
import ee.taltech.fulltextsearchcomparison.util.DataUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("postgres")
@RequiredArgsConstructor
public class PostgresController {

    public static final String LOG_SEPARATOR = "-------";

    private final PostgresService postgresService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public void saveData(@RequestBody Metadata metadata) {
        Instant start = Instant.now();
        postgresService.saveData(metadata);
        Instant finish = Instant.now();
        log.info(LOG_SEPARATOR);
        log.info("Save duration: {} ms", Duration.between(start, finish).toMillis());
        log.info(LOG_SEPARATOR);
    }

    @PostMapping("file")
    public void saveMetadataFromFile() {
        Metadata metadata = DataUtil.readFromJson("testdata/metadata.json", objectMapper, new TypeReference<>() {});
        Instant start = Instant.now();
        postgresService.saveData(metadata);
        Instant finish = Instant.now();
        log.info(LOG_SEPARATOR);
        log.info("Save duration: {} ms", Duration.between(start, finish).toMillis());
        log.info(LOG_SEPARATOR);
    }

    @PostMapping("separate-tables/file")
    public void saveMetadataFromFileSeparateTables() {
        Metadata metadata = DataUtil.readFromJson("testdata/metadata.json", objectMapper, new TypeReference<>() {});
        Instant start = Instant.now();
        postgresService.saveDataSeparateTables(metadata);
        Instant finish = Instant.now();
        log.info(LOG_SEPARATOR);
        log.info("Save duration: {} ms", Duration.between(start, finish).toMillis());
        log.info(LOG_SEPARATOR);
    }

    @GetMapping
    public SearchResult searchStudies(@Valid SearchQuery searchQuery) {
        Instant start = Instant.now();
        SearchResult studySearchHitDtos = postgresService.searchStudies(searchQuery);
        Instant finish = Instant.now();
        log.info(LOG_SEPARATOR);
        log.info("Search duration: {} ms", Duration.between(start, finish).toMillis());
        log.info(LOG_SEPARATOR);
        return studySearchHitDtos;
    }
}
