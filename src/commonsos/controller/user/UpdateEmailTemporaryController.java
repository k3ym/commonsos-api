package commonsos.controller.user;

import static commonsos.annotation.SyncObject.USERNAME_AND_EMAIL_ADDRESS;

import javax.inject.Inject;

import com.google.gson.Gson;

import commonsos.annotation.Synchronized;
import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.service.command.UpdateEmailTemporaryCommand;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

@Synchronized(USERNAME_AND_EMAIL_ADDRESS)
public class UpdateEmailTemporaryController extends AfterLoginController {

  @Inject UserService userService;
  @Inject Gson gson;

  @Override protected Object handleAfterLogin(User user, Request request, Response response) {
    UpdateEmailTemporaryCommand command = gson.fromJson(request.body(), UpdateEmailTemporaryCommand.class);
    if(StringUtils.isEmpty(command.getNewEmailAddress())) throw new BadRequestException("newEmailAddress is required");
    
    command.setUserId(user.getId());
    
    userService.updateEmailTemporary(command);

    return null;
  }
}
