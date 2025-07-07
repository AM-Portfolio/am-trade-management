package am.trade.common.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum OptionType {
    CALL("CE", "Call Option"),
    PUT("PE", "Put Option"),
    NONE(null, "Not Applicable");

    private final String value;
    private final String description;

    OptionType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static OptionType fromValue(String value) {
        if (value == null) {
            return NONE;
        }
        
        return Arrays.stream(values())
                .filter(optionType -> {
                    if (optionType.value == null) {
                        return false;
                    }
                    return optionType.value.equalsIgnoreCase(value);
                })
                .findFirst()
                .orElse(NONE);
    }
}
