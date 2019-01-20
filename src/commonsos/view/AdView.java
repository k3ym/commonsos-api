package commonsos.view;

import java.math.BigDecimal;
import java.time.Instant;

import commonsos.repository.entity.AdType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class AdView {
  private Long id;
  private Long communityId;
  private UserView createdBy;
  private String title;
  private String description;
  private BigDecimal points;
  private String location;
  private boolean own;
  private boolean payable;
  private Instant createdAt;
  private String photoUrl;
  private AdType type;
}