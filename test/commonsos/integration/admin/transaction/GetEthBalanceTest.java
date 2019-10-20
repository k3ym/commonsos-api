package commonsos.integration.admin.transaction;

import static commonsos.repository.entity.CommunityStatus.PRIVATE;
import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;

public class GetEthBalanceTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin;
  private Admin com1Teller;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setStatus(PRIVATE));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin = create(new Admin().setEmailAddress("com1Admin@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller = create(new Admin().setEmailAddress("com1Teller@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));
  }
  
  @Test
  public void getTokenBalance_ncl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    // public com
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance?communityId={id}", com1.getId())
      .then().statusCode(200)
      .body("balance", notNullValue());
    
    // private com
    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/admin/transactions/eth/balance?communityId={id}", com2.getId())
    .then().statusCode(200)
    .body("balance", notNullValue());

    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/admin/transactions/eth/balance?communityId={id}", -1L)
    .then().statusCode(400);
  }
  
  @Test
  public void createRedistribution_com1Admin() {
    sessionId = loginAdmin(com1Admin.getEmailAddress(), "password");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance?communityId={id}", com1.getId())
      .then().statusCode(200)
      .body("balance", notNullValue());
    
    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/admin/transactions/eth/balance?communityId={id}", com2.getId())
    .then().statusCode(403);
  }
  
  @Test
  public void createRedistribution_com1Teller() {
    sessionId = loginAdmin(com1Teller.getEmailAddress(), "password");

    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/transactions/eth/balance?communityId={id}", com1.getId())
      .then().statusCode(200)
      .body("balance", notNullValue());
    
    given()
    .cookie("JSESSIONID", sessionId)
    .when().get("/admin/transactions/eth/balance?communityId={id}", com2.getId())
    .then().statusCode(403);
  }
}