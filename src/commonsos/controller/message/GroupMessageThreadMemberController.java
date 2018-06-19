package commonsos.controller.message;

import com.google.gson.Gson;
import commonsos.controller.Controller;
import commonsos.domain.auth.User;
import commonsos.domain.message.AddGroupMemberCommand;
import commonsos.domain.message.MessageService;
import commonsos.domain.message.MessageThreadView;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

public class GroupMessageThreadMemberController extends Controller {

  @Inject Gson gson;
  @Inject MessageService service;

  @Override protected MessageThreadView handle(User user, Request request, Response response) {
    return service.groupMember(user, gson.fromJson(request.body(), AddGroupMemberCommand .class));
  }
}
