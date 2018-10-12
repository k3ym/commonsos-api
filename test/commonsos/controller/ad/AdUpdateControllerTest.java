package commonsos.controller.ad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.gson.Gson;

import commonsos.domain.ad.Ad;
import commonsos.domain.ad.AdService;
import commonsos.domain.ad.AdType;
import commonsos.domain.ad.AdUpdateCommand;
import commonsos.domain.ad.AdView;
import commonsos.domain.auth.User;
import spark.Request;

@RunWith(MockitoJUnitRunner.class)
public class AdUpdateControllerTest {

  @Mock Request request;
  @Mock AdService adService;
  @Captor ArgumentCaptor<AdUpdateCommand> commandCaptor;
  @InjectMocks AdUpdateController controller;

  @Before
  public void setUp() throws Exception {
    controller.gson = new Gson();
  }

  @Test
  public void handle() throws Exception {
    // prepare
    when(request.body()).thenReturn("{"
        + " \"title\": \"title\","
        + " \"description\": \"description\","
        + " \"amount\": \"123.456\","
        + " \"location\": \"location\","
        + " \"type\": \"GIVE\""
        + "}");
    when(request.params("id")).thenReturn("123");
    User user = new User();
    
    Ad ad = new Ad();
    when(adService.updateAd(any(), any())).thenReturn(ad);
    AdView adView = new AdView();
    when(adService.view(ad, user)).thenReturn(adView);
    
    // execute
    AdView result = controller.handle(user, request, null);
    
    // verify
    assertThat(result).isEqualTo(adView);
    
    verify(adService).updateAd(any(), commandCaptor.capture());
    AdUpdateCommand actualCommand = commandCaptor.getValue();
    assertThat(actualCommand.getId()).isEqualTo(123L);
    assertThat(actualCommand.getTitle()).isEqualTo("title");
    assertThat(actualCommand.getDescription()).isEqualTo("description");
    assertThat(actualCommand.getAmount()).isEqualTo(new BigDecimal("123.456"));
    assertThat(actualCommand.getLocation()).isEqualTo("location");
    assertThat(actualCommand.getType()).isEqualTo(AdType.GIVE);
  }

  @Test(expected = NumberFormatException.class)
  public void handle_parseError() throws Exception {
    // prepare
    when(request.params("id")).thenReturn("string");
    
    // execute
    controller.handle(null, request, null);
  }
}