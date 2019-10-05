package commonsos.view;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class EthBalanceView extends CommonView {
  private BigDecimal balance;
}
