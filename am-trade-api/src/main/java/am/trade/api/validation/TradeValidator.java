package am.trade.api.validation;

import am.trade.common.models.Attachment;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeEntryReasoning;
import am.trade.common.models.TradePsychologyData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validator for trade-related data
 * Handles validation of trade details, psychology data, entry reasoning, and attachments
 */
@Component
@Slf4j
public class TradeValidator {

    /**
     * Validates the basic required fields for a trade
     * 
     * @param tradeDetails The trade details to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateRequiredFields(TradeDetails tradeDetails) {
        if (tradeDetails.getUserId() == null || tradeDetails.getUserId().isEmpty()) {
            log.error("User ID is required");
            throw new IllegalArgumentException("User ID is required");
        }
        
        if (tradeDetails.getPortfolioId() == null || tradeDetails.getPortfolioId().isEmpty()) {
            log.error("Portfolio ID is required");
            throw new IllegalArgumentException("Portfolio ID is required");
        }
        
        if (tradeDetails.getSymbol() == null || tradeDetails.getSymbol().isEmpty()) {
            log.error("Symbol is required");
            throw new IllegalArgumentException("Symbol is required");
        }
    }
    
    /**
     * Validates trade psychology data
     * 
     * @param psychologyData The psychology data to validate
     * @return true if valid, false otherwise
     */
    public boolean validatePsychologyData(TradePsychologyData psychologyData) {
        if (psychologyData == null) {
            return true; // Psychology data is optional
        }
        
        // Validate entry psychology factors if provided
        if (psychologyData.getEntryPsychologyFactors() != null && psychologyData.getEntryPsychologyFactors().isEmpty()) {
            log.warn("Trade psychology entry factors list cannot be empty if provided");
            return false;
        }
        
        // Validate exit psychology factors if provided
        if (psychologyData.getExitPsychologyFactors() != null && psychologyData.getExitPsychologyFactors().isEmpty()) {
            log.warn("Trade psychology exit factors list cannot be empty if provided");
            return false;
        }
        
        // Validate psychology notes if provided
        if (psychologyData.getPsychologyNotes() != null && psychologyData.getPsychologyNotes().isEmpty()) {
            log.warn("Trade psychology notes cannot be empty if provided");
            return false;
        }
        
        log.debug("Trade psychology data validation passed");
        return true;
    }
    
    /**
     * Validates trade entry reasoning
     * 
     * @param entryReasoning The entry reasoning to validate
     * @return true if valid, false otherwise
     */
    public boolean validateEntryReasoning(TradeEntryReasoning entryReasoning) {
        if (entryReasoning == null) {
            return true; // Entry reasoning is optional
        }
        
        // Validate confidence level is between 1-10 if provided
        if (entryReasoning.getConfidenceLevel() != null) {
            int confidenceLevel = entryReasoning.getConfidenceLevel();
            if (confidenceLevel < 1 || confidenceLevel > 10) {
                log.warn("Trade entry reasoning confidence level should be between 1-10, got: {}", confidenceLevel);
                return false;
            }
        }
        
        // Validate primary reason if provided
        if (entryReasoning.getPrimaryReason() != null && entryReasoning.getPrimaryReason().isEmpty()) {
            log.warn("Trade entry reasoning primary reason cannot be empty if provided");
            return false;
        }
        
        log.debug("Trade entry reasoning validation passed");
        return true;
    }
    
    /**
     * Validates trade attachments
     * 
     * @param attachments The attachments to validate
     * @return true if valid, false otherwise
     */
    public boolean validateAttachments(List<Attachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return true; // Attachments are optional
        }
        
        boolean allValid = true;
        for (Attachment attachment : attachments) {
            // Check for null or empty data
            if (attachment.getData() == null || attachment.getData().isEmpty()) {
                log.warn("Attachment data cannot be null or empty");
                allValid = false;
            }
            
            // Check for valid attachment type
            if (attachment.getType() == null || attachment.getType().isEmpty()) {
                log.warn("Attachment type cannot be null or empty");
                allValid = false;
            }
            
            // Check for valid name
            if (attachment.getName() == null || attachment.getName().isEmpty()) {
                log.warn("Attachment name cannot be null or empty");
                allValid = false;
            }
        }
        
        if (allValid) {
            log.debug("All {} attachments passed validation", attachments.size());
        } else {
            log.warn("Some attachments failed validation");
        }
        
        return allValid;
    }
    
    /**
     * Validates all aspects of a trade
     * 
     * @param tradeDetails The trade details to validate
     * @throws IllegalArgumentException if required fields validation fails
     * @return true if all validations pass, false otherwise
     */
    public boolean validateTrade(TradeDetails tradeDetails) {
        // Validate required fields (throws exception if invalid)
        validateRequiredFields(tradeDetails);
        
        // Validate optional fields (returns false if any validation fails)
        boolean psychologyValid = validatePsychologyData(tradeDetails.getPsychologyData());
        boolean reasoningValid = validateEntryReasoning(tradeDetails.getEntryReasoning());
        boolean attachmentsValid = validateAttachments(tradeDetails.getAttachments());
        
        return psychologyValid && reasoningValid && attachmentsValid;
    }
}
