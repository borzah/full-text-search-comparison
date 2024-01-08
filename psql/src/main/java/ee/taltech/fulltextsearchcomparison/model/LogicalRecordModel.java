package ee.taltech.fulltextsearchcomparison.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LogicalRecordModel {
    private UUID id;

    private UUID dataFileId;

    private String name;

    private String label;

    private String description;

    private String databaseUrl;

    private Long numberOfEntries;

}
