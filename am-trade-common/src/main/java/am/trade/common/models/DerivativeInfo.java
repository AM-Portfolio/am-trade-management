package am.trade.common.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class to hold derivative-specific information for options and futures
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DerivativeInfo {
    // Common fields for derivatives
    private LocalDate expiryDate;
    private String underlyingSymbol;
    
    // Option-specific fields
    private BigDecimal strikePrice;
    private Boolean isCall;  // true for call, false for put
    private Boolean isEuropean; // true for European, false for American
    
    // Future-specific fields
    private String futureType; // WEEKLY, MONTHLY, QUARTERLY
    
    // Settlement information
    private Boolean isCashSettled;
}
