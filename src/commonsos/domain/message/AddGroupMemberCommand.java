package commonsos.domain.message;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter @Setter @Accessors(chain=true) @EqualsAndHashCode @ToString
public class AddGroupMemberCommand {
  Long threadId;
  List<Long> memberIds;
}
