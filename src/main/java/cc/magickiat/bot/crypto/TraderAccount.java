package cc.magickiat.bot.crypto;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;

public class TraderAccount {
    private static final Logger log = LoggerFactory.getLogger(TraderAccount.class);

    private final BinanceApiRestClient restClient;
    private final BotConfig config;
    private AssetBalance balanceEth;
    private AssetBalance balanceUsdt;

    public TraderAccount(BinanceApiRestClient restClient, BotConfig config) {
        this.restClient = restClient;
        this.config = config;

        refreshBalance();
    }

    public void refreshBalance() {
        Account account = restClient.getAccount();
        balanceEth = account.getAssetBalance("ETH");
        balanceUsdt = account.getAssetBalance("USDT");
    }

    public void printBalance() {
        log.info("--------------------");
        log.info("ETH: " + balanceEth.getFree());
        log.info("USDT: " + balanceUsdt.getFree());
        log.info("--------------------");
    }

    public boolean isNoCash() {
        return new BigDecimal(balanceUsdt.getFree()).compareTo(BigDecimal.ZERO) < 1;
    }

    public Num getBalanceEth() {
        return DecimalNum.valueOf(balanceEth.getFree());
    }

    public Num getBalanceUsdt() {
        return DecimalNum.valueOf(balanceUsdt.getFree());
    }

    public Num getAmountToBuy(Num closePrice) {
        Num amountUsdtToBuy = getBalanceUsdt().multipliedBy(config.getMarginLongPercent());
        return amountUsdtToBuy.dividedBy(closePrice);
    }
}
