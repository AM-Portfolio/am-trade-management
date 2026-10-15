package am.trade.common.models;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight metadata for opening the trade calendar on the user's most recent trade.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarLatestActivity {

    /** Calendar date of the newest trade entry (from {@code entryInfo.timestamp}). */
    private LocalDate lastTradeDate;

    /** Year of {@link #lastTradeDate}. */
    private int year;

    /** Month of {@link #lastTradeDate} (1–12). */
    private int month;
}
