package am.trade.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to add an attachment to a journal entry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalAttachmentRequest {

    private String fileName;
    private String fileUrl;
    private String fileType;
    private String description;
}
