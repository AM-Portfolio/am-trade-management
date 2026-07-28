# Latency Optimization Notes: From Timeouts to SLA Compliance

This document breaks down the journey of investigating and resolving the severe latency issues we encountered during load testing (SPT). We started with a 99.82% failure rate (mostly timeouts) and brought it down to a 0.00% failure rate with an average latency of ~3 seconds—all without relying on an external Redis cache.

Here is a step-by-step, easy-to-understand explanation of what went wrong, why it happened, and how we fixed it.

---

## The Big Picture: What Was the Problem?

When we ran the load test with 100 concurrent Virtual Users (VUs), the system practically froze. Requests were taking over 500ms on average to simply fail, and many requests took so long that they just timed out completely. 

A system slowdown under load usually stems from one of three bottlenecks:
1. **Database (I/O)**: The database is locked up or doing too much work.
2. **Network/External Calls**: We are waiting too long for other systems to reply.
3. **Compute/CPU**: The server itself is doing too much heavy math or data processing over and over.

As we investigated, we found that our application was actually suffering from **all three** of these issues simultaneously!

---

## 1. The Database Issue: The `$addToSet` Bottleneck

### What was wrong?
In `TradeProcessingServiceImpl.java`, every time a trade was added or updated, we were pushing it to MongoDB using a command called `$addToSet`. 

### The Logic behind the problem
`$addToSet` is a command that says: "Look through this entire array of items, and if my item isn't in there, add it to the end." 
When a portfolio only has 10 trades, this is fast. But as a portfolio grows to 1,000 or 10,000 trades, MongoDB has to scan the *entire* array of 10,000 items *every single time* a new trade comes in. Worse, while MongoDB is doing this scan, it puts a **lock** on the document. When 100 users try to add trades at the exact same time, they all end up waiting in a massive line for the database to finish scanning and unlock the document.

### How we solved it
We changed `$addToSet` to `$push`. 
`$push` is extremely simple: it just says "Append this item to the end of the array." It doesn't scan the existing items, making it an **O(1) operation** (constant time) instead of an **O(N) operation** (time increases as the array grows). This completely eliminated the database lockup.

---

## 2. The Network Issue: The "Cache Stampede"

### What was wrong?
Our application fetches current market prices from an external API (`MarketDataApiClient.java`). We had a basic cache in place, but under heavy load, it failed due to a phenomenon known as a **Cache Stampede** (or Thundering Herd).

### The Logic behind the problem
Imagine a popular stock symbol expires from the cache. Suddenly, 100 concurrent users ask the system for the price of that stock. 
1. All 100 requests check the cache at the exact same millisecond. 
2. All 100 requests see that the cache is empty (a "cache miss").
3. Therefore, **all 100 requests independently call the external API** to fetch the exact same price.
This overwhelms both our server's network connections and the external API, causing massive slowdowns and timeouts.

### How we solved it
We introduced **Double-Checked Locking** using a `synchronized` block. 
Now, when 100 requests see an empty cache, they line up. The *first* request goes through, fetches the price from the external API, and places it in the cache. The remaining 99 requests then check the cache *again*, find the newly added price, and return immediately without ever making a network call.

---

## 3. The Compute Issue: Repeating Heavy Math

### What was wrong?
Our analytics endpoints (`getYearlyHeatmap` and `getTradeDetailsByTimePeriod`) were recalculating heavy statistics every single time a user requested them. 

### The Logic behind the problem
If a user looks at their Yearly Heatmap, the server pulls all their trades and crunches the numbers. If they refresh the page a second later, the server pulls all the trades and does all that heavy math *all over again*, even though their trades haven't changed! Under load, this maxed out the server's CPU.

### How we solved it (Without Redis!)
You specifically requested that we reduce latency *without* using Redis. Redis is a great tool, but it adds architectural complexity (you have to host and maintain a separate Redis server).

Instead, we used **Caffeine**, which is a high-performance, in-memory caching library that lives *inside* our Java application's JVM memory.
1. **Created `CaffeineCacheConfig.java`**: We set up a local cache that holds up to 5,000 items and expires them after 5 minutes.
2. **Added `@Cacheable`**: We placed this annotation on the heavy analytics methods. Now, the math is only done once. If the same user asks for the same data again, the server instantly returns the pre-calculated result from local memory.
3. **Added `@CacheEvict`**: Caches are useless if they serve stale, outdated data. We added this annotation to the `addTrade`, `updateTrade`, and `deleteTrade` methods. This ensures that the moment a user modifies a trade, their specific cached analytics are immediately wiped out. The next time they ask for their heatmap, the server will calculate fresh, accurate numbers.

---

## Summary

By taking a holistic approach and fixing the bottlenecks at the **Database**, **Network**, and **Compute** layers, we successfully transformed the application. 

Instead of throwing hardware at the problem (like adding a Redis server), we fundamentally optimized the code's logic. We eliminated O(N) database scans, stopped network cache stampedes, and prevented redundant math calculations. As a result, the API easily handled the load test, completely eliminating timeouts and bringing the average latency down to a stable ~3 seconds.
