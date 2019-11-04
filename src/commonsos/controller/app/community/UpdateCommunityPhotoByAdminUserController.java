package commonsos.controller.app.community;

import static java.lang.Long.parseLong;

import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;

import commonsos.command.UploadPhotoCommand;
import commonsos.controller.app.UploadPhotoForAppController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.CommunityService;
import commonsos.view.UrlView;
import spark.Request;
import spark.Response;

public class UpdateCommunityPhotoByAdminUserController extends UploadPhotoForAppController {

  @Inject CommunityService communityService;

  @Override
  protected UrlView handleUploadPhoto(User user, UploadPhotoCommand command, Request request, Response response) {
    String communityId = request.params("id");
    if(!NumberUtils.isParsable(communityId)) throw new BadRequestException("invalid id");

    String url = communityService.updatePhoto(user, command, parseLong(communityId));
    return new UrlView().setUrl(url);
  }
}