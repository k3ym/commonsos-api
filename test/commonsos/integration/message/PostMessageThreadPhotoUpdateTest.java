package commonsos.integration.message;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import commonsos.integration.IntegrationTest;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.User;

public class PostMessageThreadPhotoUpdateTest extends IntegrationTest {

  private Community community;
  private Community otherCommunity;
  private User user1;
  private User user2;
  private User user3;
  private User otherCommunityUser;
  private Long messageThreadId;
  private String sessionId;
  
  @Before
  public void setup() {
    community =  create(new Community().setName("community"));
    otherCommunity =  create(new Community().setName("otherCommunity"));
    user1 =  create(new User().setUsername("user1").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
    user2 =  create(new User().setUsername("user2").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
    user3 =  create(new User().setUsername("user3").setPasswordHash(hash("pass")).setCommunityList(asList(community)));
    otherCommunityUser =  create(new User().setUsername("otherCommunityUser").setPasswordHash(hash("pass")).setCommunityList(asList(otherCommunity)));

    sessionId = login("user1", "pass");

    // create group chat
    Map<String, Object> requestParam = new HashMap<>();
    requestParam.put("communityId", community.getId());
    requestParam.put("title", "title");
    requestParam.put("memberIds", asList(user2.getId()));
    int id = given()
        .cookie("JSESSIONID", sessionId)
        .body(gson.toJson(requestParam))
        .when().post("/message-threads/group")
        .then().statusCode(200)
        .extract().path("id");
    messageThreadId = (long) id;
  }
  
  @Test
  public void updatePhoto() {
    // prepare
    String body = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAMCAgICAgMCAgIDAwMDBAYEBAQEBAgGBgUGCQgKCgkICQkKDA8MCgsOCwkJDRENDg8QEBEQCgwSExIQEw8QEBD/2wBDAQMDAwQDBAgEBAgQCwkLEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBD/wAARCAB1AIIDASIAAhEBAxEB/8QAGwAAAwEAAwEAAAAAAAAAAAAAAAECAwQFBgf/xAAvEAACAgEDAwQBBAIBBQAAAAABAgARIQMSMQRBUQUTImEGMnGRoUKBUhQVweHw/8QAGgEBAQEBAQEBAAAAAAAAAAAAAAECAwQFBv/EACARAQEAAgICAwEBAAAAAAAAAAABAhEhMQMSBEFRExT/2gAMAwEAAhEDEQA/APlgYJZYXKCs1MOGE1KryRcYXAC0MY8T9Hvb89ax2UMtUrYKJBloGZQupW5RkiBSwGvB7wds9hNNyYBW2/3l7SBYMDxg8wlu0EkChiSLY7xzwY23A2VMQBVvjiTC463ydkAApiY2BsgNipPNgYIlWy1bZPMm3X1gRRt5kMTfMo4IzXmLYWJIIIlYtoFyKXZpsoVYOMzMbl5E00g7k7VwPMhNfYCKAAcSNRQpDMSRwB4nL0ekGsSHevucj/o9FU277YiiZNtSR1e0/8AAmE7H/tyDAc0IRs9XZ6X4bRdUd2n0S6Z5YajAYnD630jqfTdY6Gvpm6NAC8z7n1PT6fthF0xjANZnnvUvwzQ9T6pdYM5KghlLUB08ePybvmPXfi42cPlnpn496r6rps/R6e728NjMz9R9N9T9KX2et6N9Nr2qGGSZ9j9J9D0PRnGgBgGznpHqP4x6L6tqrroaTsytuABIXmanyrMuuGf8ALJO3xQI4q9vj49pp0HpvWo6vtdJpBypRPAns/y78G6j3X630h0XS00O7ROCR9SvxdNPo9IwoV9vy3DJPedv6zKbjjPBZeXmvUvxj1T07pR1nU7TpG7K/44nTMg2ghueJ9B/KdTX1elPT7gFKFit0CTPANaqqFR8MTXjyuU5c8sPW8JKEizQMTE7gtC/FRk7Bk89oHYygqRgzcibqCQxNjPjxF8mzVD6icFGtf7mirpkYPbMUhKbyMfc0TUfSwGvdzII2qaz2qWtUQeAOZNterfS1ipLKZoddnagazOKLrbWK5jAAr5GSmM07EaZIB91YTJSgAHuHiENvu7daqIADZ5xR1DBy4GDOHr9Ro6zCnwO84Z6waXUezusDjwZ8v7fSnTuveZjbmwO8y1OocN7gB5udW/Xtt2K1GLkv1etQYBIwDNSbY7cvqddddWOyzRFHvPJendB1nS9Xr6utpkJqOWX9p6A9TQ3MR4ka/X6SJu1mFA4AzmdMOGLqPD/lunrrh2J3A89p532y74Az9z1H5GRrkqjZu6nmi76AcbfnQFET2eK6jxSxx2pXbS1ALWR7RB3EX4rtLYqcMcnkmS5IC/pE6uUhMGal/UbMe5h8QADxmL3GC5FkcVBdhaycwcKsrZJsnxNNwO212msyF4pqxESf8a/8AUzpu5cNFarYmxwB3Eh3G6lazV1GdpBdRV9pGOQuR/UTlLlD3eoGKb/UJoGah8oRpPZ9O1fUNNCEQ/LuO0Y1/ezkzqPcZ9TnM1TqSjUzBRPB6PoeztPbBG8mvsZmNamI1GNjAM4Or1ZBCF/7xOP1XqGjplU1dSiczeOCXKOx1dcFiBxOP1PUrppuY2a4nX6fqaAlrJE6zrfVH1dUhF2KcnxN44Xbnllw5PW9XoaYOuy728HzPPdV1Q1tZtXZtJqcnX6ldbHt4PA7Tg69MbOnQp6McddPJld8siSTlrEGN0Kqv7lKoY7AtVyYOpalC7tvE1tIzNcm1BwYwFOB/MbCmIKlfIMFUWCCc8XMor/GBoENiI2P1cJDEk4ENTFoS4RojiS27eTuNwusEWOcwbYMFq/8SwuP4LPkwmoKkAfuEbZd43XuoLhu/mWvq2mmSC350mQSGe1PmUGUH9NmZ9I63yV2Gt6ydVmVNJh84ev1D6qKQxxM23D5FecCR8v0q0smkme3I6bqX073E1Uz1NTeS6A1feZ2wajX47ZbUVXmGbab4v7OJmzsfiADQHMvJADj/YmeoB2wMG4Sdi6JNcCIklVxL4qxg4jsXt2m4XLtD2SEUkHmSSTkkY7TTOq7cyGBBJC2BCSbQxWxYJP12klvl8QTGCWQShHsYYA5h0kk6TybsX4gxGQSO1YhsO7HbEtFBPzwObhWg0RQyeIRD3iAQefqEm3GrIHAP8iBD0GVR/uNyztu30PHcxUb2gX3MrU1rk21HsEqTQ4izQJFEizAqGFEgfscyiNxBzQiF0WwuMsK8wFcE/wAw1FF0LzmP2yoyLNXFSSEzWasRsAq/ICoiqkisGDKRjwIWSUGmX4HHIhgDc2SIgaAxyZodIiwSILNVGWWx34kBrPtqSPua3sOTjtAqrYqiP7hJNsiwKAVm0GyRTYEZU2RXHENtGtue0vTc1rgjp97NmI6YUHNZoS2ABkRfFQSTk8xwb0VmENyf8AwhIwZ8/x9SlYbas5EmyOT9/UraHIIPIg3sBUOdolFhYBFDixIClcZl/ryi1XIMICwLEUTXBhuO0YsylXFnBlIo2ksePAgZhSSQLx9QKkEiu0vaBbA8wOgGF7u0NY/qEBqqlCwcjYtMfKgayblA55JH3DUu6epRHx7VIwue58zUIpFXUjVoDaYOGe0ZLDmZtQJW89poCwJLXxjxBmVHW0vFXCasJWVTQOfuSSQ2VjByQcRkWL7AQnMTj6hLpPUIa5IDcgvOZQUA7QOBCEOauxPiCkiEIFA3z0BpquoQtjPmEIWE5wfqZoxDDPIhCGp014FjmTqAhAQxhCGd6hb2GMSCSxNm6hCHSQbd6tZOBcQJrPeEIX7UQKJqLhD9iEIL2gEVlBCEIR//Z";
    
    // call api
    given()
      .body(body)
      .cookie("JSESSIONID", sessionId)
      .when().post("/message-threads/{id}/photo", messageThreadId)
      .then().statusCode(200);
  }
  
  @Test
  public void updatePhoto_notMember() {
    sessionId = login("otherCommunityUser", "pass");
    
    // prepare
    String body = "data:image/jpeg;base64,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";
    
    // call api
    given()
      .body(body)
      .cookie("JSESSIONID", sessionId)
      .when().post("/message-threads/{id}/photo", messageThreadId)
      .then().statusCode(400);
  }
}
