package commonsos.controller.user;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isBlank;

import javax.inject.Inject;

import commonsos.controller.Controller;
import commonsos.repository.user.User;
import commonsos.service.user.UserService;
import commonsos.util.UserUtil;
import spark.Request;
import spark.Response;

public class UserController extends Controller {

  @Inject private UserService userService;
  @Inject private UserUtil userUtil;

  @Override public Object handle(User user, Request request, Response response) {
    if (isBlank(request.params("id"))) return userService.privateView(user);
    
    Long id = parseLong(request.params("id"));
    User requestedUser = userService.user(id);
    
    if (userUtil.isAdminOfUser(user, requestedUser)) return userService.privateView(user, id);
    else return userService.view(id);
  }
}
