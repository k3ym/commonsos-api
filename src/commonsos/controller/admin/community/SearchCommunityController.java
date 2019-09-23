package commonsos.controller.admin.community;

import javax.inject.Inject;

import commonsos.command.PaginationCommand;
import commonsos.controller.admin.AfterAdminLoginController;
import commonsos.repository.entity.Admin;
import commonsos.service.CommunityService;
import commonsos.util.PaginationUtil;
import commonsos.view.admin.CommunityListForAdminView;
import spark.Request;
import spark.Response;

public class SearchCommunityController extends AfterAdminLoginController {

  @Inject CommunityService communityService;

  @Override
  public CommunityListForAdminView handleAfterLogin(Admin admin, Request request, Response response) {
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    
    CommunityListForAdminView view = communityService.searchForAdmin(admin, paginationCommand);
    return view;
  }
}
