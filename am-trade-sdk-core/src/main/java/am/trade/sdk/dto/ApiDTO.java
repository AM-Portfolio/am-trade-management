package am.trade.sdk.dto;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * User-facing DTOs for other operations.
 */
public class ApiDTO {

    /**
     * DTO for journal entry creation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JournalEntryCreateRequest {
        @SerializedName("trade_id")
        private String tradeId;

        @SerializedName("title")
        private String title;

        @SerializedName("content")
        private String content;

        @SerializedName("tags")
        private List<String> tags;

        public boolean isValid() {
            return title != null && !title.isEmpty()
                    && content != null && !content.isEmpty();
        }
    }

    /**
     * DTO for journal entry responses.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JournalEntryResponse {
        @SerializedName("id")
        private String id;

        @SerializedName("trade_id")
        private String tradeId;

        @SerializedName("title")
        private String title;

        @SerializedName("content")
        private String content;

        @SerializedName("tags")
        private List<String> tags;

        @SerializedName("created_at")
        private LocalDateTime createdAt;

        @SerializedName("updated_at")
        private LocalDateTime updatedAt;
    }

    /**
     * DTO for filter creation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterCreateRequest {
        @SerializedName("name")
        private String name;

        @SerializedName("criteria")
        private Map<String, Object> criteria;

        @SerializedName("description")
        private String description;

        public boolean isValid() {
            return name != null && !name.isEmpty()
                    && criteria != null && !criteria.isEmpty();
        }
    }

    /**
     * DTO for filter responses.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterResponse {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("criteria")
        private Map<String, Object> criteria;

        @SerializedName("description")
        private String description;

        @SerializedName("is_shared")
        private Boolean isShared;

        @SerializedName("created_by")
        private String createdBy;

        @SerializedName("created_at")
        private LocalDateTime createdAt;
    }

    /**
     * DTO for paginated responses.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagedResponse {
        @SerializedName("content")
        private List<Map<String, Object>> content;

        @SerializedName("page_number")
        private Integer pageNumber;

        @SerializedName("page_size")
        private Integer pageSize;

        @SerializedName("total_elements")
        private Long totalElements;

        @SerializedName("total_pages")
        private Integer totalPages;

        @SerializedName("is_last")
        private Boolean isLast;

        @SerializedName("is_first")
        private Boolean isFirst;
    }

    /**
     * DTO for success responses.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuccessResponse {
        @SerializedName("data")
        private Map<String, Object> data;

        @SerializedName("message")
        private String message;

        @SerializedName("request_id")
        private String requestId;

        @SerializedName("timestamp")
        private LocalDateTime timestamp;
    }

    /**
     * DTO for error responses.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        @SerializedName("status_code")
        private Integer statusCode;

        @SerializedName("message")
        private String message;

        @SerializedName("error_code")
        private String errorCode;

        @SerializedName("details")
        private Map<String, Object> details;

        @SerializedName("request_id")
        private String requestId;

        @SerializedName("timestamp")
        private LocalDateTime timestamp;
    }
}
