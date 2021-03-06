package commonsos.controller.user;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.annotation.ReadOnly;
import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.service.command.PaginationCommand;
import commonsos.util.PaginationUtil;
import commonsos.view.UserListView;
import spark.Request;
import spark.Response;

@ReadOnly
public class UserSearchController extends AfterLoginController {

  @Inject UserService service;

  @Override
  public UserListView handleAfterLogin(User user, Request request, Response response) {
    String communityId = request.queryParams("communityId");
    if (isEmpty(communityId)) throw new BadRequestException("communityId is required");
    if (!NumberUtils.isParsable(communityId)) throw new BadRequestException("invalid communityId");
    
    String q = request.queryParams("q");
    
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);

    UserListView view = service.searchUsers(user, parseLong(communityId), q, paginationCommand);
    return view;
  }
}
