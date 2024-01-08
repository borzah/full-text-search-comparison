package ee.taltech.elastic.converter;

import ee.taltech.elastic.model.dto.StudySearchHitDto;
import ee.taltech.elastic.model.dto.VariableSearchHitDto;
import ee.taltech.elastic.model.index.CompressedStudyIndex;
import ee.taltech.elastic.model.index.StudyIndex;
import ee.taltech.elastic.model.index.nested.CompressedVariableNested;
import ee.taltech.elastic.model.index.nested.LogicalRecordNested;
import ee.taltech.elastic.model.index.nested.VariableNested;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHit;

import java.util.List;

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@NoArgsConstructor(access = PRIVATE)
public class DtoMapper {

    public static final String INNER_HITS_PATH_STUDY_VARIABLES = "dataFiles.logicalRecords.variables";
    public static final String HIGHLIGHT_PATH_STUDY_VARIABLE_LABEL = "dataFiles.logicalRecords.variables.label";
    public static final String INNER_HITS_PATH_STUDY_VARIABLES_COMP = "variables";
    public static final String HIGHLIGHT_PATH_STUDY_VARIABLE_LABEL_COMP = "variables.label";
    public static final int MAX_AMOUNT_OF_VARIABLES = 6;

    public static List<StudySearchHitDto> transformStudyIndexSearchHitToPaginatedStudiesDto(List<SearchHit<StudyIndex>> studySearchHits) {
        return studySearchHits.stream().map(studyIndexSearchHit -> {
            StudySearchHitDto studySearchHitDto = new StudySearchHitDto();
            studySearchHitDto.setId(studyIndexSearchHit.getContent().getId());
            studySearchHitDto.setSeriesTitle(studyIndexSearchHit.getContent().getSeriesTitle());
            studySearchHitDto.setUniverseLabel(studyIndexSearchHit.getContent().getUniverseLabel());
            studySearchHitDto.setTitle(studyIndexSearchHit.getContent().getTitle());
            studySearchHitDto.setPurpose(studyIndexSearchHit.getContent().getPurpose());
            studySearchHitDto.setReferenceArea(studyIndexSearchHit.getContent().getReferenceArea());
            studySearchHitDto.setTimeCoverage(studyIndexSearchHit.getContent().getTimeCoverage());
            studySearchHitDto.setVariables(ofNullable(studyIndexSearchHit.getInnerHits(INNER_HITS_PATH_STUDY_VARIABLES))
                    .map(innerHits -> innerHits.getSearchHits()
                            .stream()
                            .limit(MAX_AMOUNT_OF_VARIABLES)
                            .map(variableSearchHit -> getStudyVariableSearchHitDto((SearchHit<VariableNested>) variableSearchHit, studyIndexSearchHit))
                            .toList())
                    .orElse(List.of()));
            return studySearchHitDto;
        }).toList();
    }

    private static VariableSearchHitDto getVariableSearchHitDto(SearchHit<VariableNested> searchHit, String highlightFieldPath) {
        VariableSearchHitDto variableSearchHitDto = new VariableSearchHitDto();
        variableSearchHitDto.setId(searchHit.getContent().getId());
        variableSearchHitDto.setName(searchHit.getContent().getName());
        variableSearchHitDto.setRepresentationType(searchHit.getContent().getRepresentationType());

        String unHighlightedLabel = searchHit.getContent().getLabel();
        List<String> highlightedLabels = searchHit.getHighlightField(highlightFieldPath);
        variableSearchHitDto.setLabel(isNotEmpty(highlightedLabels) ? highlightedLabels.get(0) : unHighlightedLabel);
        return variableSearchHitDto;
    }

    private static VariableSearchHitDto getStudyVariableSearchHitDto(SearchHit<VariableNested> searchHit, SearchHit<StudyIndex> studyIndexSearchHit) {
        int dataFileOffset = searchHit.getNestedMetaData().getOffset();
        int logicalRecordOffset = searchHit.getNestedMetaData().getChild().getOffset();
        int variableOffset = searchHit.getNestedMetaData().getChild().getChild().getOffset();
        LogicalRecordNested logicalRecordNested = studyIndexSearchHit.getContent().getDataFiles().get(dataFileOffset).getLogicalRecords().get(logicalRecordOffset);
        String logicalRecordId = logicalRecordNested.getId();
        String variableId = logicalRecordNested.getVariables().get(variableOffset).getId();

        VariableSearchHitDto variableSearchHitDto = getVariableSearchHitDto(searchHit, HIGHLIGHT_PATH_STUDY_VARIABLE_LABEL);
        variableSearchHitDto.setLogicalRecordId(logicalRecordId);
        variableSearchHitDto.setId(variableId); // TODO: this is redundant when elasticsearch spring data package is updated and the bug is fixed where innerhits items get the index id instead of their own id
        return variableSearchHitDto;
    }

    public static List<StudySearchHitDto> transformStudyIndexSearchHitToPaginatedStudiesDtoComp(List<SearchHit<CompressedStudyIndex>> studySearchHits) {
        return studySearchHits.stream().map(studyIndexSearchHit -> {
            StudySearchHitDto studySearchHitDto = new StudySearchHitDto();
            studySearchHitDto.setId(studyIndexSearchHit.getContent().getId());
            studySearchHitDto.setSeriesTitle(studyIndexSearchHit.getContent().getSeriesTitle());
            studySearchHitDto.setUniverseLabel(studyIndexSearchHit.getContent().getUniverseLabel());
            studySearchHitDto.setTitle(studyIndexSearchHit.getContent().getTitle());
            studySearchHitDto.setPurpose(studyIndexSearchHit.getContent().getPurpose());
            studySearchHitDto.setReferenceArea(studyIndexSearchHit.getContent().getReferenceArea());
            studySearchHitDto.setTimeCoverage(studyIndexSearchHit.getContent().getTimeCoverage());
            studySearchHitDto.setVariables(ofNullable(studyIndexSearchHit.getInnerHits(INNER_HITS_PATH_STUDY_VARIABLES_COMP))
                    .map(innerHits -> innerHits.getSearchHits()
                            .stream()
                            .limit(MAX_AMOUNT_OF_VARIABLES)
                            .map(variableSearchHit -> getStudyVariableSearchHitDtoComp((SearchHit<CompressedVariableNested>) variableSearchHit, studyIndexSearchHit))
                            .toList())
                    .orElse(List.of()));
            return studySearchHitDto;
        }).toList();
    }

    private static VariableSearchHitDto getVariableSearchHitDtoComp(SearchHit<CompressedVariableNested> searchHit) {
        VariableSearchHitDto variableSearchHitDto = new VariableSearchHitDto();
        variableSearchHitDto.setId(searchHit.getContent().getId());
        variableSearchHitDto.setName(searchHit.getContent().getName());
        variableSearchHitDto.setRepresentationType(searchHit.getContent().getRepresentationType());

        String unHighlightedLabel = searchHit.getContent().getLabel();
        List<String> highlightedLabels = searchHit.getHighlightField(HIGHLIGHT_PATH_STUDY_VARIABLE_LABEL_COMP);
        variableSearchHitDto.setLabel(isNotEmpty(highlightedLabels) ? highlightedLabels.get(0) : unHighlightedLabel);
        return variableSearchHitDto;
    }

    private static VariableSearchHitDto getStudyVariableSearchHitDtoComp(SearchHit<CompressedVariableNested> searchHit, SearchHit<CompressedStudyIndex> studyIndexSearchHit) {
        int variableOffset = searchHit.getNestedMetaData().getOffset();
        String variableId = studyIndexSearchHit.getContent().getVariables().get(variableOffset).getId();
        String logicalRecordId = studyIndexSearchHit.getContent().getVariables().get(variableOffset).getLogicalRecordId();
        VariableSearchHitDto variableSearchHitDto = getVariableSearchHitDtoComp(searchHit);
        variableSearchHitDto.setId(variableId); // TODO: this is redundant when elasticsearch spring data package is updated and the bug is fixed where innerhits items get the index id instead of their own id
        variableSearchHitDto.setLogicalRecordId(logicalRecordId);
        return variableSearchHitDto;
    }
}
