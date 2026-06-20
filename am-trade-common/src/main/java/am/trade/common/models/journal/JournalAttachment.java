package am.trade.common.models.journal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalAttachment {
    private String fileName;
    private String fileUrl;
    private String fileType;
    private String uploadedAt;
    private String description;
}
