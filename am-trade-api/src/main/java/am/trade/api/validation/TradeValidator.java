package am.trade.api.validation;

import com.am.security.context.UserContext;
import am.trade.common.models.Attachment;
import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.InstrumentInfo;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeEntryExistReasoning;
import am.trade.common.models.TradePsychologyData;
import am.trade.models.enums.TradeStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Authoritative trade write-path validator (create / update / batch).
 * Throws {@link IllegalArgumentException} on any hard failure → HTTP 400.
 */
@Component
@Slf4j
public class TradeValidator {

    /**
     * Full trade validation. Sets {@code userId} from auth context.
     *
     * @param tradeDetails trade payload
     * @throws IllegalArgumentException if any hard rule fails
     */
    public void validateTrade(TradeDetails tradeDetails) {
        if (tradeDetails == null) {
            throw new IllegalArgumentException("Trade details are required");
        }
        validateRequiredFields(tradeDetails);
        validateEntryInfo(tradeDetails);
        validateLifecycle(tradeDetails);
        validateInstrumentIfPresent(tradeDetails.getInstrumentInfo());
        validatePsychologyData(tradeDetails.getPsychologyData());
        validateReasoning(tradeDetails.getEntryReasoning(), "Entry");
        validateReasoning(tradeDetails.getExitReasoning(), "Exit");
        validateAttachments(tradeDetails.getAttachments());
    }

    /**
     * Portfolio, symbol, status, position type. Injects authenticated userId.
     */
    public void validateRequiredFields(TradeDetails tradeDetails) {
        tradeDetails.setUserId(UserContext.getUserIdOrThrow());

        if (isBlank(tradeDetails.getPortfolioId())) {
            throw new IllegalArgumentException("Portfolio ID is required");
        }

        String symbol = tradeDetails.getSymbol();
        if (isBlank(symbol)) {
            throw new IllegalArgumentException("Symbol is required");
        }

        if (tradeDetails.getStatus() == null) {
            throw new IllegalArgumentException("Trade status is required");
        }

        if (tradeDetails.getTradePositionType() == null) {
            throw new IllegalArgumentException("Trade position type is required");
        }
    }

    public void validateEntryInfo(TradeDetails tradeDetails) {
        EntryExitInfo entry = tradeDetails.getEntryInfo();
        if (entry == null) {
            throw new IllegalArgumentException("Entry information is required");
        }
        if (entry.getTimestamp() == null) {
            throw new IllegalArgumentException("Entry timestamp is required");
        }
        requirePositivePrice(entry.getPrice(), "Entry price");
        requirePositiveQuantity(entry.getQuantity(), "Entry quantity");
    }

    /**
     * OPEN may omit exit. WIN / LOSS / BREAK_EVEN require a valid exit
     * with time ≥ entry and quantity ≤ entry.
     */
    public void validateLifecycle(TradeDetails tradeDetails) {
        TradeStatus status = tradeDetails.getStatus();
        EntryExitInfo entry = tradeDetails.getEntryInfo();
        EntryExitInfo exit = tradeDetails.getExitInfo();

        boolean closed = status == TradeStatus.WIN
                || status == TradeStatus.LOSS
                || status == TradeStatus.BREAK_EVEN;

        if (!closed) {
            return;
        }

        if (exit == null) {
            throw new IllegalArgumentException("Exit information is required for closed trades");
        }
        if (exit.getTimestamp() == null) {
            throw new IllegalArgumentException("Exit timestamp is required for closed trades");
        }
        requirePositivePrice(exit.getPrice(), "Exit price");
        requirePositiveQuantity(exit.getQuantity(), "Exit quantity");

        if (entry != null && entry.getTimestamp() != null
                && exit.getTimestamp().isBefore(entry.getTimestamp())) {
            throw new IllegalArgumentException("Exit timestamp must be on or after entry timestamp");
        }
        if (entry != null && entry.getQuantity() != null
                && exit.getQuantity() > entry.getQuantity()) {
            throw new IllegalArgumentException("Exit quantity cannot exceed entry quantity");
        }
    }

    public void validateInstrumentIfPresent(InstrumentInfo instrumentInfo) {
        if (instrumentInfo == null) {
            return;
        }
        if (instrumentInfo.getExchange() == null) {
            throw new IllegalArgumentException("Exchange is required when instrument info is provided");
        }
        if (instrumentInfo.getSegment() == null) {
            throw new IllegalArgumentException("Segment is required when instrument info is provided");
        }
    }

    /**
     * Psychology is optional. When the block is present, reject blank notes if set
     * to an empty string (null lists are fine).
     */
    public void validatePsychologyData(TradePsychologyData psychologyData) {
        if (psychologyData == null) {
            return;
        }
        if (psychologyData.getPsychologyNotes() != null
                && psychologyData.getPsychologyNotes().isEmpty()) {
            throw new IllegalArgumentException("Psychology notes cannot be empty if provided");
        }
    }

    public void validateReasoning(TradeEntryExistReasoning reasoning, String label) {
        if (reasoning == null) {
            return;
        }
        requireConfidenceInRange(reasoning.getConfidenceLevel(), label + " confidence level");
        requireConfidenceInRange(reasoning.getExitConfidenceLevel(), label + " exit confidence level");
        requireConfidenceInRange(reasoning.getExitQualityScore(), label + " exit quality score");

        if (reasoning.getPrimaryReason() != null && reasoning.getPrimaryReason().isEmpty()) {
            throw new IllegalArgumentException(label + " primary reason cannot be empty if provided");
        }
        if (reasoning.getExitPrimaryReason() != null && reasoning.getExitPrimaryReason().isEmpty()) {
            throw new IllegalArgumentException(label + " exit primary reason cannot be empty if provided");
        }
    }

    public void validateAttachments(List<Attachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }
        for (int i = 0; i < attachments.size(); i++) {
            Attachment attachment = attachments.get(i);
            if (attachment == null) {
                throw new IllegalArgumentException("Attachment at index " + i + " is null");
            }
            if (isBlank(attachment.getFileName())) {
                throw new IllegalArgumentException("Attachment fileName is required");
            }
            if (isBlank(attachment.getFileType())) {
                throw new IllegalArgumentException("Attachment fileType is required");
            }
            if (isBlank(attachment.getFileUrl())) {
                throw new IllegalArgumentException("Attachment fileUrl is required");
            }
        }
    }

    private static void requirePositivePrice(BigDecimal price, String label) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0");
        }
    }

    private static void requirePositiveQuantity(Integer quantity, String label) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(label + " must be greater than 0");
        }
    }

    private static void requireConfidenceInRange(Integer value, String label) {
        if (value == null) {
            return;
        }
        if (value < 1 || value > 10) {
            throw new IllegalArgumentException(label + " must be between 1 and 10");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
