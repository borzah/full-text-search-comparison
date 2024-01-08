package ee.taltech.elastic.errors;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RestError {

    private int status;
    private String message;
    private String errorCode;
    private Object moreInfo;

    public RestError(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
