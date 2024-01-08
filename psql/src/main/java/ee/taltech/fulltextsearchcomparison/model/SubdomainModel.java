package ee.taltech.fulltextsearchcomparison.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SubdomainModel {
    private UUID id;

    private UUID metadataDomainId;

    private String label;

}
