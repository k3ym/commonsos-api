package commonsos.integration.admin.admin;

import static commonsos.repository.entity.CommunityStatus.PUBLIC;
import static commonsos.repository.entity.Role.COMMUNITY_ADMIN;
import static commonsos.repository.entity.Role.NCL;
import static commonsos.repository.entity.Role.TELLER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Admin;
import commonsos.repository.entity.Community;

public class SearchAdminTest extends IntegrationTest {

  private Community com1;
  private Community com2;
  private Admin ncl;
  private Admin com1Admin1;
  private Admin com1Admin2;
  private Admin com1Teller1;
  private Admin com1Teller2;
  private String sessionId;
  
  @BeforeEach
  public void setup() throws Exception {
    com1 =  create(new Community().setName("com1").setStatus(PUBLIC));
    com2 =  create(new Community().setName("com2").setStatus(PUBLIC));
    
    ncl = create(new Admin().setEmailAddress("ncl@before.each.com").setPasswordHash(hash("password")).setRole(NCL));
    com1Admin1 = create(new Admin().setEmailAddress("com1Admin1@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Admin2 = create(new Admin().setEmailAddress("com1Admin2@before.each.com").setPasswordHash(hash("password")).setRole(COMMUNITY_ADMIN).setCommunity(com1));
    com1Teller1 = create(new Admin().setEmailAddress("com1Teller1@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));
    com1Teller2 = create(new Admin().setEmailAddress("com1Teller2@before.each.com").setPasswordHash(hash("password")).setRole(TELLER).setCommunity(com1));
  }
  
  @Test
  public void searchAdmin_byNcl() {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");
    
    // search community_admin
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={communityId}&roleId={roleId}", com1.getId(), COMMUNITY_ADMIN.getId())
      .then().statusCode(200)
      .body("adminList.id",  contains(com1Admin1.getId().intValue(), com1Admin2.getId().intValue()));

    // search teller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={communityId}&roleId={roleId}", com1.getId(), TELLER.getId())
      .then().statusCode(200)
      .body("adminList.id",  contains(com1Teller1.getId().intValue(), com1Teller2.getId().intValue()));
  }
  
  @Test
  public void searchAdmin_byCom1Admin1() {
    sessionId = loginAdmin(com1Admin1.getEmailAddress(), "password");
    
    // search community_admin
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={communityId}&roleId={roleId}", com1.getId(), COMMUNITY_ADMIN.getId())
      .then().statusCode(200)
      .body("adminList.id",  contains(com1Admin1.getId().intValue(), com1Admin2.getId().intValue()));

    // search teller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={communityId}&roleId={roleId}", com1.getId(), TELLER.getId())
      .then().statusCode(200)
      .body("adminList.id",  contains(com1Teller1.getId().intValue(), com1Teller2.getId().intValue()));

    // search community_admin [forbidden]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={communityId}&roleId={roleId}", com2.getId(), COMMUNITY_ADMIN.getId())
      .then().statusCode(403);
  }
  
  @Test
  public void searchAdmin_byCom1Teller1() {
    sessionId = loginAdmin(com1Teller1.getEmailAddress(), "password");
    
    // search community_admin [forbidden]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={communityId}&roleId={roleId}", com1.getId(), COMMUNITY_ADMIN.getId())
      .then().statusCode(403);

    // search teller
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={communityId}&roleId={roleId}", com1.getId(), TELLER.getId())
      .then().statusCode(200)
      .body("adminList.id",  contains(com1Teller1.getId().intValue(), com1Teller2.getId().intValue()));

    // search community_admin [forbidden]
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={communityId}&roleId={roleId}", com2.getId(), TELLER.getId())
      .then().statusCode(403);
  }

  @Test
  public void searchCommunity_pagination() throws Exception {
    sessionId = loginAdmin(ncl.getEmailAddress(), "password");

    Community page_com =  create(new Community().setName("page_com").setStatus(PUBLIC));
    create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("page_a1").setCommunity(page_com));
    create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("page_a2").setCommunity(page_com));
    create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("page_a3").setCommunity(page_com));
    create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("page_a4").setCommunity(page_com));
    create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("page_a5").setCommunity(page_com));
    create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("page_a6").setCommunity(page_com));
    create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("page_a7").setCommunity(page_com));
    create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("page_a8").setCommunity(page_com));
    create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("page_a9").setCommunity(page_com));
    create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("page_a10").setCommunity(page_com));
    create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("page_a11").setCommunity(page_com));
    create(new Admin().setRole(COMMUNITY_ADMIN).setEmailAddress("page_a12").setCommunity(page_com));

    // page 0 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={comId}&roleId={roleId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", page_com.getId(), COMMUNITY_ADMIN.getId(), "0", "10", "ASC")
      .then().statusCode(200)
      .body("adminList.emailAddress", contains(
          "page_a1", "page_a2", "page_a3", "page_a4", "page_a5",
          "page_a6", "page_a7", "page_a8", "page_a9", "page_a10"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 asc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={comId}&roleId={roleId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", page_com.getId(), COMMUNITY_ADMIN.getId(), "1", "10", "ASC")
      .then().statusCode(200)
      .body("adminList.emailAddress", contains(
          "page_a11", "page_a12"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("ASC"))
      .body("pagination.lastPage", equalTo(1));

    // page 0 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={comId}&roleId={roleId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", page_com.getId(), COMMUNITY_ADMIN.getId(), "0", "10", "DESC")
      .then().statusCode(200)
      .body("adminList.emailAddress", contains(
          "page_a12", "page_a11", "page_a10", "page_a9", "page_a8",
          "page_a7", "page_a6", "page_a5", "page_a4", "page_a3"))
      .body("pagination.page", equalTo(0))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));

    // page 1 size 10 desc
    given()
      .cookie("JSESSIONID", sessionId)
      .when().get("/admin/admins?communityId={comId}&roleId={roleId}&pagination[page]={page}&pagination[size]={size}&pagination[sort]={sort}", page_com.getId(), COMMUNITY_ADMIN.getId(), "1", "10", "DESC")
      .then().statusCode(200)
      .body("adminList.emailAddress", contains(
          "page_a2", "page_a1"))
      .body("pagination.page", equalTo(1))
      .body("pagination.size", equalTo(10))
      .body("pagination.sort", equalTo("DESC"))
      .body("pagination.lastPage", equalTo(1));
  }
}