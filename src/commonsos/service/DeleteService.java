package commonsos.service;

import static java.time.Instant.now;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.exception.BadRequestException;
import commonsos.exception.ForbiddenException;
import commonsos.repository.AdRepository;
import commonsos.repository.MessageRepository;
import commonsos.repository.MessageThreadRepository;
import commonsos.repository.UserRepository;
import commonsos.repository.entity.Ad;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.MessageThreadParty;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.User;
import commonsos.service.image.ImageUploadService;
import commonsos.util.MessageUtil;
import commonsos.util.UserUtil;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class DeleteService {

  @Inject private AdRepository adRepository;
  @Inject private UserRepository userRepository;
  @Inject private MessageThreadRepository messageThreadRepository;
  @Inject private MessageRepository messageRepository;
  @Inject private ImageUploadService imageService;

  public void deleteUser(User user) {
    log.info(String.format("deleting user. userId=%d", user.getId()));
    
    // delete user's ads
    List<Ad> myAds = adRepository.myAds(user.getId(), null).getList();
    myAds.forEach(ad -> deleteAdByUser(user, ad));
    
    // delete user's message threads party
    user.getCommunityUserList().forEach(cu -> {
      List<MessageThread> myThreads = messageThreadRepository.listByUser(user, cu.getCommunity().getId() ,null, null, null).getList();
      myThreads.forEach(thread -> deleteMessageThreadParty(user, thread.getId()));
    });
    
    // delete user's photo
    deletePhoto(user.getAvatarUrl());
    
    // delete user(logically)
    user.setDeleted(true);
    userRepository.update(user);

    log.info(String.format("deleted user. userId=%d", user.getId()));
  }
  
  public void deleteAdByUser(User user, Ad ad) {
    log.info(String.format("deleting ad by user. adId=%d, userId=%d", ad.getId(), user.getId()));
    
    // only creator is allowed to delete ad
    if (!ad.getCreatedBy().equals(user.getId())) throw new ForbiddenException();

    // delete ad's photo
    deleteAd(ad);
    
    log.info(String.format("deleted ad by user. adId=%d, userId=%d", ad.getId(), user.getId()));
  }
  
  public void deleteAdByAdmin(User admin, Ad ad) {
    log.info(String.format("deleting ad by admin. adId=%d, adminId=%d", ad.getId(), admin.getId()));
    
    // only admin is allowed to delete ad
    if (!UserUtil.isAdmin(admin, ad.getCommunityId()))throw new ForbiddenException();

    // delete ad's photo
    deleteAd(ad);
    
    log.info(String.format("deleted ad by admin. adId=%d, adminId=%d", ad.getId(), admin.getId()));
  }
  
  private void deleteAd(Ad ad) {
    // delete ad's photo
    deletePhoto(ad.getPhotoUrl());
    
    // delete ad's message-thread
    ResultList<MessageThread> messageThreadResult = messageThreadRepository.byAdId(ad.getId(), null);
    for (MessageThread messageThread : messageThreadResult.getList()) {
      deleteMessageThread(messageThread);
    }
    
    // delete ad(logically)
    ad.setDeleted(true);
    adRepository.update(ad);
  }

  public void deleteMessageThread(MessageThread messageThread) {
    log.info(String.format("deleting message-thread. threadId=%d", messageThread.getId()));
    
    // delete message-thread
    messageThread.setDeleted(true);
    messageThreadRepository.update(messageThread);
    
    log.info(String.format("deleted message-thread. threadId=%d", messageThread.getId()));
  }
  
  public void deleteMessageThreadParty(User user, Long threadId) {
    log.info(String.format("deleting message-thread-party. threadId=%d, userId=%d", threadId, user.getId()));
    
    // verify user is a member of thread
    MessageThread thread = messageThreadRepository.findStrictById(threadId);
    Optional<MessageThreadParty> userMtp = thread.getParties().stream().filter(p -> p.getUser().getId().equals(user.getId())).findFirst();
    if (!userMtp.isPresent()) throw new BadRequestException("User is not a member of thread");
    
    // delete message-thread-party's photo
    deletePhoto(userMtp.get().getPhotoUrl());
    
    // delete message-thread-party
    messageThreadRepository.deleteMessageThreadParty(user, threadId);
    
    // send system message to message-thread
    Message systemMessage = new Message()
        .setCreatedBy(MessageUtil.getSystemMessageCreatorId())
        .setCreatedAt(now())
        .setThreadId(thread.getId())
        .setText(MessageUtil.getSystemMessageUnsubscribe(user.getUsername()));
    messageRepository.create(systemMessage);

    log.info(String.format("deleted message-thread-party. threadId=%d, userId=%d", threadId, user.getId()));
  }
  
  public void deletePhoto(String photoUrl) {
    log.info(String.format("deleting photo. url=%s", photoUrl));
    imageService.delete(photoUrl);
    log.info(String.format("deleted photo. url=%s", photoUrl));
  }
}
