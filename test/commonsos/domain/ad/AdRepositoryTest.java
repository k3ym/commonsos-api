package commonsos.domain.ad;

import static commonsos.TestId.id;
import static commonsos.domain.ad.AdType.GIVE;
import static java.math.BigDecimal.TEN;
import static java.time.Instant.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import commonsos.DBTest;
import commonsos.domain.auth.User;
import commonsos.domain.auth.UserRepository;

public class AdRepositoryTest extends DBTest {

  private AdRepository repository = new AdRepository(entityManagerService);
  private UserRepository userRepository = new UserRepository(entityManagerService);

  @Test
  public void create() {
    Long id = inTransaction(() -> repository.create(new Ad()).getId());

    assertThat(em().find(Ad.class, id)).isNotNull();
  }

  @Test
  public void ads() {
    Long id1 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community1")).setDeleted(true)).getId());
    Long id4 = inTransaction(() -> repository.create(new Ad().setCommunityId(id("community2"))).getId());

    List<Ad> list = repository.ads(id("community1"));

    assertThat(list).extracting("id").containsExactly(id1, id2);
  }

  @Test
  public void ads_notFound() {
    List<Ad> list = repository.ads(id("community1"));

    assertThat(list.size()).isEqualTo(0);
  }

  @Test
  public void ads_filter_description() {
    Long userId1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")).getId());
    Long userId2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")).getId());
    Long userId3 = inTransaction(() -> userRepository.create(new User().setUsername("user3")).getId());
    Long id1 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId1).setDescription("text").setCommunityId(id("community1"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId2).setDescription("text").setCommunityId(id("community2"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId3).setDescription("text").setCommunityId(id("community3"))).getId());

    List<Ad> list = repository.ads(id("community1"), "text");

    assertThat(list).extracting("id").containsExactly(id1);
  }

  @Test
  public void ads_filter_title() {
    Long userId1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")).getId());
    Long userId2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")).getId());
    Long userId3 = inTransaction(() -> userRepository.create(new User().setUsername("user3")).getId());
    Long id1 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId1).setTitle("_title_").setCommunityId(id("community1"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId2).setTitle("_title_").setCommunityId(id("community2"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId3).setTitle("_title_").setCommunityId(id("community3"))).getId());

    List<Ad> list = repository.ads(id("community1"), "title");

    assertThat(list).extracting("id").containsExactly(id1);
  }

  @Test
  public void ads_filter_username() {
    Long userId1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")).getId());
    Long userId2 = inTransaction(() -> userRepository.create(new User().setUsername("user2")).getId());
    Long userId3 = inTransaction(() -> userRepository.create(new User().setUsername("user3")).getId());
    Long id1 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId1).setCommunityId(id("community1"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId2).setCommunityId(id("community2"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId3).setCommunityId(id("community3"))).getId());

    List<Ad> list = repository.ads(id("community1"), "user");

    assertThat(list).extracting("id").containsExactly(id1);
  }

  @Test
  public void ads_filter_deleted() {
    Long userId1 = inTransaction(() -> userRepository.create(new User().setUsername("user1")).getId());
    Long userId2 = inTransaction(() -> userRepository.create(new User().setUsername("user2").setDeleted(true)).getId());
    Long userId3 = inTransaction(() -> userRepository.create(new User().setUsername("user3")).getId());
    Long id1 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId1).setCommunityId(id("community1"))).getId());
    Long id2 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId2).setCommunityId(id("community1"))).getId());
    Long id3 = inTransaction(() -> repository.create(new Ad().setCreatedBy(userId3).setCommunityId(id("community1")).setDeleted(true)).getId());

    List<Ad> list = repository.ads(id("community1"), "user");

    assertThat(list).extracting("id").containsExactly(id1);
  }

  @Test
  public void ads_filter_notFound() {
    List<Ad> list = repository.ads(id("community1"), "user");

    assertThat(list.size()).isEqualTo(0);
  }

  @Test
  public void findById() {
    Long id = inTransaction(() -> repository.create(new Ad()
        .setTitle("Title")
        .setCreatedBy(id("john"))
        .setPoints(TEN).setType(GIVE)
        .setPhotoUrl("url://photo")
        .setCreatedAt(parse("2016-02-02T20:15:30Z"))
        .setDescription("description")
        .setLocation("home"))
      .getId());

    Ad result = repository.find(id).get();

    assertThat(result.getTitle()).isEqualTo("Title");
    assertThat(result.getCreatedBy()).isEqualTo(id("john"));
    assertThat(result.getType()).isEqualTo(GIVE);
    assertThat(result.getPhotoUrl()).isEqualTo("url://photo");
    assertThat(result.getCreatedAt()).isEqualTo(parse("2016-02-02T20:15:30Z"));
    assertThat(result.getDescription()).isEqualTo("description");
    assertThat(result.getLocation()).isEqualTo("home");
    assertThat(result.isDeleted()).isEqualTo(false);
  }

  @Test
  public void findById_deleted() {
    Long id = inTransaction(() -> repository.create(new Ad().setDeleted(true)).getId());

    Optional<Ad> result = repository.find(id);

    assertFalse(result.isPresent());
  }

  @Test
  public void findById_notFound() {
    Optional<Ad> result = inTransaction(() -> repository.find(id("unknown")));

    assertFalse(result.isPresent());
  }
}