package commonsos;

import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.post;

import org.web3j.protocol.Web3j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;

import commonsos.controller.JsonTransformer;
import commonsos.controller.PreflightController;
import commonsos.controller.ad.AdController;
import commonsos.controller.ad.AdCreateController;
import commonsos.controller.ad.AdDeleteController;
import commonsos.controller.ad.AdListController;
import commonsos.controller.ad.AdPhotoUpdateController;
import commonsos.controller.ad.AdUpdateController;
import commonsos.controller.ad.AdminAdDeleteController;
import commonsos.controller.ad.MyAdsController;
import commonsos.controller.auth.CreateAccountCompleteController;
import commonsos.controller.auth.CreateAccountTemporaryController;
import commonsos.controller.auth.LoginController;
import commonsos.controller.auth.LogoutController;
import commonsos.controller.auth.PasswordResetController;
import commonsos.controller.auth.PasswordResetRequestCheckController;
import commonsos.controller.auth.PasswordResetRequestController;
import commonsos.controller.community.CommunityCoverPhotoUpdateController;
import commonsos.controller.community.CommunityListController;
import commonsos.controller.community.CommunityNotificationController;
import commonsos.controller.community.CommunityNotificationListController;
import commonsos.controller.community.CommunityPhotoUpdateController;
import commonsos.controller.message.GroupMessageThreadController;
import commonsos.controller.message.GroupMessageThreadUpdateController;
import commonsos.controller.message.MessageListController;
import commonsos.controller.message.MessagePostController;
import commonsos.controller.message.MessageThreadController;
import commonsos.controller.message.MessageThreadForAdController;
import commonsos.controller.message.MessageThreadListController;
import commonsos.controller.message.MessageThreadPhotoUpdateController;
import commonsos.controller.message.MessageThreadUnreadCountController;
import commonsos.controller.message.MessageThreadUnsubscribeController;
import commonsos.controller.message.MessageThreadWithUserController;
import commonsos.controller.message.UpdateMessageThreadPersonalTitleController;
import commonsos.controller.transaction.BalanceController;
import commonsos.controller.transaction.TransactionCreateController;
import commonsos.controller.transaction.TransactionListController;
import commonsos.controller.user.SearchUsersCommunityController;
import commonsos.controller.user.UpdateAdLastViewTimeController;
import commonsos.controller.user.UpdateEmailCompleteController;
import commonsos.controller.user.UpdateEmailTemporaryController;
import commonsos.controller.user.UpdateNotificationLastViewTimeController;
import commonsos.controller.user.UpdateWalletLastViewTimeController;
import commonsos.controller.user.UserAvatarUpdateController;
import commonsos.controller.user.UserController;
import commonsos.controller.user.UserDeleteController;
import commonsos.controller.user.UserMobileDeviceUpdateController;
import commonsos.controller.user.UserNameUpdateController;
import commonsos.controller.user.UserPasswordResetRequestController;
import commonsos.controller.user.UserSearchController;
import commonsos.controller.user.UserStatusUpdateController;
import commonsos.controller.user.UserUpdateCommunitiesController;
import commonsos.controller.user.UserUpdateController;
import commonsos.di.GsonProvider;
import commonsos.di.Web3jProvider;
import commonsos.exception.AuthenticationException;
import commonsos.exception.BadRequestException;
import commonsos.exception.DisplayableException;
import commonsos.exception.ForbiddenException;
import commonsos.filter.AddHeaderFilter;
import commonsos.filter.LogFilter;
import commonsos.interceptor.TransactionInterceptor;
import commonsos.repository.DatabaseMigrator;
import commonsos.service.blockchain.BlockchainEventService;
import lombok.extern.slf4j.Slf4j;
import spark.Request;

@Slf4j
public class Server {

  @Inject private JsonTransformer toJson;
  @Inject private DatabaseMigrator databaseMigrator;
  @Inject private BlockchainEventService blockchainEventService;
  private Injector injector;

  public void start(String[] args) {
    injector = initDependencies();
    databaseMigrator.execute();
    CookieSecuringEmbeddedJettyFactory.register();
    setupServer();
    initRoutes();
    blockchainEventService.listenEvents();
  }
  
  protected void setupServer() {
    // nothing to setup in production server.
  }

  protected Injector initDependencies() {
    Module module = new AbstractModule() {
      @Override protected void configure() {
        bind(Gson.class).toProvider(GsonProvider.class);
        bind(Web3j.class).toProvider(Web3jProvider.class);
        bind(ObjectMapper.class).toInstance(new ObjectMapper());
      }
    };

    Injector injector = Guice.createInjector(module, new TransactionInterceptor());
    injector.injectMembers(this);
    return injector;
  }

  private void initRoutes() {

    before(new AddHeaderFilter());
    before(new LogFilter());
    before((request, response) -> log.info(requestInfo(request)));

    options("/*", injector.getInstance(PreflightController.class));

    post("/login", injector.getInstance(LoginController.class), toJson);
    post("/logout", injector.getInstance(LogoutController.class), toJson);
    post("/create-account", injector.getInstance(CreateAccountTemporaryController.class), toJson);
    post("/create-account/:accessId", injector.getInstance(CreateAccountCompleteController.class), toJson);
    get("/user", injector.getInstance(UserController.class), toJson);
    get("/users/:id", injector.getInstance(UserController.class), toJson);
    post("/users/:id", injector.getInstance(UserUpdateController.class), toJson);
    post("/users/:id/username", injector.getInstance(UserNameUpdateController.class), toJson);
    post("/users/:id/status", injector.getInstance(UserStatusUpdateController.class), toJson);
    post("/users/:id/passwordreset", injector.getInstance(UserPasswordResetRequestController.class), toJson);
    post("/users/:id/delete", injector.getInstance(UserDeleteController.class), toJson);
    post("/users/:id/avatar", injector.getInstance(UserAvatarUpdateController.class), toJson);
    post("/users/:id/mobile-device", injector.getInstance(UserMobileDeviceUpdateController.class), toJson);
    get("/users", injector.getInstance(UserSearchController.class), toJson);
    get("/users/:id/communities", injector.getInstance(SearchUsersCommunityController.class), toJson);
    post("/users/:id/communities", injector.getInstance(UserUpdateCommunitiesController.class), toJson);
    post("/users/:id/emailaddress", injector.getInstance(UpdateEmailTemporaryController.class), toJson);
    post("/users/:id/emailaddress/:accessId", injector.getInstance(UpdateEmailCompleteController.class), toJson);
    post("/users/:id/wallet/lastViewTime", injector.getInstance(UpdateWalletLastViewTimeController.class), toJson);
    post("/users/:id/ad/lastViewTime", injector.getInstance(UpdateAdLastViewTimeController.class), toJson);
    post("/users/:id/notification/lastViewTime", injector.getInstance(UpdateNotificationLastViewTimeController.class), toJson);
    post("/passwordreset", injector.getInstance(PasswordResetRequestController.class), toJson);
    get("/passwordreset/:accessId", injector.getInstance(PasswordResetRequestCheckController.class), toJson);
    post("/passwordreset/:accessId", injector.getInstance(PasswordResetController.class), toJson);

    post("/ads", injector.getInstance(AdCreateController.class), toJson);
    get("/ads", injector.getInstance(AdListController.class), toJson);
    get("/ads/:id", injector.getInstance(AdController.class), toJson);
    post("/ads/:id", injector.getInstance(AdUpdateController.class), toJson);
    post("/ads/:id/delete", injector.getInstance(AdDeleteController.class), toJson);
    post("/ads/:id/photo", injector.getInstance(AdPhotoUpdateController.class), toJson);
    get("/my-ads", injector.getInstance(MyAdsController.class), toJson);

    get("/balance", injector.getInstance(BalanceController.class), toJson);
    get("/transactions", injector.getInstance(TransactionListController.class), toJson);
    post("/transactions", injector.getInstance(TransactionCreateController.class), toJson);

    post("/message-threads/for-ad/:adId", injector.getInstance(MessageThreadForAdController.class), toJson);
    post("/message-threads/user/:userId", injector.getInstance(MessageThreadWithUserController.class), toJson);

    post("/message-threads/group", injector.getInstance(GroupMessageThreadController.class), toJson);
    post("/message-threads/:id/group", injector.getInstance(GroupMessageThreadUpdateController.class), toJson);
    post("/message-threads/:id/title", injector.getInstance(UpdateMessageThreadPersonalTitleController.class), toJson);
    post("/message-threads/:id/photo", injector.getInstance(MessageThreadPhotoUpdateController.class), toJson);
    post("/message-threads/:id/unsubscribe", injector.getInstance(MessageThreadUnsubscribeController.class), toJson);

    get("/message-threads/unread-count", injector.getInstance(MessageThreadUnreadCountController.class), toJson);
    get("/message-threads/:id", injector.getInstance(MessageThreadController.class), toJson);
    get("/message-threads", injector.getInstance(MessageThreadListController.class), toJson);
    post("/message-threads/:id/messages", injector.getInstance(MessagePostController.class), toJson);
    get("/message-threads/:id/messages", injector.getInstance(MessageListController.class), toJson);

    get("/communities", injector.getInstance(CommunityListController.class), toJson);
    post("/communities/:id/photo", injector.getInstance(CommunityPhotoUpdateController.class), toJson);
    post("/communities/:id/coverPhoto", injector.getInstance(CommunityCoverPhotoUpdateController.class), toJson);
    post("/communities/:id/notification/:wordpressId", injector.getInstance(CommunityNotificationController.class), toJson);
    get("/communities/:id/notification", injector.getInstance(CommunityNotificationListController.class), toJson);

    post("/admin/ads/:id/delete", injector.getInstance(AdminAdDeleteController.class), toJson);
    
    exception(BadRequestException.class, (exception, request, response) -> {
      log.error("Bad request", exception);
      response.status(400);
      response.body("");
    });
    exception(AuthenticationException.class, (exception, request, response) -> {
      log.error("Not authenticated", exception);
      response.status(401);
      response.body("");
    });
    exception(ForbiddenException.class, (exception, request, response) -> {
      log.error("Access denied", exception);
      response.status(403);
      response.body("");
    });
    exception(DisplayableException.class, (exception, request, response) -> {
      log.error("Displayable error", exception);
      response.status(468);
      response.body(toJson.render(ImmutableMap.of("key", exception.getMessage())));
    });
    exception(Exception.class, (exception, request, response) -> {
      log.error("Processing failed", exception);
      response.status(500);
      response.body("");
    });
  }

  private String requestInfo(Request request) {
    String info = request.requestMethod() + " " + request.pathInfo();
    if (request.queryString() != null) info += "?" + request.queryString();
    return info;
  }

  public static void main(String[] args) {
    try {
      new Server().start(args);
    }
    catch (Throwable e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
