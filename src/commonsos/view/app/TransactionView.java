package commonsos.view.app;

import java.math.BigDecimal;
import java.time.Instant;

import commonsos.view.CommonView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class TransactionView extends CommonView {
  private Boolean isFromAdmin;
  private PublicUserView remitter;
  private PublicUserView beneficiary;
  private BigDecimal amount;
  private String description;
  private Instant createdAt;
  private boolean completed;
  private boolean debit;
}
