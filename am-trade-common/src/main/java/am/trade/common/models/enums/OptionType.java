package am.trade.common.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;

@Schema(description = "Type of option contract")
public enum OptionType {
    @Schema(description = "Call Option (CE) - Right to buy")
    CALL("CE", "Call Option"),
    
    @Schema(description = "Put Option (PE) - Right to sell")
    PUT("PE", "Put Option"),
    
    @Schema(description = "Not Applicable - Not an option instrument")
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
