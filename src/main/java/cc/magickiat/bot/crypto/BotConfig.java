package cc.magickiat.bot.crypto;

import com.binance.api.client.domain.market.CandlestickInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Properties;

public class BotConfig {
    private static final Logger log = LoggerFactory.getLogger(BotConfig.class);

    private static final String MARGIN_LONG_PERCENT = "margin.long.percent";
    private static final String TAKE_PROFIT_PERCENT = "take.profit.percent";
    private static final String STOP_LOSS_PERCENT = "stop.loss.percent";

    public static final String SYMBOL = "symbol";
    public static final String TIMEFRAME = "timeframe";
    private static BotConfig config;
    private Num marginLongPercent = DecimalNum.valueOf(0.1);
    private String symbol = "BTCUSDT";
    private String timeframe = "1h";
    private BigDecimal takeProfitPercent = new BigDecimal("0.05");
    private BigDecimal stopLossPercent = new BigDecimal("0.02");

    private BotConfig() {
        Properties properties = new Properties();
        try (InputStream is = new FileInputStream("bot.properties")) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (properties.containsKey(MARGIN_LONG_PERCENT)) {
            marginLongPercent = DecimalNum.valueOf(properties.getProperty(MARGIN_LONG_PERCENT));
        }

        if (properties.containsKey(SYMBOL)) {
            symbol = properties.getProperty(SYMBOL);
        }

        if (properties.containsKey(TIMEFRAME)) {
            timeframe = properties.getProperty(TIMEFRAME);
        }

        if (properties.containsKey(TAKE_PROFIT_PERCENT)) {
            takeProfitPercent = new BigDecimal(properties.getProperty(TAKE_PROFIT_PERCENT));
        }

        if (properties.containsKey(STOP_LOSS_PERCENT)) {
            stopLossPercent = new BigDecimal(properties.getProperty(STOP_LOSS_PERCENT));
        }

        log.info("--------------------------");
        log.info("Symbol: " + symbol);
        log.info("Timeframe: " + timeframe);
        log.info("Margin long percent: " + marginLongPercent);
        log.info("Take profit percent: " + takeProfitPercent.toPlainString());
        log.info("Stop loss percent: " + stopLossPercent.toPlainString());
        log.info("--------------------------");
    }

    public static BotConfig getInstance() {
        if (config == null) {
            config = new BotConfig();
        }
        return config;
    }

    public Num getMarginLongPercent() {
        return marginLongPercent;
    }

    public CandlestickInterval getTimeframe() {
        return Arrays.stream(CandlestickInterval.values())
                .sequential()
                .filter(e -> e.getIntervalId().equalsIgnoreCase(timeframe))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid timeframe config: " + timeframe));
    }

    public String getSymbol() {
        return symbol;
    }

    public String getBinanceApiKey() {
        return System.getenv("BINANCE_API_KEY");
    }

    public String getBinanceApiSecret() {
        return System.getenv("BINANCE_API_SECRET");
    }

    public ZoneId getZoneId() {
        return ZoneId.of("Asia/Bangkok");
    }

    public BigDecimal getTakeProfitPercent() {
        return takeProfitPercent;
    }

    public BigDecimal getStopLossPercent() {
        return stopLossPercent;
    }
}
