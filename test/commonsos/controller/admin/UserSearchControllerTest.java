package commonsos.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.UserService;
import commonsos.view.UserView;
import spark.Request;

@RunWith(MockitoJUnitRunner.class)
public class UserSearchControllerTest {
  
  @Mock Request request;
  @Mock UserService service;
  @InjectMocks UserSearchController controller;

  @Test
  public void handle() {
    when(request.queryParams("communityId")).thenReturn("123");
    when(request.queryParams("q")).thenReturn("john doe");
    ArrayList<UserView> users = new ArrayList<>();
    User user = new User();
    when(service.searchUsers(user, 123L, "john doe")).thenReturn(users);

    List<UserView> result = controller.handleAfterLogin(user, request, null);

    assertThat(result).isEqualTo(users);
  }
  
  @Test(expected = BadRequestException.class)
  public void handle_noCommunityId() {
    controller.handleAfterLogin(new User(), request, null);
  }
}