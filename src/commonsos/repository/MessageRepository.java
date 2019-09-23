package commonsos.repository;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.TypedQuery;

import commonsos.command.PaginationCommand;
import commonsos.repository.entity.Message;
import commonsos.repository.entity.ResultList;

@Singleton
public class MessageRepository extends Repository {

  @Inject
  public MessageRepository(EntityManagerService entityManagerService) {
    super(entityManagerService);
  }

  public Message create(Message message) {
    em().persist(message);
    return message;
  }

  public ResultList<Message> listByThread(Long threadId, PaginationCommand pagination) {
    TypedQuery<Message> query = em()
      .createQuery("FROM Message WHERE threadId = :threadId ORDER BY createdAt, id", Message.class)
      .setParameter("threadId", threadId);
    
    ResultList<Message> resultList = getResultList(query, pagination);
    
    return resultList;
  }

  public Optional<Message> lastMessage(Long threadId) {
    List<Message> messages = em().createQuery(
        "FROM Message " +
        "WHERE threadId = :threadId " +
        "ORDER BY createdAt DESC", Message.class)
      .setParameter("threadId", threadId)
      .setMaxResults(1)
      .getResultList();

    return messages.isEmpty() ? Optional.empty() : Optional.of(messages.get(0));
  }
}
