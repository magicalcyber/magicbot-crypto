package cc.magickiat.bot.crypto;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;

public class TraderAccount {
    private final BinanceApiRestClient restClient;
    private AssetBalance balanceBtc;
    private AssetBalance balanceUsdt;

    public TraderAccount(BinanceApiRestClient restClient) {
        this.restClient = restClient;

        refreshBalance();
    }

    public void refreshBalance() {
        Account account = restClient.getAccount();
        balanceBtc = account.getAssetBalance("BTC");
        balanceUsdt = account.getAssetBalance("USDT");
    }

    public boolean isNoCash() {
        return new BigDecimal(balanceUsdt.getFree()).compareTo(BigDecimal.ZERO) < 1;
    }

    public Num getBalanceBtc() {
        return DecimalNum.valueOf(balanceBtc.getFree());
    }

    public Num getBalanceUsdt() {
        return DecimalNum.valueOf(balanceUsdt.getFree());
    }


}
