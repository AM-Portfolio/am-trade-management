from polyfactory.factories.pydantic_factory import ModelFactory
import generated_models
import json

class TradeModelFactory(ModelFactory[generated_models.TradeModel]):
    __model__ = generated_models.TradeModel

def main():
    print("Automatically generating mock data from the parsed Swagger schema models...\n")
    
    # Generate random mock data
    mock_trade = TradeModelFactory.build()
    
    # Print the resulting JSON
    print(mock_trade.model_dump_json(indent=2))
    print("\n✅ Successfully generated perfectly typed JSON data using the Enums read from Swagger!")

if __name__ == "__main__":
    main()
