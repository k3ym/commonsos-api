package commonsos.service.command;

import commonsos.repository.entity.SortType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class PaginationCommand {
  private int page;
  private int size;
  private SortType sort;
}
