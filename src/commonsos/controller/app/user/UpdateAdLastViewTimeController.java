package commonsos.controller.app.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.command.app.UpdateLastViewTimeCommand;
import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.CommonView;
import spark.Request;
import spark.Response;

public class UpdateAdLastViewTimeController extends AfterAppLoginController {

  @Inject UserService service;
  @Inject Gson gson;

  @Override
  public CommonView handleAfterLogin(User user, Request request, Response response) {
    UpdateLastViewTimeCommand command = gson.fromJson(request.body(), UpdateLastViewTimeCommand.class);
    service.updateAdLastViewTime(user, command);
    return new CommonView();
  }
}
