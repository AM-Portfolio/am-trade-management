package am.trade.common.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
    
    @Field("fileName")
    @JsonProperty("fileName")
    private String fileName;
    
    @Field("fileUrl")
    @JsonProperty("fileUrl")
    private String fileUrl;
    
    @Field("fileType")
    @JsonProperty("fileType")
    private String fileType;
    
    @Field("uploadedAt")
    @JsonProperty("uploadedAt")
    private LocalDateTime uploadedAt;
    
    @Field("description")
    @JsonProperty("description")
    private String description;
}
