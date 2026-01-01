package am.trade.common.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Stack;

/**
 * Smart application logger that automatically detects the calling class and
 * method.
 */
@Component
public class AppLogger {

    /**
     * Log an INFO message with automatic class and method detection
     * 
     * @param message The message format string
     * @param args    The arguments for the message format
     */
    public void info(String message, Object... args) {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        walker.walk(frames -> frames.skip(1).findFirst())
                .ifPresent(frame -> {
                    Logger logger = LoggerFactory.getLogger(frame.getDeclaringClass());
                    String prefix = "[" + frame.getMethodName() + "] ";
                    logger.info(prefix + message, args);
                });
    }

    /**
     * Log a DEBUG message with automatic class and method detection
     * 
     * @param message The message format string
     * @param args    The arguments for the message format
     */
    public void debug(String message, Object... args) {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        walker.walk(frames -> frames.skip(1).findFirst())
                .ifPresent(frame -> {
                    Logger logger = LoggerFactory.getLogger(frame.getDeclaringClass());
                    if (logger.isDebugEnabled()) {
                        String prefix = "[" + frame.getMethodName() + "] ";
                        logger.debug(prefix + message, args);
                    }
                });
    }

    /**
     * Log a WARN message with automatic class and method detection
     * 
     * @param message The message format string
     * @param args    The arguments for the message format
     */
    public void warn(String message, Object... args) {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        walker.walk(frames -> frames.skip(1).findFirst())
                .ifPresent(frame -> {
                    Logger logger = LoggerFactory.getLogger(frame.getDeclaringClass());
                    String prefix = "[" + frame.getMethodName() + "] ";
                    logger.warn(prefix + message, args);
                });
    }

    /**
     * Log an ERROR message with automatic class and method detection
     * 
     * @param message The message format string
     * @param args    The arguments for the message format
     */
    public void error(String message, Object... args) {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        walker.walk(frames -> frames.skip(1).findFirst())
                .ifPresent(frame -> {
                    Logger logger = LoggerFactory.getLogger(frame.getDeclaringClass());
                    String prefix = "[" + frame.getMethodName() + "] ";
                    logger.error(prefix + message, args);
                });
    }

    /**
     * Log an ERROR message with exception and automatic class and method detection
     * 
     * @param message The message
     * @param t       The throwable
     */
    public void error(String message, Throwable t) {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        walker.walk(frames -> frames.skip(1).findFirst())
                .ifPresent(frame -> {
                    Logger logger = LoggerFactory.getLogger(frame.getDeclaringClass());
                    String prefix = "[" + frame.getMethodName() + "] ";
                    logger.error(prefix + message, t);
                });
    }
}
