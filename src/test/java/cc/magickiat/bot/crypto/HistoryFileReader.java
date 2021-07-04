package cc.magickiat.bot.crypto;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HistoryFileReader {
    public static BarSeries readFile(String file, BotConfig config) throws IOException {
        List<BigDecimal> bdClose = new ArrayList<>();
        List<BigDecimal> bdHigh = new ArrayList<>();
        List<BigDecimal> bdLow = new ArrayList<>();
        List<BigDecimal> bdOpen = new ArrayList<>();
        List<BigDecimal> bdVolume = new ArrayList<>();
        List<ZonedDateTime> dateList = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(file))) {
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] data = line.split(",");

                ZoneId zoneId = config.getZoneId();
                Instant instant = Instant.ofEpochMilli(Long.parseLong(data[0]));
                dateList.add(ZonedDateTime.ofInstant(instant, zoneId));

                bdOpen.add(new BigDecimal(data[1]));
                bdHigh.add(new BigDecimal(data[2]));
                bdLow.add(new BigDecimal(data[3]));
                bdClose.add(new BigDecimal(data[4]));
                bdVolume.add(new BigDecimal(data[5]));
            }
        }

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
