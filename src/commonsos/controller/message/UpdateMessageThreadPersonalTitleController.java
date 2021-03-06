package commonsos.controller.message;

import static java.lang.Long.parseLong;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.Gson;

import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.MessageService;
import commonsos.service.command.UpdateMessageThreadPersonalTitleCommand;
import commonsos.view.MessageThreadView;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

public class UpdateMessageThreadPersonalTitleController extends AfterLoginController {

  @Inject Gson gson;
  @Inject MessageService service;

  @Override protected MessageThreadView handleAfterLogin(User user, Request request, Response response) {
    UpdateMessageThreadPersonalTitleCommand command = gson.fromJson(request.body(), UpdateMessageThreadPersonalTitleCommand.class);
    
    String id = request.params("id");
    if(StringUtils.isEmpty(id)) throw new BadRequestException("id is required");
    if(!NumberUtils.isParsable(id)) throw new BadRequestException("invalid id");
    command.setThreadId(parseLong(id));
    
    return service.updatePersonalTitle(user, command);
  }
}
