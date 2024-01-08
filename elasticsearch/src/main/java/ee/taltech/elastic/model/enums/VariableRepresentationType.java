package ee.taltech.elastic.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VariableRepresentationType {

    NUMERIC("Numeric"),
    TEXT("Text"),
    CODE("Code"),
    DATE("DateTime");

    private final String value;
}
