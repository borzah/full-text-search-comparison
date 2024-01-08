package ee.taltech.fulltextsearchcomparison.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SeriesModel {

    private UUID id;

    private UUID subdomainId;

    private String title;

}
