package commonsos.integration.app.ad;

import static commonsos.ApiVersion.APP_API_VERSION;
import static commonsos.repository.entity.PublishStatus.PUBLIC;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.AdType;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.User;

public class CreateAdTest extends IntegrationTest {

  private Community community;
  private User user;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    community =  create(new Community().setName("community").setPublishStatus(PUBLIC));
    user =  create(new User().setUsername("user").setPasswordHash(hash("pass")).setCommunityUserList(asList(new CommunityUser().setCommunity(community))));

    sessionId = loginApp("user", "pass");
  }
  
  @Test
  public void adCreate() {
    // prepare
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("title", "title");
    requestParam.put("description", "description");
    requestParam.put("points", 10);
    requestParam.put("location", "location");
    requestParam.put("type", "GIVE");

    // call api
    given()
      .body(gson.toJson(requestParam))
      .cookie("JSESSIONID", sessionId)
      .when().post("/app/v{v}/ads", APP_API_VERSION.getMajor())
      .then().statusCode(200)
      .body("title", equalTo("title"))
      .body("description", equalTo("description"))
      .body("points", equalTo(10))
      .body("location", equalTo("location"))
      .body("own", equalTo(true))
      .body("type", equalTo("GIVE"))
      .body("createdBy.id", equalTo(user.getId().intValue()))
      .body("createdBy.username", equalTo("user"));
    
    // verify
    Ad ad = emService.get().createQuery("FROM Ad WHERE title = 'title'", Ad.class).getSingleResult();
    assertThat(ad.getTitle()).isEqualTo("title");
    assertThat(ad.getDescription()).isEqualTo("description");
    assertThat(ad.getPoints()).isEqualByComparingTo(BigDecimal.TEN);
    assertThat(ad.getLocation()).isEqualTo("location");
    assertThat(ad.getType()).isEqualTo(AdType.GIVE);
    assertThat(ad.getCommunityId()).isEqualTo(community.getId());
    assertThat(ad.getCreatedUserId()).isEqualTo(user.getId());
    assertThat(ad.isDeleted()).isEqualTo(false);
    assertThat(ad.getPublishStatus()).isEqualTo(PUBLIC);
  }
}