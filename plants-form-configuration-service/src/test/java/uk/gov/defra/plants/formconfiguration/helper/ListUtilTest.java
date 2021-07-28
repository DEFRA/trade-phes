package uk.gov.defra.plants.formconfiguration.helper;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import uk.gov.defra.plants.formconfiguration.helper.ListUtil;
import uk.gov.defra.plants.formconfiguration.model.Direction;

public class ListUtilTest {

  private List<Integer> list = ImmutableList.of(1, 2, 3);

  @Test
  public void shouldMoveObjectUpList_objectInMiddle() {
    assertThat(ListUtil.moveInList(list, 2, Direction.UP)).containsExactly(2, 1, 3);
  }

  @Test
  public void shouldMoveObjectUpList_objectAtEnd() {
    assertThat(ListUtil.moveInList(list, 3, Direction.UP)).containsExactly(1, 3, 2);
  }

  @Test
  public void shouldMoveObjectDownList_objectInMiddle() {
    assertThat(ListUtil.moveInList(list, 2, Direction.DOWN)).containsExactly(1, 3, 2);
  }

  @Test
  public void shouldMoveObjectDownList_objectAtStart() {
    assertThat(ListUtil.moveInList(list, 1, Direction.DOWN)).containsExactly(2, 1, 3);
  }

  @Test
  public void shouldNotAlterListWhenMovingUpAndObjectIsAtStart() {
    assertThat(ListUtil.moveInList(list, 1, Direction.UP)).isEqualTo(list);
  }

  @Test
  public void shouldNotAlterListWhenMovingDownAndObjectIsAtEnd() {
    assertThat(ListUtil.moveInList(list, 3, Direction.DOWN)).isEqualTo(list);
  }
}
