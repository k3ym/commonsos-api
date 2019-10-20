package commonsos.controller.app.ad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.gson.Gson;

import commonsos.command.app.CreateAdCommand;
import commonsos.controller.app.ad.CreateAdController;
import commonsos.repository.entity.AdType;
import commonsos.repository.entity.User;
import commonsos.service.AdService;
import commonsos.view.AdView;
import spark.Request;

@ExtendWith(MockitoExtension.class)
public class AdCreateControllerTest {

  @Mock Request request;
  @Mock AdService service;
  @Captor ArgumentCaptor<CreateAdCommand> commandCaptor;
  @InjectMocks CreateAdController controller;

  @BeforeEach
  public void setUp() throws Exception {
    controller.gson = new Gson();
  }

  @Test
  public void handle() throws Exception {
    when(request.body()).thenReturn("{\"communityId\": 123, \"title\": \"title\", \"description\": \"description\", \"points\": \"123.456\", \"location\": \"location\", \"type\": \"GIVE\"}");
    User user = new User();
    AdView adView = new AdView();
    when(service.create(eq(user), commandCaptor.capture())).thenReturn(adView);

    AdView result = controller.handleAfterLogin(user, request, null);

    assertThat(result).isEqualTo(adView);
    CreateAdCommand ad = commandCaptor.getValue();
    assertThat(ad.getCommunityId()).isEqualTo(Long.valueOf(123L));
    assertThat(ad.getTitle()).isEqualTo("title");
    assertThat(ad.getDescription()).isEqualTo("description");
    assertThat(ad.getPoints()).isEqualTo(new BigDecimal("123.456"));
    assertThat(ad.getLocation()).isEqualTo("location");
    assertThat(ad.getType()).isEqualTo(AdType.GIVE);
  }
}