/**
 * deduplicate-trade-ids.js
 *
 * Purpose: Remove duplicate tradeId documents from the trade_details collection
 *          so that the unique index on tradeId can be (re-)created by the application.
 *
 * Background: During early load tests, trades were inserted without a unique index
 *             on tradeId, creating duplicate entries. The application's MongoIndexConfig
 *             catches this failure and falls back to a non-unique index, but the
 *             unique constraint cannot be enforced until duplicates are removed.
 *
 * Instructions:
 *   1. Connect to the target MongoDB instance (e.g., via mongosh or MongoDB Compass)
 *   2. Switch to the correct database: use trade
 *   3. Run this script: load("deduplicate-trade-ids.js")
 *
 * IMPORTANT: Run in DRY_RUN mode first (dryRun = true) to see what would be deleted.
 *            Only set dryRun = false after reviewing the output and confirming it is safe.
 *
 * Environment: am-apps-dev ONLY. Never run against staging or production.
 */

const DRY_RUN = true; // SAFETY: Set to false to actually delete
const COLLECTION = "trade_details";

print(`\n=== Trade Deduplication Script ===`);
print(`Database:   ${db.getName()}`);
print(`Collection: ${COLLECTION}`);
print(`Dry Run:    ${DRY_RUN}\n`);

if (DRY_RUN) {
    print("*** DRY RUN MODE — no data will be modified ***\n");
}

// Step 1: Find all duplicate tradeIds
const duplicates = db[COLLECTION].aggregate([
    {
        $group: {
            _id: "$tradeId",
            count: { $sum: 1 },
            docIds: { $push: "$_id" }
        }
    },
    {
        $match: { count: { $gt: 1 } }
    },
    {
        $sort: { count: -1 }
    }
]).toArray();

if (duplicates.length === 0) {
    print("No duplicate tradeIds found. The unique index can be safely created.");
    print("\nNext step: Restart the application pod to trigger MongoIndexConfig.createIndexes()");
    print("  kubectl rollout restart deployment/am-trade-management -n am-apps-dev");
} else {
    print(`Found ${duplicates.length} tradeId(s) with duplicates:\n`);

    let totalToDelete = 0;
    duplicates.forEach(dup => {
        print(`  tradeId: ${dup._id}  |  ${dup.count} copies  |  keeping: ${dup.docIds[0]}  |  deleting: ${dup.count - 1}`);
        totalToDelete += dup.count - 1;
    });

    print(`\nTotal documents to remove: ${totalToDelete}`);

    if (!DRY_RUN) {
        print("\nDeleting duplicates (keeping the first inserted document per tradeId)...");

        let deleted = 0;
        duplicates.forEach(dup => {
            const idsToDelete = dup.docIds.slice(1);
            const result = db[COLLECTION].deleteMany({ _id: { $in: idsToDelete } });
            deleted += result.deletedCount;
        });

        print(`\nDeleted ${deleted} duplicate documents.`);
        print("\nNext step: Restart the application pod to trigger index creation:");
        print("  kubectl rollout restart deployment/am-trade-management -n am-apps-dev");
        print("\nThen verify the unique index was created:");
        print(`  db.${COLLECTION}.getIndexes()`);
    } else {
        print(`\nTo apply: set DRY_RUN = false and re-run this script.`);
    }
}
