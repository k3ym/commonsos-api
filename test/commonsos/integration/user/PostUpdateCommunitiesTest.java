package commonsos.integration.user;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.iterableWithSize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;

public class PostUpdateCommunitiesTest extends IntegrationTest {

  private Community community1;
  private Community community2;
  private User admin1;
  private User admin2;
  private User user;
  private String sessionId;
  
  @Before
  public void createUser() {
    community1 = create(new Community().setName("community1").setTokenContractAddress("0x0"));
    community2 = create(new Community().setName("community2").setTokenContractAddress("0x0"));
    admin1 = create(new User().setUsername("admin1").setPasswordHash(hash("password")).setEmailAddress("admin1@test.com").setCommunityList(asList(community1)));
    admin2 = create(new User().setUsername("admin2").setPasswordHash(hash("password")).setEmailAddress("admin2@test.com").setCommunityList(asList(community2)));
    user = create(new User().setUsername("user").setPasswordHash(hash("password")).setEmailAddress("user@test.com").setCommunityList(asList(community1)));
    update(community1.setAdminUser(admin1));
    update(community2.setAdminUser(admin2));
    
    sessionId = login("user", "password");
  }
  
  @Test
  public void updateCommunities() throws Exception {
    Map<String, Object> requestParam = new HashMap<>();
    List<Long> communityList = new ArrayList<>(Arrays.asList(community2.getId()));
    requestParam.put("communityList", communityList);
    
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{id}/communities", user.getId())
      .then().statusCode(200)
      .body("communityList.id", iterableWithSize(1))
      .body("communityList.id", contains(community2.getId().intValue()));
    
    
    communityList = new ArrayList<>(Arrays.asList(community1.getId(), community2.getId()));
    requestParam.put("communityList", communityList);
    given()
      .cookie("JSESSIONID", sessionId)
      .body(gson.toJson(requestParam))
      .when().post("/users/{id}/communities", user.getId())
      .then().statusCode(200)
      .body("communityList.id", iterableWithSize(2))
      .body("communityList.id", contains(community1.getId().intValue(), community2.getId().intValue()));
  }
}