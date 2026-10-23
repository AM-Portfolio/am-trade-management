package am.trade.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Extracts searchable plain text from journal content (Quill delta JSON or plain string).
 */
public final class JournalContentPlainText {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JournalContentPlainText() {
    }

    public static String extract(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String trimmed = content.trim();
        if (!trimmed.startsWith("{")) {
            return trimmed;
        }
        try {
            JsonNode root = MAPPER.readTree(trimmed);
            JsonNode ops = root.get("ops");
            if (ops == null || !ops.isArray()) {
                return trimmed;
            }
            StringBuilder sb = new StringBuilder();
            for (JsonNode op : ops) {
                JsonNode insert = op.get("insert");
                if (insert == null) {
                    continue;
                }
                if (insert.isTextual()) {
                    sb.append(insert.asText());
                } else if (insert.isObject()) {
                    JsonNode alt = insert.get("alt");
                    if (alt != null && alt.isTextual()) {
                        sb.append(alt.asText());
                    }
                }
            }
            String plain = sb.toString().trim();
            return plain.isEmpty() ? null : plain;
        } catch (Exception e) {
            return trimmed;
        }
    }
}
