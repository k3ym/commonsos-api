package commonsos.repository;

import static java.util.Optional.empty;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;

import commonsos.exception.MessageThreadNotFoundException;
import commonsos.repository.entity.MessageThread;
import commonsos.repository.entity.MessageThreadParty;
import commonsos.repository.entity.ResultList;
import commonsos.repository.entity.User;
import commonsos.service.command.PaginationCommand;

@Singleton
public class MessageThreadRepository extends Repository {

  @Inject
  public MessageThreadRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Optional<MessageThread> byCreaterAndAdId(User user, Long adId) {
    try {
      return Optional.of(em().createQuery("FROM MessageThread WHERE adId = :adId AND createdBy = :createdBy AND deleted IS FALSE", MessageThread.class)
        .setLockMode(lockMode())
        .setParameter("adId", adId)
        .setParameter("createdBy", user.getId())
        .getSingleResult());
    }
    catch (NoResultException e) {
      return empty();
    }
  }

  public ResultList<MessageThread> byAdId(Long adId, PaginationCommand pagination) {
    StringBuilder sql = new StringBuilder();
    sql.append("FROM MessageThread WHERE adId = :adId AND deleted IS false ORDER BY id");

    TypedQuery<MessageThread> query = em().createQuery(sql.toString(), MessageThread.class)
      .setLockMode(lockMode())
      .setParameter("adId", adId);

    ResultList<MessageThread> resultList = getResultList(query, pagination);
    return resultList;
  }

  public Optional<MessageThread> betweenUsers(Long userId1, Long userId2, Long communityId) {
    String sql = "SELECT mt FROM MessageThread mt " +
      "WHERE mt.communityId = :communityId " +
      "AND mt.adId IS NULL " +
      "AND mt.group IS FALSE " +
      "AND mt.deleted IS FALSE " +
      "AND EXISTS (SELECT 1 FROM MessageThreadParty mtp WHERE mtp.messageThreadId = mt.id AND mtp.user.id = :userId1) " +
      "AND EXISTS (SELECT 1 FROM MessageThreadParty mtp WHERE mtp.messageThreadId = mt.id AND mtp.user.id = :userId2)";
    try {
      MessageThread singleResult = em().createQuery(sql, MessageThread.class)
        .setLockMode(lockMode())
        .setParameter("communityId", communityId)
        .setParameter("userId1", userId1)
        .setParameter("userId2", userId2)
        .getSingleResult();
      
      return Optional.of(singleResult);
    }
    catch (NoResultException e) {
      return empty();
    }
  }

  public MessageThread create(MessageThread messageThread) {
    em().persist(messageThread);
    return messageThread;
  }

  public ResultList<MessageThread> listByUser(User user, Long communityId, String memberFilter, String messageFilter, PaginationCommand pagination) {
    StringBuilder sql = new StringBuilder();
    sql.append(
        "SELECT " +
        "    mt " +
        "FROM MessageThread mt " +
        "JOIN mt.parties p " +
        "WHERE mt.communityId = :communityId " +
        "AND mt.deleted IS FALSE " +
        "AND p.user.id = :userId ");
    if (StringUtils.isNotBlank(memberFilter)) {
      sql.append(
          "AND EXISTS ( " +
          "    SELECT 1 FROM MessageThreadParty mtp " +
          "    WHERE mtp.messageThreadId = mt.id " +
          "    AND LOWER(mtp.user.username) LIKE LOWER(:memberFilter) " +
          ") ");
    }
    if (StringUtils.isNotBlank(messageFilter)) {
      sql.append(
          "AND EXISTS ( " +
          "    SELECT 1 FROM Message m " +
          "    WHERE m.threadId = mt.id " +
          "    AND m.text LIKE :messageFilter" +
          ") ");
    }
    sql.append("ORDER BY mt.id");

    TypedQuery<MessageThread> query = em().createQuery(sql.toString(), MessageThread.class);
    query.setLockMode(lockMode());
    query.setParameter("communityId", communityId);
    query.setParameter("userId", user.getId());
    if (StringUtils.isNotBlank(memberFilter)) {
      query.setParameter("memberFilter", "%" + memberFilter + "%");
    }
    if (StringUtils.isNotBlank(messageFilter)) {
      query.setParameter("messageFilter", "%" + messageFilter + "%");
    }
    
    ResultList<MessageThread> resultList = getResultList(query, pagination);
    resultList.getList().forEach(r -> em().lock(r, lockMode()));
    
    return resultList;
  }

  public Optional<MessageThread> findById(Long id) {
    try {
      return Optional.of(em().createQuery("FROM MessageThread WHERE id = :id AND deleted IS FALSE", MessageThread.class)
        .setLockMode(lockMode())
        .setParameter("id", id)
        .getSingleResult()
      );
    }
    catch (NoResultException e) {
        return empty();
    }
  }

  public MessageThread findStrictById(Long id) {
    return findById(id).orElseThrow(MessageThreadNotFoundException::new);
  }

  public void update(MessageThreadParty party) {
    em().merge(party);
  }

  public void update(MessageThread messageThread) {
    em().merge(messageThread);
  }

  public int deleteMessageThreadParty(User user) {
    return em().createQuery(
      "DELETE FROM MessageThreadParty mtp WHERE mtp.user = :user " +
      "AND EXISTS (SELECT mt FROM MessageThread mt WHERE mt.id = mtp.messageThreadId AND (mt.adId IS NOT NULL OR mt.group IS TRUE))")
      .setParameter("user", user).executeUpdate();
  }

  public int deleteMessageThreadParty(User user, Long threadId) {
    return em().createQuery(
      "DELETE FROM MessageThreadParty mtp WHERE mtp.user = :user " +
      "AND mtp.messageThreadId = :threadId")
      .setParameter("user", user)
      .setParameter("threadId", threadId)
      .executeUpdate();
  }

  public List<Long> unreadMessageThreadIds(User user, Long communityId) {
    return em().createQuery(
      "SELECT mt.id " +
        "FROM MessageThread mt JOIN mt.parties p " +
        "WHERE mt.communityId = :communityId " +
        "AND p.user = :user " +
        "AND EXISTS (SELECT 1 FROM Message WHERE threadId = mt.id) "+
        "AND (p.visitedAt IS NULL OR p.visitedAt < (SELECT MAX(createdAt) FROM Message WHERE threadId = mt.id)) " +
        "ORDER BY mt.id", Long.class)
      .setParameter("communityId", communityId)
      .setParameter("user", user)
      .getResultList();
  }
}
