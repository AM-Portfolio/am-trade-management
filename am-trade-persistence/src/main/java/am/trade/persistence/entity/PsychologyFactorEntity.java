package am.trade.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity for storing custom psychology factors with their descriptions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PsychologyFactorEntity {
    // The code identifier for the psychology factor
    private String code;
    
    // The description of the psychology factor
    private String description;
}
