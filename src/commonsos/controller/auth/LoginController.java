package commonsos.controller.auth;

import com.google.gson.Gson;
import commonsos.domain.auth.Session;
import commonsos.domain.auth.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.inject.Inject;
import java.util.Map;

public class LoginController implements Route {

  @Inject Gson gson;
  @Inject UserService userService;

  @Override public Session handle(Request request, Response response) {
    Map map = gson.fromJson(request.body(), Map.class);

    return userService.login(String.valueOf(map.get("username")), String.valueOf(map.get("password")));
  }
}