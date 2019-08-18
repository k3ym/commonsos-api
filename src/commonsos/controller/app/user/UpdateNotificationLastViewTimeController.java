package commonsos.controller.app.user;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.controller.app.AfterAppLoginController;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.service.command.LastViewTimeUpdateCommand;
import spark.Request;
import spark.Response;

public class UpdateNotificationLastViewTimeController extends AfterAppLoginController {

  @Inject UserService service;
  @Inject Gson gson;

  @Override
  public Object handleAfterLogin(User user, Request request, Response response) {
    LastViewTimeUpdateCommand command = gson.fromJson(request.body(), LastViewTimeUpdateCommand.class);
    service.updateNotificationLastViewTime(user, command);
    return "";
  }
}