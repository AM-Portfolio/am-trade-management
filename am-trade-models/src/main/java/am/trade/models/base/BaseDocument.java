package am.trade.models.base;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

/**
 * Base document class for all MongoDB documents
 */
@Data
public abstract class BaseDocument {
    
    @Id
    private String id;
    
    @CreatedDate
    @Field("created_date")
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Field("last_modified_date")
    private LocalDateTime lastModifiedDate;
    
    @CreatedBy
    @Field("created_by")
    private String createdBy;
    
    @LastModifiedBy
    @Field("last_modified_by")
    private String lastModifiedBy;
    
    @Version
    private Long version;
    
    @Field("is_active")
    private boolean active = true;
}
