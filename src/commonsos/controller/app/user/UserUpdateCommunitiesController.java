package commonsos.controller.app.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.app.UserUpdateCommunitiesCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.UserView;
import spark.Request;
import spark.Response;

public class UserUpdateCommunitiesController extends AfterAppLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override
  protected UserView handleAfterLogin(User user, Request request, Response response) {
    UserUpdateCommunitiesCommand command = gson.fromJson(request.body(), UserUpdateCommunitiesCommand.class);
    User updatedUser = userService.updateUserCommunities(user, command);
    return userService.privateView(updatedUser);
  }
}
