package cc.magickiat.bot.crypto;

import com.binance.api.client.domain.event.CandlestickEvent;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TradeStrategyTest {

    @Test
    public void testBuySignal() throws IOException {
        // Given
        BotConfig config = BotConfig.getInstance();
        BarSeries series = HistoryFileReader.readFile("candlestick-history/btcusdt-1h-test-buy-signal.csv", config);

        TradeStrategy strategy = new TradeStrategy(series, config);

        // When
        CandlestickEvent event = new CandlestickEvent();
        event.setBarFinal(true);
        event.setCloseTime(new Date().getTime());
        event.setOpen("35629.64000000");
        event.setHigh("35899.54000000");
        event.setLow("35500.00000000");
        event.setClose("35832.98000000");
        event.setVolume("2340.54572400");
        TradeAction action = strategy.onCandlestickEvent(event);

        // Then
        assertEquals(TradeAction.BUY, action);
    }

    @Test
    public void testSellSignal() throws IOException {
        // Given
        // Given
        BotConfig config = BotConfig.getInstance();
        BarSeries series = HistoryFileReader.readFile("candlestick-history/btcusdt-1h-test-buy-signal.csv", config);

        TradeStrategy strategy = new TradeStrategy(series, config);
        // When
        CandlestickEvent event = new CandlestickEvent();
        event.setBarFinal(true);
        event.setCloseTime(new Date().getTime());
        event.setOpen("35629.64000000");
        event.setHigh("35899.54000000");
        event.setLow("35500.00000000");
        event.setClose("35832.98000000");
        event.setVolume("2340.54572400");
        TradeAction action = strategy.onCandlestickEvent(event);
        assertEquals(TradeAction.BUY, action);
        strategy.setInPosition(true);

        // When
        event = new CandlestickEvent();
        event.setBarFinal(false);
        event.setCloseTime(new Date().getTime());
        event.setOpen("35832.98000000");
        event.setHigh("37625.98000000");
        event.setLow("35800.98000000");
        event.setClose("37625.98000000");
        event.setVolume("2340.54572400");
        action = strategy.onCandlestickEvent(event);

        // Then
        assertEquals(TradeAction.SELL, action);
    }
}