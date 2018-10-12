package commonsos.domain.ad;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class AdUpdateCommand {
  private Long id;
  private String title;
  private String description;
  private BigDecimal amount;
  private String location;
  private AdType type;
}