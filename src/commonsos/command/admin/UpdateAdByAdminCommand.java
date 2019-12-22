package commonsos.command.admin;

import java.math.BigDecimal;

import commonsos.repository.entity.AdType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class UpdateAdByAdminCommand {
  private Long id;
  private String title;
  private String status;
  private String description;
  private BigDecimal points;
  private String location;
  private AdType type;
}