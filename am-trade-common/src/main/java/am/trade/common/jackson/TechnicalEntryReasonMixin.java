package am.trade.common.jackson;

import am.trade.common.models.enums.TechnicalEntryReason;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Jackson mixin for TechnicalEntryReason to enable proper deserialization from strings
 */
public abstract class TechnicalEntryReasonMixin {
    
    /**
     * Factory method for Jackson to use when deserializing from string
     */
    @JsonCreator
    public static TechnicalEntryReason fromCode(String code) {
        return TechnicalEntryReason.fromCode(code);
    }
}
