package commonsos.controller.app.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.app.UserStatusUpdateCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.UserView;
import spark.Request;
import spark.Response;

public class UserStatusUpdateController extends AfterAppLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override
  public UserView handleAfterLogin(User user, Request request, Response response) {
    UserStatusUpdateCommand command = gson.fromJson(request.body(), UserStatusUpdateCommand.class);
    User updatedUser = userService.updateStatus(user, command);
    return userService.privateView(updatedUser);
  }
}
