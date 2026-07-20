// =============================================================
// am-trade-management — Test Data Seed Script
// Run with: mongosh "mongodb://localhost:27017/trade" seed-data.js
// Or against dev: mongosh "mongodb+srv://..." seed-data.js
//
// PURPOSE: Creates 10 realistic test users, each with 2 portfolios
// and 200 trades per portfolio (2000 trades total).
// This matches real production-level data volumes.
// =============================================================

const SYMBOLS = ['RELIANCE', 'TCS', 'INFY', 'HDFCBANK', 'ICICIBANK', 'WIPRO', 'AXISBANK', 'SBIN', 'BAJFINANCE', 'MARUTI'];
const EXCHANGES = ['NSE', 'BSE'];
const TRADE_STATUSES = ['WIN', 'LOSS', 'OPEN', 'BREAK_EVEN'];

// --- Helpers ---
function uuid() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

function randomBetween(min, max) {
  return Math.random() * (max - min) + min;
}

function randomPrice() {
  return parseFloat(randomBetween(100, 5000).toFixed(2));
}

function randomDate(daysBack) {
  const d = new Date();
  d.setDate(d.getDate() - Math.floor(Math.random() * daysBack));
  return d;
}

function makeTrade(portfolioId, ownerId, status) {
  const symbol = SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)];
  const entryPrice = randomPrice();
  const quantity = Math.floor(randomBetween(10, 500));
  const entryDate = randomDate(365);

  const priceDelta = (Math.random() - 0.45) * entryPrice * 0.1; // slight positive bias
  const exitPrice = parseFloat((entryPrice + priceDelta).toFixed(2));

  const totalEntryValue = parseFloat((entryPrice * quantity).toFixed(2));
  const totalExitValue = parseFloat((exitPrice * quantity).toFixed(2));
  const profitLoss = parseFloat((totalExitValue - totalEntryValue).toFixed(2));

  const resolvedStatus = status || (profitLoss > 0 ? 'WIN' : profitLoss < 0 ? 'LOSS' : 'BREAK_EVEN');

  const exitDate = new Date(entryDate.getTime() + Math.floor(randomBetween(1, 30)) * 24 * 60 * 60 * 1000);
  const isOpen = resolvedStatus === 'OPEN';

  return {
    _id: uuid(),
    tradeId: uuid(),
    portfolioId: portfolioId,
    userId: ownerId,
    symbol: symbol,
    instrumentInfo: {
      symbol: symbol,
      exchange: EXCHANGES[Math.floor(Math.random() * EXCHANGES.length)],
      segment: 'EQ',
      series: 'EQ',
    },
    tradePositionType: Math.random() > 0.2 ? 'LONG' : 'SHORT',
    status: resolvedStatus,
    entryInfo: {
      timestamp: entryDate,
      price: NumberDecimal(entryPrice.toString()),
      quantity: quantity,
      totalValue: NumberDecimal(totalEntryValue.toString()),
      fees: NumberDecimal(parseFloat(randomBetween(10, 80).toFixed(2)).toString()),
    },
    exitInfo: isOpen ? null : {
      timestamp: exitDate,
      price: NumberDecimal(exitPrice.toString()),
      quantity: quantity,
      totalValue: NumberDecimal(totalExitValue.toString()),
      fees: NumberDecimal(parseFloat(randomBetween(10, 80).toFixed(2)).toString()),
    },
    metrics: isOpen ? null : {
      profitLoss: NumberDecimal(profitLoss.toString()),
      profitLossPercentage: NumberDecimal(((profitLoss / totalEntryValue) * 100).toFixed(4).toString()),
      returnOnEquity: NumberDecimal(((profitLoss / totalEntryValue) * 100).toFixed(4).toString()),
      holdingTimeDays: NumberLong(Math.floor(randomBetween(1, 30))),
      holdingTimeHours: NumberLong(Math.floor(randomBetween(0, 23))),
      holdingTimeMinutes: NumberLong(Math.floor(randomBetween(0, 59))),
      riskAmount: NumberDecimal('0'),
      rewardAmount: NumberDecimal(profitLoss > 0 ? profitLoss.toString() : '0'),
      riskRewardRatio: NumberDecimal('0'),
    },
    tradeDate: entryDate,
    notes: `SPT seed trade for ${symbol}`,
    tags: [symbol, resolvedStatus],
  };
}

function makePortfolio(portfolioId, ownerId, name, tradeIds) {
  const winCount = Math.floor(tradeIds.length * 0.55);
  return {
    _id: portfolioId,
    portfolioId: portfolioId,
    name: name,
    description: `SPT test portfolio — ${name}`,
    ownerId: ownerId,
    active: true,
    currency: 'INR',
    initialCapital: NumberDecimal('500000'),
    currentCapital: NumberDecimal(randomBetween(450000, 650000).toFixed(2).toString()),
    createdDate: randomDate(400),
    lastUpdatedDate: new Date(),
    trades: tradeIds,
    winningTrades: tradeIds.slice(0, winCount),
    losingTrades: tradeIds.slice(winCount),
    metrics: {
      totalTrades: tradeIds.length,
      winningTrades: winCount,
      losingTrades: tradeIds.length - winCount,
      breakEvenTrades: 0,
      openPositions: 5,
      winRate: NumberDecimal(((winCount / tradeIds.length) * 100).toFixed(4).toString()),
      lossRate: NumberDecimal((((tradeIds.length - winCount) / tradeIds.length) * 100).toFixed(4).toString()),
      profitFactor: NumberDecimal('1.45'),
      expectancy: NumberDecimal('1234.56'),
      totalValue: NumberDecimal('10000000'),
      netProfitLoss: NumberDecimal('85000'),
      netProfitLossPercentage: NumberDecimal('1.7'),
    },
  };
}

// =============================================================
// MAIN SEEDING LOGIC
// =============================================================

print("=== am-trade-management SPT Data Seeding ===");
print("Clearing old SPT seed data...");

// Clear old SPT data (identified by notes field)
db.trade_details.deleteMany({ notes: { $regex: /^SPT seed trade/ } });
db.portfolio_trades.deleteMany({ description: { $regex: /^SPT test portfolio/ } });

const NUM_USERS = 10;
const PORTFOLIOS_PER_USER = 2;
const TRADES_PER_PORTFOLIO = 200;

const createdUsers = [];

for (let u = 0; u < NUM_USERS; u++) {
  const ownerId = `spt-user-${u + 1}`;
  const userPortfolioIds = [];

  for (let p = 0; p < PORTFOLIOS_PER_USER; p++) {
    const portfolioId = `spt-portfolio-${u + 1}-${p + 1}`;
    const trades = [];
    const tradeIds = [];

    for (let t = 0; t < TRADES_PER_PORTFOLIO; t++) {
      // Last 10 trades per portfolio are open positions — realistic scenario
      const status = t >= TRADES_PER_PORTFOLIO - 10 ? 'OPEN' : null;
      const trade = makeTrade(portfolioId, ownerId, status);
      trades.push(trade);
      tradeIds.push(trade.tradeId);
    }

    db.trade_details.insertMany(trades);
    db.portfolio_trades.insertOne(makePortfolio(portfolioId, ownerId, `SPT Portfolio ${u + 1}-${p + 1}`, tradeIds));

    userPortfolioIds.push(portfolioId);
    print(`  Created portfolio ${portfolioId} with ${TRADES_PER_PORTFOLIO} trades`);
  }
  createdUsers.push({ ownerId, portfolioIds: userPortfolioIds });
}

print("\n=== SEEDING COMPLETE ===");
print(`Created ${NUM_USERS} users, ${NUM_USERS * PORTFOLIOS_PER_USER} portfolios, ${NUM_USERS * PORTFOLIOS_PER_USER * TRADES_PER_PORTFOLIO} trades`);
print("\nCopy the following into your .env file before running k6:\n");

for (const u of createdUsers) {
  print(`USER_ID=${u.ownerId}  PORTFOLIO_ID_1=${u.portfolioIds[0]}  PORTFOLIO_ID_2=${u.portfolioIds[1]}`);
}

print("\n--- Quick Verify ---");
print("trade_details count:", db.trade_details.countDocuments({ notes: { $regex: /^SPT seed/ } }));
print("portfolio_trades count:", db.portfolio_trades.countDocuments({ description: { $regex: /^SPT test/ } }));
