package uk.gov.defra.plants.formconfiguration.helper;

import java.util.ArrayList;
import java.util.List;
import uk.gov.defra.plants.formconfiguration.model.Direction;

public class ListUtil {

  private ListUtil() {
  }

  public static <T> List<T> moveInList(List<T> list, T objectBeingMoved, Direction direction) {
    int index = list.indexOf(objectBeingMoved);

    boolean canMoveObject;
    int newIndex;

    if (Direction.DOWN.equals(direction)) {
      newIndex = index + 1;
      canMoveObject = !(newIndex >= list.size());
    } else {
      newIndex = index - 1;
      canMoveObject = !(newIndex < 0);
    }

    List<T> retList;

    if (canMoveObject) {
      retList = new ArrayList<>(list);
      retList.remove(objectBeingMoved);
      retList.add(newIndex, objectBeingMoved);
    } else {
      retList = list;
    }

    return retList;
  }
}
