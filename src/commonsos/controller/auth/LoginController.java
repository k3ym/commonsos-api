package commonsos.controller.auth;

import static commonsos.CookieSecuringEmbeddedJettyFactory.MAX_SESSION_AGE_IN_SECONDS;

import javax.inject.Inject;

import org.slf4j.MDC;

import com.google.gson.Gson;

import commonsos.CSRF;
import commonsos.LogFilter;
import commonsos.repository.user.User;
import commonsos.service.auth.LoginCommand;
import commonsos.service.user.UserPrivateView;
import commonsos.service.user.UserService;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Session;

public class LoginController implements Route {

  public static final String USER_SESSION_ATTRIBUTE_NAME = "user";
  @Inject Gson gson;
  @Inject UserService userService;
  @Inject CSRF csrf;

  @Override public UserPrivateView handle(Request request, Response response) {
    LoginCommand command = gson.fromJson(request.body(), LoginCommand.class);
    User user = userService.checkPassword(command.getUsername(), command.getPassword());
    
    Session session = request.session();
    session.attribute(USER_SESSION_ATTRIBUTE_NAME, userService.session(user));
    session.maxInactiveInterval(MAX_SESSION_AGE_IN_SECONDS);
    
    MDC.put(LogFilter.USERNAME_MDC_KEY, user.getUsername());
    csrf.setToken(request, response);
    
    return userService.privateView(user);
  }
}
