package ee.taltech.elastic.service;

import ee.taltech.elastic.converter.DtoMapper;
import ee.taltech.elastic.model.Metadata;
import ee.taltech.elastic.model.SearchPage;
import ee.taltech.elastic.model.dto.StudySearchHitDto;
import ee.taltech.elastic.model.index.CodeListIndex;
import ee.taltech.elastic.model.index.CompressedStudyIndex;
import ee.taltech.elastic.model.index.DomainIndex;
import ee.taltech.elastic.model.index.StudyIndex;
import ee.taltech.elastic.model.index.nested.CompressedVariableNested;
import ee.taltech.elastic.model.query.SearchQueryParams;
import ee.taltech.elastic.model.query.StudySearchQuery;
import ee.taltech.elastic.repository.CodeListRepository;
import ee.taltech.elastic.repository.CompressedStudyRepository;
import ee.taltech.elastic.repository.DomainRepository;
import ee.taltech.elastic.repository.StudyRepository;
import ee.taltech.elastic.validator.SearchValidator;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static ee.taltech.elastic.model.query.compressed.CompressedQueryBuilder.getQueryForCompressed;
import static ee.taltech.elastic.util.SearchUtil.getSearchQueryPageable;

@Service
@RequiredArgsConstructor
@ToString
@Slf4j
public class MetadataService {

    private final CodeListRepository codeListRepository;
    private final DomainRepository domainRepository;
    private final StudyRepository studyRepository;
    private final CompressedStudyRepository compressedStudyRepository;
    private final ElasticsearchOperations operations;

    public void saveMetadata(Metadata metadata, boolean isCluster) {
        List<CodeListIndex> codeListIndices = new ArrayList<>();
        List<DomainIndex> domainIndices = new ArrayList<>();
        List<StudyIndex> studyIndices = new ArrayList<>();

        metadata.domains().forEach(domain -> {
            DomainIndex domainIndex = DomainIndex.createDomainIndex(domain);
            domainIndices.add(domainIndex);

            domain.subdomains().forEach(subdomain -> subdomain.series().forEach(series -> series.studies().forEach(study -> {
                StudyIndex studyIndex = StudyIndex.createStudyIndex(study);
                studyIndices.add(studyIndex);
            })));
        });

        metadata.codeLists().forEach(codeList -> {
            CodeListIndex codeListIndex = CodeListIndex.createCodeListIndex(codeList);
            codeListIndices.add(codeListIndex);
        });

//        List<CompressedStudyIndex> compressedStudyIndices = getCompressedStudyIndexes(studyIndices);

        try {
            codeListRepository.deleteAll();
            if (isCluster) {
                List<List<CodeListIndex>> subLists = ListUtils.partition(codeListIndices, 4);
                subLists.forEach(codeListRepository::saveAll);
            } else {
                codeListRepository.saveAll(codeListIndices);
            }
        } catch (Exception e) {
            log.error("Codelist saving failed. Error: {}", e.getMessage());
        }
        try {
            domainRepository.deleteAll();
            domainRepository.saveAll(domainIndices);
        } catch (Exception e) {
            log.error("Domain saving failed. Error: {}", e.getMessage());
        }
        try {
            studyRepository.deleteAll();
            // save with batches in case of elastic cluster
            if (isCluster) {
                List<List<StudyIndex>> subLists = ListUtils.partition(studyIndices, 4);
                subLists.forEach(studyRepository::saveAll);
            } else {
                studyRepository.saveAll(studyIndices);
            }
        } catch (Exception e) {
            log.error("Study saving failed. Error: {}", e.getMessage());
        }

//        try {
//            compressedStudyRepository.deleteAll();
//            // save with batches in case of elastic cluster
//            List<List<CompressedStudyIndex>> subLists = ListUtils.partition(compressedStudyIndices, 5);
//            subLists.forEach(compressedStudyRepository::saveAll);
////            compressedStudyRepository.saveAll(compressedStudyIndices);
//        } catch (Exception e) {
//            log.error("Study compressed saving failed. Error: {}", e.getMessage());
//        }
    }

    public SearchPage<StudySearchHitDto> findStudiesBy(SearchQueryParams searchQueryParams) {
        SearchValidator.validateSearchParams(searchQueryParams);
        Pageable pageable = getSearchQueryPageable(searchQueryParams.getPage());
        Query query = new StudySearchQuery(searchQueryParams, pageable).getQuery();
        SearchHits<StudyIndex> searchHits = operations.search(query, StudyIndex.class);
        return new SearchPage<>(DtoMapper.transformStudyIndexSearchHitToPaginatedStudiesDto(searchHits.getSearchHits()), pageable, searchHits.getTotalHits());
    }

    public SearchPage<StudySearchHitDto> findStudiesCompressedBy(SearchQueryParams searchQueryParams) {
        SearchValidator.validateSearchParams(searchQueryParams);
        Pageable pageable = getSearchQueryPageable(searchQueryParams.getPage());
        Query query = getQueryForCompressed(pageable, searchQueryParams.getSearchValue());
        SearchHits<CompressedStudyIndex> searchHits = operations.search(query, CompressedStudyIndex.class);
        return new SearchPage<>(DtoMapper.transformStudyIndexSearchHitToPaginatedStudiesDtoComp(searchHits.getSearchHits()), pageable, searchHits.getTotalHits());
    }

    private List<CompressedStudyIndex> getCompressedStudyIndexes(List<StudyIndex> studyIndices) {
        List<CompressedStudyIndex> compressedStudyIndices = new ArrayList<>();

        studyIndices.forEach(studyIndex -> {
            List<CompressedVariableNested> compressedVariables = new ArrayList<>();
            List<String> studyWords = new ArrayList<>(List.of(
                    studyIndex.getSeriesTitle(),
                    studyIndex.getUniverseLabel(),
                    studyIndex.getTitle(),
                    studyIndex.getSummary(),
                    studyIndex.getPurpose(),
                    studyIndex.getStudyCode(),
                    studyIndex.getSectorCoverage(),
                    studyIndex.getOtherDissemination(),
                    studyIndex.getDocumentationOnMethodology(),
                    studyIndex.getGeographicalComparability(),
                    studyIndex.getComparabilityOverTime(),
                    studyIndex.getSourceData(),
                    studyIndex.getFrequencyOfDataCollection(),
                    studyIndex.getDataCollection(),
                    studyIndex.getDataValidation(),
                    studyIndex.getDataCompilation()));
            studyIndex.getDataFiles().forEach(dataFileNested -> {
                studyWords.add(dataFileNested.getTitle());
                dataFileNested.getKeywords().forEach(keywordNested -> studyWords.add(keywordNested.getName()));
                dataFileNested.getSubjects().forEach(subjectNested -> studyWords.add(subjectNested.getName()));
                dataFileNested.getLogicalRecords().forEach(logicalRecordNested -> {
                    studyWords.add(logicalRecordNested.getName());
                    studyWords.add(logicalRecordNested.getLabel());
                    studyWords.add(logicalRecordNested.getDescription());
                    logicalRecordNested.getVariables().forEach(studyVariableNested -> {
                        studyWords.add(studyVariableNested.getName());
                        studyWords.add(studyVariableNested.getLabel());
                        studyWords.add(studyVariableNested.getDescription());
                        studyWords.add(studyVariableNested.getRepresentedVariableLabel());
                        studyWords.add(studyVariableNested.getConceptualVariableLabel());
                        compressedVariables.add(
                                new CompressedVariableNested(
                                        studyVariableNested.getId(),
                                        studyVariableNested.getUnitTypeId(),
                                        studyVariableNested.getName(),
                                        studyVariableNested.getLabel(),
                                        studyVariableNested.getDescription(),
                                        studyVariableNested.getRepresentationType(),
                                        studyVariableNested.getRepresentedVariableLabel(),
                                        studyVariableNested.getConceptualVariableLabel(),
                                        logicalRecordNested.getId()
                                )
                        );
                        studyVariableNested.getConcepts().forEach(conceptNested -> studyWords.add(conceptNested.getLabel()));
                    });
                });
            });

            StringBuilder stringBuilder = new StringBuilder();
            for (String word: studyWords) {
                stringBuilder.append(word);
                stringBuilder.append(" ");
            }
            String studyWordsDocument = stringBuilder.toString();

            compressedStudyIndices.add(
                    new CompressedStudyIndex(
                            studyIndex.getId(),
                            studyIndex.getDomainId(),
                            studyIndex.getSubdomainId(),
                            studyIndex.getSeriesId(),
                            studyIndex.getSeriesTitle(),
                            studyIndex.getUniverseLabel(),
                            studyIndex.getTitle(),
                            studyIndex.getPurpose(),
                            studyIndex.getReferenceArea(),
                            studyIndex.getTimeCoverage(),
                            studyWordsDocument,
                            compressedVariables
                    ));
        });
        return compressedStudyIndices;
    }
}
