package commonsos.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commonsos.command.PaginationCommand;
import commonsos.repository.entity.Community;
import commonsos.repository.entity.Redistribution;
import commonsos.repository.entity.SortType;
import commonsos.repository.entity.User;

public class RedistributionRepositoryTest extends AbstractRepositoryTest {

  private RedistributionRepository repository = spy(new RedistributionRepository(emService));
  private CommunityRepository communityRepository = spy(new CommunityRepository(emService));
  private UserRepository userRepository = spy(new UserRepository(emService));

  @BeforeEach
  public void ignoreCheckLocked() {
    doNothing().when(repository).checkLocked(any());
    doNothing().when(communityRepository).checkLocked(any());
    doNothing().when(userRepository).checkLocked(any());
  }
  
  @Test
  public void findById() {
    // prepare
    Redistribution r1 = inTransaction(() -> repository.create(new Redistribution()));
    Redistribution r2 = inTransaction(() -> repository.create(new Redistribution().setDeleted(true)));

    // execute & verify
    Optional<Redistribution> result = repository.findById(r1.getId());
    assertThat(result.get().getId()).isEqualTo(r1.getId());

    // execute & verify
    result = repository.findById(r2.getId());
    assertFalse(result.isPresent());

    // execute & verify
    result = repository.findById(-1L);
    assertFalse(result.isPresent());
  }

  @Test
  public void findByCommunityId() {
    // prepare
    Community com1 = inTransaction(() -> communityRepository.create(new Community().setName("com1")));
    Community com2 = inTransaction(() -> communityRepository.create(new Community().setName("com2")));
    Community com3 = inTransaction(() -> communityRepository.create(new Community().setName("com3")));
    Redistribution r1 = inTransaction(() -> repository.create(new Redistribution().setCommunity(com1)));
    Redistribution r2 = inTransaction(() -> repository.create(new Redistribution().setCommunity(com1)));
    Redistribution r3 = inTransaction(() -> repository.create(new Redistribution().setCommunity(com1).setDeleted(true)));
    Redistribution r4 = inTransaction(() -> repository.create(new Redistribution().setCommunity(com2)));

    // execute & verify
    List<Redistribution> result = repository.searchByCommunityId(com1.getId(), null).getList();
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(0).getId()).isEqualTo(r1.getId());
    assertThat(result.get(1).getId()).isEqualTo(r2.getId());

    // execute & verify
    result = repository.searchByCommunityId(com2.getId(), null).getList();
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getId()).isEqualTo(r4.getId());

    // execute & verify
    result = repository.searchByCommunityId(com3.getId(), null).getList();
    assertThat(result.size()).isEqualTo(0);
  }

  @Test
  public void findByCommunityId_pagination() {
    // prepare
    Community com1 = inTransaction(() -> communityRepository.create(new Community().setName("com1")));
    inTransaction(() -> repository.create(new Redistribution().setCommunity(com1)));
    inTransaction(() -> repository.create(new Redistribution().setCommunity(com1)));
    inTransaction(() -> repository.create(new Redistribution().setCommunity(com1)));
    inTransaction(() -> repository.create(new Redistribution().setCommunity(com1)));
    inTransaction(() -> repository.create(new Redistribution().setCommunity(com1)));
    inTransaction(() -> repository.create(new Redistribution().setCommunity(com1)));
    inTransaction(() -> repository.create(new Redistribution().setCommunity(com1)));
    inTransaction(() -> repository.create(new Redistribution().setCommunity(com1)));
    inTransaction(() -> repository.create(new Redistribution().setCommunity(com1)));
    inTransaction(() -> repository.create(new Redistribution().setCommunity(com1)));
    inTransaction(() -> repository.create(new Redistribution().setCommunity(com1)));
    inTransaction(() -> repository.create(new Redistribution().setCommunity(com1)));

    // execute & verify
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(10).setSort(SortType.ASC);
    List<Redistribution> result = repository.searchByCommunityId(com1.getId(), pagination).getList();
    assertThat(result.size()).isEqualTo(10);

    // execute & verify
    pagination.setPage(1);
    result = repository.searchByCommunityId(com1.getId(), pagination).getList();
    assertThat(result.size()).isEqualTo(2);
  }

  @Test
  public void findByUserId() {
    // prepare
    User user1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")));
    User user2 = inTransaction(() -> userRepository.create(new User().setUsername("user1")));
    Redistribution r1 = inTransaction(() -> repository.create(new Redistribution().setUser(user1)));
    Redistribution r2 = inTransaction(() -> repository.create(new Redistribution().setUser(user1)));
    Redistribution r3 = inTransaction(() -> repository.create(new Redistribution().setUser(user1).setDeleted(true)));

    // execute & verify
    List<Redistribution> result = repository.searchByUserId(user1.getId(), null).getList();
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(0).getId()).isEqualTo(r1.getId());
    assertThat(result.get(1).getId()).isEqualTo(r2.getId());

    // execute & verify
    result = repository.searchByCommunityId(user2.getId(), null).getList();
    assertThat(result.size()).isEqualTo(0);
  }

  @Test
  public void findByUserId_pagination() {
    // prepare
    User user1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")));
    inTransaction(() -> repository.create(new Redistribution().setUser(user1)));
    inTransaction(() -> repository.create(new Redistribution().setUser(user1)));
    inTransaction(() -> repository.create(new Redistribution().setUser(user1)));
    inTransaction(() -> repository.create(new Redistribution().setUser(user1)));
    inTransaction(() -> repository.create(new Redistribution().setUser(user1)));
    inTransaction(() -> repository.create(new Redistribution().setUser(user1)).setDeleted(true));

    // execute & verify
    PaginationCommand pagination = new PaginationCommand().setPage(0).setSize(3).setSort(SortType.DESC);
    List<Redistribution> result = repository.searchByUserId(user1.getId(), pagination).getList();
    assertThat(result.size()).isEqualTo(3);

    // execute & verify
    pagination.setPage(1);
    result = repository.searchByUserId(user1.getId(), pagination).getList();
    assertThat(result.size()).isEqualTo(2);
  }

  @Test
  public void sumByCommunityId() {
    // prepare
    Community com1 = inTransaction(() -> communityRepository.create(new Community().setName("com1")));
    Community com2 = inTransaction(() -> communityRepository.create(new Community().setName("com2")));
    inTransaction(() -> repository.create(new Redistribution().setCommunity(com1).setRate(new BigDecimal("1.5"))));
    inTransaction(() -> repository.create(new Redistribution().setCommunity(com1).setRate(new BigDecimal("1.25"))));
    inTransaction(() -> repository.create(new Redistribution().setCommunity(com1).setRate(new BigDecimal("0.1"))));
    inTransaction(() -> repository.create(new Redistribution().setCommunity(com1).setRate(new BigDecimal("90")).setDeleted(true)));

    // execute & verify
    BigDecimal result = repository.sumByCommunityId(com1.getId());
    assertThat(result.stripTrailingZeros()).isEqualTo(new BigDecimal("2.85"));

    // execute & verify
    result = repository.sumByCommunityId(com2.getId());
    assertThat(result.stripTrailingZeros()).isEqualTo(new BigDecimal("0"));
  }

  @Test
  public void create() {
    // create
    Community com = inTransaction(() -> communityRepository.create(new Community().setName("com")));
    User user = inTransaction(() -> userRepository.create(new User().setUsername("user")));
    Redistribution r = inTransaction(() -> repository.create(
        new Redistribution()
          .setRate(new BigDecimal("1.5"))
          .setCommunity(new Community().setId(com.getId()))
          .setUser(new User().setId(user.getId()))));

    // find
    Redistribution result = em().find(Redistribution.class, r.getId());
    
    // verify
    assertThat(result.getId()).isEqualTo(r.getId());
    assertThat(result.getRate().stripTrailingZeros()).isEqualTo(new BigDecimal("1.5"));
    assertThat(result.getCommunity().getName()).isEqualTo("com");
    assertThat(result.getUser().getUsername()).isEqualTo("user");
  }
  
  @Test
  public void update() {
    // create
    Community com1 = inTransaction(() -> communityRepository.create(new Community().setName("com1")));
    Community com2 = inTransaction(() -> communityRepository.create(new Community().setName("com2")));
    User user1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")));
    User user2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")));
    Redistribution r = inTransaction(() -> repository.create(
        new Redistribution()
          .setRate(new BigDecimal("1.5"))
          .setCommunity(new Community().setId(com1.getId()))
          .setUser(new User().setId(user1.getId()))));

    // update
    Redistribution r2 = inTransaction(() -> repository.update(r.setRate(new BigDecimal("0.999"))
          .setCommunity(new Community().setId(com2.getId()))
          .setUser(new User().setId(user2.getId()))));
    
    // find
    Redistribution result = em().find(Redistribution.class, r2.getId());
    
    // verify
    assertThat(result.getRate().stripTrailingZeros()).isEqualTo(new BigDecimal("0.999"));
    assertThat(result.getCommunity().getName()).isEqualTo("com2");
    assertThat(result.getUser().getUsername()).isEqualTo("user2");
  }
}