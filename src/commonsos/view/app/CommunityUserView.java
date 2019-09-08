package commonsos.view.app;

import java.math.BigDecimal;
import java.time.Instant;

import commonsos.view.CommonView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class CommunityUserView extends CommonView {
  private Long id;
  private String name;
  private Long adminUserId;
  private String description;
  private String tokenSymbol;
  private BigDecimal balance;
  private String photoUrl;
  private String coverPhotoUrl;
  private BigDecimal transactionFee;
  private Instant walletLastViewTime;
  private Instant adLastViewTime;
  private Instant notificationLastViewTime;
}
