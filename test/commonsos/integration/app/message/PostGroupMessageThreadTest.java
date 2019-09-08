package commonsos.integration.app.message;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.User;

public class PostGroupMessageThreadTest extends IntegrationTest {

  private Community community;
  private Community otherCommunity;
  private User user1;
  private User user2;
  private User user3;
  private User otherCommunityUser;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    community =  create(new Community().setStatus(PUBLIC).setName("community"));
    otherCommunity =  create(new Community().setStatus(PUBLIC).setName("otherCommunity"));
    user1 =  create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    user2 =  create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    user3 =  create(new User().setUsername("user3").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));
    otherCommunityUser =  create(new User().setUsername("otherCommunityUser").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(otherCommunity))));

    sessionId = loginApp("user1", "pass");
  }
  
  @Test
  public void groupMessageThread() {
    // prepare
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("title", "title");
    requestParam.put("memberIds", asList(user2.getId(), user3.getId()));
    
    // call api
    int id = given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/message-threads/group", APP_API_VERSION.getMajor())
      .then().statusCode(200)
      .body("id", notNullValue())
      .body("ad.id", nullValue())
      .body("communityId", equalTo(community.getId().intValue()))
      .body("title", equalTo("title"))
      .body("personalTitle", nullValue())
      .body("parties.id", contains(user2.getId().intValue(), user3.getId().intValue()))
      .body("creator.id", equalTo(user1.getId().intValue()))
      .body("counterParty.id", equalTo(user2.getId().intValue()))
      .body("lastMessage", nullValue())
      .body("unread", equalTo(false))
      .body("group", equalTo(true))
      .body("photoUrl", nullValue())
      .body("createdAt", notNullValue())
      .extract().path("id");
    
    // verify db
    MessageThread messageThread = emService.get().find(MessageThread.class, (long) id);
    assertThat(messageThread.getTitle()).isEqualTo("title");
    assertThat(messageThread.getAdId()).isNull();
    assertThat(messageThread.getCreatedBy()).isEqualTo(user1.getId());
    assertThat(messageThread.isGroup()).isEqualTo(true);
    
    messageThread.getParties().sort((a,b) -> a.getUser().getId().compareTo(b.getUser().getId()));
    assertThat(messageThread.getParties().size()).isEqualTo(3);
    assertThat(messageThread.getParties().get(0).getUser().getId()).isEqualTo(user1.getId());
    assertThat(messageThread.getParties().get(1).getUser().getId()).isEqualTo(user2.getId());
    assertThat(messageThread.getParties().get(2).getUser().getId()).isEqualTo(user3.getId());
  }

  @Test
  public void groupMessageThread_otherCommunityUser() {
    // prepare
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("title", "title");
    requestParam.put("memberIds", asList(otherCommunityUser.getId()));
    
    // call api
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/app/v{v}/message-threads/group", APP_API_VERSION.getMajor())
      .then().statusCode(400);
  }
}
