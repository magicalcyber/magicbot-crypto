package cc.magickiat.bot.crypto;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.market.Candlestick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public class Trader {
    private static final Logger log = LoggerFactory.getLogger(Trader.class);

    public static void main(String[] args) {
        log.info("=================================");
        log.info("\tMagicBot - Crypto");
        log.info("=================================");
        log.info("Load config...");
        BotConfig config = BotConfig.getInstance();

        // Prepare Binance client
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(config.getBinanceApiKey(), config.getBinanceApiSecret());
        BinanceApiRestClient restClient = factory.newRestClient();

        // Get balances
        final TraderAccount account = new TraderAccount(restClient, config);
        // show balance
        account.printBalance();

        if (account.isNoCash()) {
            log.warn("Please refill USDT to your port");
            return;
        }

        // Init candlesticks for strategy
        List<Candlestick> candlestickBars = restClient.getCandlestickBars(config.getSymbol().toUpperCase(), config.getTimeframe());
        candlestickBars.remove(candlestickBars.size() - 1); // remove last candle that not closed

        TradeHistory tradeHistory = new TradeHistory(candlestickBars);
        TradeStrategy strategy = new TradeStrategy(tradeHistory.getBarSeries(), config);

        BinanceApiWebSocketClient webSocketKLine = factory.newWebSocketClient();
        final Closeable closeable = webSocketKLine.onCandlestickEvent(config.getSymbol().toLowerCase(), config.getTimeframe(), e -> {
                    log.debug(String.format("Candlestick: Open time: %s, Close price: %s", e.getOpenTime(), e.getClose()));
                    TradeAction action = strategy.onCandlestickEvent(e);
                    switch (action) {
                        case BUY: {
                            log.info(">>> BUY Signal@ " + e);
                            account.refreshBalance();

                            Num amountBtc = account.getAmountBtcToBuy(DecimalNum.valueOf(e.getClose()));
                            NewOrderResponse newOrderResponse = restClient.newOrder(NewOrder.marketBuy(config.getSymbol().toUpperCase(), amountBtc.toString()));
                            log.info(newOrderResponse.toString());

                            strategy.setInPosition(newOrderResponse.getStatus() == OrderStatus.FILLED);
                            account.refreshBalance();
                            account.printBalance();
                            break;
                        }
                        case SELL: {
                            log.info(">>> SELL Signal@ " + e);
                            account.refreshBalance();

                            NewOrderResponse newOrderResponse = restClient.newOrder(NewOrder.marketSell(config.getSymbol().toUpperCase(), account.getBalanceBtc().toString()));
                            log.info(newOrderResponse.toString());

                            if (newOrderResponse.getStatus() == OrderStatus.FILLED) {
                                strategy.setInPosition(false);
                                account.refreshBalance();
                                account.printBalance();
                            }

                            break;
                        }
                        default: {
                            log.debug("Do nothing");
                        }
                    }
                }
        );

        // graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("Releasing resources");
                closeable.close();
                log.info("Released resources");
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }));

    }
}
