package commonsos.controller.app.user;

import static commonsos.annotation.SyncObject.USERNAME_AND_EMAIL_ADDRESS;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.controller.app.AbstcactAppController;
import commonsos.service.UserService;
import commonsos.service.command.CreateUserTemporaryCommand;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;

@Synchronized(USERNAME_AND_EMAIL_ADDRESS)
public class CreateUserTemporaryController extends AbstcactAppController {

  @Inject Gson gson;
  @Inject UserService userService;

  @Override
  public CommonView handleApp(Request request, Response response) {
    CreateUserTemporaryCommand command = gson.fromJson(request.body(), CreateUserTemporaryCommand.class);
    userService.createAccountTemporary(command);
    
    return new CommonView();
  }
}
