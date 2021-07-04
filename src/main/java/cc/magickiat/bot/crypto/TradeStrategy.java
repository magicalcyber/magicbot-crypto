package cc.magickiat.bot.crypto;

import com.binance.api.client.domain.event.CandlestickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;

public class TradeStrategy {
    private static final Logger log = LoggerFactory.getLogger(TradeStrategy.class);

    private final BarSeries barSeries;
    private final EMAIndicator ema50;
    private final BotConfig botConfig;

    private boolean inPosition = false;
    private Num takeProfitPrice;
    private Num stopLossPrice;

    public TradeStrategy(BarSeries barSeries, BotConfig botConfig) {
        this.barSeries = barSeries;
        this.botConfig = botConfig;

        ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
        ema50 = new EMAIndicator(closePrice, 50);
    }


    public TradeAction onCandlestickEvent(CandlestickEvent event) {
        if (event.getBarFinal()) {
            Instant instant = Instant.ofEpochMilli(event.getCloseTime());
            ZonedDateTime closeTime = ZonedDateTime.ofInstant(instant, botConfig.getZoneId());
            barSeries.addBar(closeTime,
                    new BigDecimal(event.getOpen()),
                    new BigDecimal(event.getHigh()),
                    new BigDecimal(event.getLow()),
                    new BigDecimal(event.getClose()),
                    new BigDecimal(event.getVolume()));

            // check buy signal
            Bar barCurrent = barSeries.getBar(barSeries.getEndIndex());
            Bar barPrev1 = barSeries.getBar(barSeries.getEndIndex() - 1);
            Bar barPrev2 = barSeries.getBar(barSeries.getEndIndex() - 2);

            Num ema50Current = ema50.getValue(barSeries.getEndIndex());
            Num ema50Prev1 = ema50.getValue(barSeries.getEndIndex() - 1);
            Num ema50Prev2 = ema50.getValue(barSeries.getEndIndex() - 2);

            boolean buySignal = barPrev2.getClosePrice().isLessThan(ema50Prev2)
                    && barPrev1.getClosePrice().isGreaterThan(ema50Prev1)
                    && barCurrent.getClosePrice().isGreaterThan(ema50Current);

            if (buySignal) {
                takeProfitPrice = barCurrent.getClosePrice().plus(
                        barCurrent.getClosePrice().multipliedBy(DecimalNum.valueOf(botConfig.getTakeProfitPercent())));

                stopLossPrice = barCurrent.getClosePrice().minus(
                        barCurrent.getClosePrice().multipliedBy(DecimalNum.valueOf(botConfig.getStopLossPercent())));

                return TradeAction.BUY;
            }
        }

        // check sell signal
        if (inPosition) {
            DecimalNum currentPrice = DecimalNum.valueOf(event.getClose());
            if (currentPrice.isGreaterThanOrEqual(takeProfitPrice)
                    || currentPrice.isLessThanOrEqual(stopLossPrice)) {
                return TradeAction.SELL;
            }
        }

        return TradeAction.DO_NOTHING;
    }

    public void setInPosition(boolean inPosition) {
        log.info("> Set in_position = " + inPosition);
        this.inPosition = inPosition;
    }
}
