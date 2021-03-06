package commonsos.controller.ad;

import static java.lang.Long.parseLong;
import static spark.utils.StringUtils.isEmpty;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.controller.AfterLoginController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import spark.Request;
import spark.Response;

public class AdDeleteController extends AfterLoginController {

  @Inject AdService adService;

  @Override
  public Object handleAfterLogin(User user, Request request, Response response) {
    String id = request.params("id");
    if (isEmpty(id)) throw new BadRequestException("id is required");
    if (!NumberUtils.isParsable(id)) throw new BadRequestException("invalid id");
    adService.deleteAdLogicallyByUser(user, parseLong(id));
    return "";
  }
}
