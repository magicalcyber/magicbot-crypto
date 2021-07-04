package cc.magickiat.bot.crypto;

import com.binance.api.client.domain.market.Candlestick;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class TradeHistory {
    private final List<Candlestick> candlestickBars;

    public TradeHistory(List<Candlestick> candlestickBars) {
        if (candlestickBars == null || candlestickBars.isEmpty()) {
            throw new IllegalArgumentException("Candlestick should not empty");
        }
        this.candlestickBars = candlestickBars;
    }

    public BarSeries getBarSeries() {
        List<BigDecimal> bdClose = new ArrayList<>(candlestickBars.size());
        List<BigDecimal> bdHigh = new ArrayList<>(candlestickBars.size());
        List<BigDecimal> bdLow = new ArrayList<>(candlestickBars.size());
        List<BigDecimal> bdOpen = new ArrayList<>(candlestickBars.size());
        List<BigDecimal> bdVolume = new ArrayList<>(candlestickBars.size());
        List<ZonedDateTime> dateList = new ArrayList<>(candlestickBars.size());

        ZoneId zoneId = ZoneId.of("Asia/Bangkok");
        candlestickBars.forEach(e -> {
            Instant instant = Instant.ofEpochMilli(e.getCloseTime());
            dateList.add(ZonedDateTime.ofInstant(instant, zoneId));
            bdClose.add(new BigDecimal(e.getClose()));
            bdHigh.add(new BigDecimal(e.getHigh()));
            bdLow.add(new BigDecimal(e.getLow()));
            bdOpen.add(new BigDecimal(e.getOpen()));
            bdVolume.add(new BigDecimal(e.getVolume()));
        });

        BarSeries series = new BaseBarSeriesBuilder().build();
        for (int i = 0; i < dateList.size(); i++) {
            series.addBar(dateList.get(i),
                    bdOpen.get(i),
                    bdHigh.get(i),
                    bdLow.get(i),
                    bdClose.get(i),
                    bdVolume.get(i));
        }
        return series;
    }
}
