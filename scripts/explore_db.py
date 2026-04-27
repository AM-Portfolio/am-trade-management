from pymongo import MongoClient
import json

uri = "mongodb://admin:sGwaiRdoaYtBiaqq4n9qaRkr@mongodb.munish.org:8888/trade_management?authSource=admin&directConnection=true"
client = MongoClient(uri)
db = client.get_database("trade_management")

results = {}

collections_to_check = {
    "users": ["userId", "id", "_id"],
    "portfolios": ["portfolioId", "id", "_id"],
    "favorite_filters": ["filterId", "id", "_id"],
    "journal_templates": ["templateId", "id", "_id"],
    "trades": ["tradeId", "userId", "portfolioId"]
}

# Also check for any common collection names in case my guesses are off
actual_collections = db.list_collection_names()
print(f"Collections found: {actual_collections}")

from datetime import datetime
from bson import ObjectId

class MongoJSONEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, datetime):
            return o.isoformat()
        if isinstance(o, ObjectId):
            return str(o)
        return super().default(o)

# ... inside the loop ...
for coll_name in actual_collections:
    coll = db[coll_name]
    count = coll.count_documents({})
    docs = list(coll.find().limit(5))
    results[coll_name] = {"count": count, "docs": docs}

output = []
for coll, data in results.items():
    output.append(f"\n--- Collection: {coll} (Total: {data['count']}) ---")
    if not data['docs']:
        output.append("No documents found.")
        continue
    for i, doc in enumerate(data['docs']):
        output.append(f"Doc {i+1}: {json.dumps(doc, indent=2, cls=MongoJSONEncoder)}")

with open("db_exploration.txt", "w") as f:
    f.write("\n".join(output))

print("Exploration complete. Results saved to db_exploration.txt")
