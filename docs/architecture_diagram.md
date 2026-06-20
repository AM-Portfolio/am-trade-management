# AM Trade Management Architecture Diagram

This diagram maps out the data flow originating from the `am-trade-api` folder, tracing it down through the internal services and out to the connected databases and resource brokers.

```mermaid
graph TD
    %% Define Styles
    classDef client fill:#f9f,stroke:#333,stroke-width:2px;
    classDef apiLayer fill:#d4edda,stroke:#28a745,stroke-width:2px;
    classDef serviceLayer fill:#cce5ff,stroke:#007bff,stroke-width:2px;
    classDef persistenceLayer fill:#fff3cd,stroke:#ffc107,stroke-width:2px;
    classDef database fill:#e2e3e5,stroke:#6c757d,stroke-width:2px;
    classDef broker fill:#f8d7da,stroke:#dc3545,stroke-width:2px;

    Client((Frontend UI)):::client -->|REST Requests| Controllers

    subgraph AM Trade API [Module: am-trade-api]
        Controllers[Controllers Layer\n(Trade, Journal, Filters, etc.)]:::apiLayer
        ApiServices[API Services Layer\n(Workflow Orchestration)]:::apiLayer
        
        Controllers -->|Delegates mapping & validation| ApiServices
    end

    subgraph AM Trade Services [Module: am-trade-services]
        BusinessServices[Core Business Logic\n(Math, Cycles, Processing)]:::serviceLayer
        
        ApiServices -->|Hands off Domain Models| BusinessServices
    end

    subgraph AM Persistence [Module: am-trade-persistence]
        Repositories[Repository Layer\n(Mappers & Spring Data)]:::persistenceLayer
        
        BusinessServices -->|Translate to DB Entities| Repositories
        ApiServices -->|Direct basic saves| Repositories
    end

    Mongo[(MongoDB)]:::database
    Kafka[[Apache Kafka]]:::broker

    Repositories <-->|Read / Write BSON| Mongo
    BusinessServices -.->|Publish Metric Events| Kafka
```
