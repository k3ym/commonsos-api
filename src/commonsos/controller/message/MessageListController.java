package commonsos.controller.message;

import static java.lang.Long.parseLong;

import javax.inject.Inject;

import commonsos.controller.AfterLoginController;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.service.command.PaginationCommand;
import commonsos.util.PaginationUtil;
import commonsos.view.MessageListView;
import spark.Request;
import spark.Response;

public class MessageListController extends AfterLoginController {

  @Inject MessageService service;

  @Override protected MessageListView handleAfterLogin(User user, Request request, Response response) {
    PaginationCommand paginationCommand = PaginationUtil.getCommand(request);
    MessageListView view = service.messages(user, parseLong(request.params("id")), paginationCommand);
    return view;
  }
}
