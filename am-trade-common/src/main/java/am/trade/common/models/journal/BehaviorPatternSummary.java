package am.trade.common.models.journal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorPatternSummary {
    private String summary;
    private String mood;
    private Integer marketSentiment;
    private List<String> tags;
}
