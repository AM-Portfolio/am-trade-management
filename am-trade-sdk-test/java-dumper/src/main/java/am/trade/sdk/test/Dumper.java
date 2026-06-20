package am.trade.sdk.test;

import am.trade.models.document.Trade;
import am.trade.models.enums.TradeStatus;
import am.trade.models.enums.TradeType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class Dumper {
    public static void main(String[] args) {
        try {
            // Create a full populated Trade object
            Trade trade = new Trade();
            trade.setId("trade-test-uuid-1234");
            trade.setPortfolioId("portfolio-uuid-5678");
            trade.setSymbol("AAPL");
            trade.setTradeType(TradeType.BUY);
            trade.setQuantity(100.0);
            trade.setEntryPrice(150.25);
            trade.setEntryDate(LocalDateTime.parse("2023-10-27T10:00:00"));
            trade.setExitPrice(155.50);
            trade.setExitDate(LocalDateTime.parse("2023-10-28T14:30:00"));
            trade.setStatus(TradeStatus.WIN);
            trade.setNotes("Test trade for SDK compatibility");
            trade.setPnl(525.0);
            trade.setPnlPercentage(3.5);

            // Serialize to JSON
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                    .setPrettyPrinting()
                    .create();
            
            String json = gson.toJson(trade);
            
            // Write to file
            try (FileWriter writer = new FileWriter("../trade_sample.json")) {
                writer.write(json);
                System.out.println("Successfully dumped trade_sample.json");
            }

        } catch (IOException e) {
            System.err.println("Failed to write JSON: " + e.getMessage());
            System.exit(1);
        }
    }

    // Simple adapter to match what the backend likely does (default string format)
    static class LocalDateTimeTypeAdapter extends com.google.gson.TypeAdapter<LocalDateTime> {
        @Override
        public void write(com.google.gson.stream.JsonWriter out, LocalDateTime value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public LocalDateTime read(com.google.gson.stream.JsonReader in) throws IOException {
            return LocalDateTime.parse(in.nextString());
        }
    }
}
