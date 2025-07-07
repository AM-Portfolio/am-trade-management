package am.trade.common.jackson;

import am.trade.common.models.enums.FundamentalEntryReason;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Jackson mixin for FundamentalEntryReason to enable proper deserialization from strings
 */
public abstract class FundamentalEntryReasonMixin {
    
    /**
     * Factory method for Jackson to use when deserializing from string
     */
    @JsonCreator
    public static FundamentalEntryReason fromCode(String code) {
        return FundamentalEntryReason.fromCode(code);
    }
}
