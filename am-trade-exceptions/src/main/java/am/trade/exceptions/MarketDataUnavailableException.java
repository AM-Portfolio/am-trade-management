package am.trade.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when the market data service is unavailable or returns an error.
 */
public class MarketDataUnavailableException extends TradeException {

    public MarketDataUnavailableException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public MarketDataUnavailableException(String message, Throwable cause) {
        super(message, cause, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
