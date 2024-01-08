package ee.taltech.fulltextsearchcomparison.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SearchQuery {

    @NotBlank
    private String searchValue;
    private Integer page = 0;
    private Integer size = 10;
}
