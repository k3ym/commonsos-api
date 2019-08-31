package commonsos.integration.admin.community.redistribution;

import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityUser;
import commonsos.repository.entity.Redistribution;
import commonsos.repository.entity.User;

public class SearchRedistributionTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private User user1;
  private User user2;
  private User user3;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));

    user1 = create(new User().setUsername("user1").setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));
    user2 = create(new User().setUsername("user2").setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));
    user3 = create(new User().setUsername("user3").setCommunityUserList(asList(new CommunityUser().setCommunity(com1))));

    create(new Redistribution().setCommunity(com1).setUser(user1).setRate(new BigDecimal("1.5")));
    create(new Redistribution().setCommunity(com1).setUser(user2).setRate(new BigDecimal("1.6")));
    create(new Redistribution().setCommunity(com1).setUser(user3).setRate(new BigDecimal("1.7")));
  }
  
  @Test
  public void createRedistribution_ncl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    // get redistribution [success]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}/redistributions", com1.getId())
      .then().statusCode(200)
      .body("redistributionList.isAll", contains(false, false, false))
      .body("redistributionList.userId", contains(user1.getId().intValue(), user2.getId().intValue(), user3.getId().intValue()))
      .body("redistributionList.username", contains("user1", "user2", "user3"))
      .body("redistributionList.redistributionRate", contains(1.5F, 1.6F, 1.7F));
    
    // get redistribution [success]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}/redistributions", com2.getId())
      .then().statusCode(200);
  }
  
  @Test
  public void createRedistribution_com1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");
    
    // get redistribution [success]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}/redistributions", com1.getId())
      .then().statusCode(200)
      .body("redistributionList.isAll", contains(false, false, false))
      .body("redistributionList.userId", contains(user1.getId().intValue(), user2.getId().intValue(), user3.getId().intValue()))
      .body("redistributionList.username", contains("user1", "user2", "user3"))
      .body("redistributionList.redistributionRate", contains(1.5F, 1.6F, 1.7F));
    
    // get redistribution [success]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}/redistributions", com2.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void createRedistribution_com1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");
    
    // get redistribution [success]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}/redistributions", com1.getId())
      .then().statusCode(403);
  }

  @Test
  public void searchCommunity_pagination() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    Community page_com = create(new Community().setName("page_com"));
    create(new Redistribution().setCommunity(page_com).setUser(user1).setRate(new BigDecimal("1")));
    create(new Redistribution().setCommunity(page_com).setUser(user1).setRate(new BigDecimal("2")));
    create(new Redistribution().setCommunity(page_com).setUser(user1).setRate(new BigDecimal("3")));
    create(new Redistribution().setCommunity(page_com).setUser(user1).setRate(new BigDecimal("4")));
    create(new Redistribution().setCommunity(page_com).setUser(user1).setRate(new BigDecimal("5")));
    create(new Redistribution().setCommunity(page_com).setUser(user1).setRate(new BigDecimal("6")));
    create(new Redistribution().setCommunity(page_com).setUser(user1).setRate(new BigDecimal("7")));
    create(new Redistribution().setCommunity(page_com).setUser(user1).setRate(new BigDecimal("8")));
    create(new Redistribution().setCommunity(page_com).setUser(user1).setRate(new BigDecimal("9")));
    create(new Redistribution().setCommunity(page_com).setUser(user1).setRate(new BigDecimal("10")));
    create(new Redistribution().setCommunity(page_com).setUser(user1).setRate(new BigDecimal("11")));
    create(new Redistribution().setCommunity(page_com).setUser(user1).setRate(new BigDecimal("12")));

    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}/redistributions?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", page_com.getId(), "0", "10", "ASC")
      .then().statusCode(200)
      .body("redistributionList.redistributionRate", contains(
          1F, 2F, 3F, 4F, 5F, 6F, 7F, 8F, 9F, 10F))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}/redistributions?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", page_com.getId(), "1", "10", "ASC")
      .then().statusCode(200)
      .body("redistributionList.redistributionRate", contains(
          11F, 12F))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}/redistributions?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", page_com.getId(), "0", "10", "DESC")
      .then().statusCode(200)
      .body("redistributionList.redistributionRate", contains(
          12F, 11F, 10F, 9F, 8F, 7F, 6F, 5F, 4F, 3F))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/communities/{id}/redistributions?pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", page_com.getId(), "1", "10", "DESC")
      .then().statusCode(200)
      .body("redistributionList.redistributionRate", contains(
          2F, 1F))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}