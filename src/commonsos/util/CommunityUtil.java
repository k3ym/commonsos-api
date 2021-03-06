package commonsos.util;

import java.util.ArrayList;
import java.util.List;

import commonsos.repository.entity.Community;
import commonsos.repository.entity.CommunityNotification;
import commonsos.view.CommunityNotificationView;
import commonsos.view.CommunityView;

public class CommunityUtil {
  
  private CommunityUtil() {}

  public static CommunityView view(Community community, String tokenSymbol) {
    Long adminUserId = community.getAdminUser() == null ? null : community.getAdminUser().getId();
    return new CommunityView()
        .setId(community.getId())
        .setName(community.getName())
        .setAdminUserId(adminUserId)
        .setDescription(community.getDescription())
        .setTokenSymbol(tokenSymbol)
        .setPhotoUrl(community.getPhotoUrl())
        .setCoverPhotoUrl(community.getCoverPhotoUrl());
  }

  public static List<CommunityNotificationView> notificationView(List<CommunityNotification> notificationList) {
    List<CommunityNotificationView> notificationViewList = new ArrayList<>();
    notificationList.forEach(n -> notificationViewList.add(notificationView(n)));
    
    return notificationViewList;
  }

  public static CommunityNotificationView notificationView(CommunityNotification notification) {
    return new CommunityNotificationView()
        .setWordpressId(notification.getWordpressId())
        .setUpdatedAt(notification.getUpdatedAt());
  }
}
