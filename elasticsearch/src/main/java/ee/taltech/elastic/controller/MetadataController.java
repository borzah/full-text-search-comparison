package ee.taltech.elastic.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.elastic.model.Metadata;
import ee.taltech.elastic.model.SearchPage;
import ee.taltech.elastic.model.dto.StudySearchHitDto;
import ee.taltech.elastic.model.query.SearchQueryParams;
import ee.taltech.elastic.service.MetadataService;
import ee.taltech.elastic.util.DataUtil;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("v1/metadata")
@RequiredArgsConstructor
@ToString
public class MetadataController {

    public static final String LOG_SEPARATOR = "-------";

    private final MetadataService metadataService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public void saveMetadata(@RequestBody Metadata metadata) {
        Instant start = Instant.now();
        metadataService.saveMetadata(metadata, false);
        Instant finish = Instant.now();
        log.info(LOG_SEPARATOR);
        log.info("Save duration: {} ms", Duration.between(start, finish).toMillis());
        log.info(LOG_SEPARATOR);
    }

    @PostMapping("cluster")
    public void saveMetadataCluster(@RequestBody Metadata metadata) {
        Instant start = Instant.now();
        metadataService.saveMetadata(metadata, true);
        Instant finish = Instant.now();
        log.info(LOG_SEPARATOR);
        log.info("Save duration: {} ms", Duration.between(start, finish).toMillis());
        log.info(LOG_SEPARATOR);
    }

    @PostMapping("file")
    public void saveMetadataFromFile() {
        Metadata metadata = DataUtil.readFromJson("testdata/metadata.json", objectMapper, new TypeReference<>() {});
        Instant start = Instant.now();
        metadataService.saveMetadata(metadata, false);
        Instant finish = Instant.now();
        log.info(LOG_SEPARATOR);
        log.info("Save duration: {} ms", Duration.between(start, finish).toMillis());
        log.info(LOG_SEPARATOR);
    }

    @PostMapping("file/cluster")
    public void saveMetadataFromFileCluster() {
        Metadata metadata = DataUtil.readFromJson("testdata/metadata.json", objectMapper, new TypeReference<>() {});
        Instant start = Instant.now();
        metadataService.saveMetadata(metadata, true);
        Instant finish = Instant.now();
        log.info(LOG_SEPARATOR);
        log.info("Save duration: {} ms", Duration.between(start, finish).toMillis());
        log.info(LOG_SEPARATOR);
    }

    @GetMapping("/search/studies")
    public SearchPage<StudySearchHitDto> getStudiesBySearchFilter(SearchQueryParams searchQueryParams) {
        Instant start = Instant.now();
        SearchPage<StudySearchHitDto> studiesBy = metadataService.findStudiesBy(searchQueryParams);
        Instant finish = Instant.now();
        log.info(LOG_SEPARATOR);
        log.info("Search duration: {} ms", Duration.between(start, finish).toMillis());
        log.info(LOG_SEPARATOR);
        return studiesBy;
    }

//    @GetMapping("/search/studies/comp")
//    public SearchPage<StudySearchHitDto> getStudiesBySearchFilterCompressed(SearchQueryParams searchQueryParams) {
//        Instant start = Instant.now();
//        SearchPage<StudySearchHitDto> studiesBy = metadataService.findStudiesCompressedBy(searchQueryParams);
//        Instant finish = Instant.now();
//        log.info(LOG_SEPARATOR);
//        log.info("Search duration: {} ms", Duration.between(start, finish).toMillis());
//        log.info(LOG_SEPARATOR);
//        return studiesBy;
//    }
}
