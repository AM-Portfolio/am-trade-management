package am.trade.models.shared;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import am.trade.models.shared.TradeModel.BasicInfo;
import am.trade.models.shared.TradeModel.Charges;
import am.trade.models.shared.TradeModel.Financials;

/**
 * Custom deserializer for TradeModel to handle nested objects properly
 */
public class TradeModelDeserializer extends StdDeserializer<TradeModel> {
    
    private static final long serialVersionUID = 1L;

    public TradeModelDeserializer() {
        this(null);
    }

    public TradeModelDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public TradeModel deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectNode root = jp.getCodec().readTree(jp);
        
        // Create a new TradeModel
        TradeModel tradeModel = new TradeModel();
        
        // Deserialize BasicInfo
        if (root.has("basicInfo")) {
            JsonNode basicInfoNode = root.get("basicInfo");
            BasicInfo basicInfo = jp.getCodec().treeToValue(basicInfoNode, BasicInfo.class);
            tradeModel.setBasicInfo(basicInfo);
        }
        
        // Deserialize InstrumentInfo
        if (root.has("instrumentInfo")) {
            JsonNode instrumentInfoNode = root.get("instrumentInfo");
            InstrumentInfo instrumentInfo = jp.getCodec().treeToValue(instrumentInfoNode, InstrumentInfo.class);
            tradeModel.setInstrumentInfo(instrumentInfo);
        }
        
        // Deserialize ExecutionInfo
        if (root.has("executionInfo")) {
            JsonNode executionInfoNode = root.get("executionInfo");
            ExecutionInfo executionInfo = jp.getCodec().treeToValue(executionInfoNode, ExecutionInfo.class);
            tradeModel.setExecutionInfo(executionInfo);
        }
        
        // Deserialize FnOInfo
        if (root.has("fnoInfo")) {
            JsonNode fnoInfoNode = root.get("fnoInfo");
            FnOInfo fnoInfo = jp.getCodec().treeToValue(fnoInfoNode, FnOInfo.class);
            tradeModel.setFnoInfo(fnoInfo);
        }
        
        // Deserialize Charges
        if (root.has("charges")) {
            JsonNode chargesNode = root.get("charges");
            Charges charges = jp.getCodec().treeToValue(chargesNode, Charges.class);
            tradeModel.setCharges(charges);
        }
        
        // Deserialize Financials
        if (root.has("financials")) {
            JsonNode financialsNode = root.get("financials");
            Financials financials = jp.getCodec().treeToValue(financialsNode, Financials.class);
            tradeModel.setFinancials(financials);
        }
        
        return tradeModel;
    }
}
