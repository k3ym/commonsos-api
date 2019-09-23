package commonsos.command.app;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain=true)
public class AppLoginCommand {
  private String username;
  private String password;
}