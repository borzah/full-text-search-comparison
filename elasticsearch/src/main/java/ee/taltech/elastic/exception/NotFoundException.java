package ee.taltech.elastic.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NotFoundException extends RuntimeException {

    private final String code;

    public NotFoundException(String message, String code) {
        super(message);
        this.code = code;
    }
}
