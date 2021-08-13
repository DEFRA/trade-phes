package uk.gov.defra.plants.applicationform.service.populators.commodity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class PopulatedValues {
  private List<Optional<String>> values = new ArrayList<>();

  public void populate(String value) {
    values.add(Optional.of(String.format("%s", value)));
  }

  public void populateIfPresent(String value) {
    if (StringUtils.isNotBlank(value)) {
      populate(value);
    }
  }

  public String toCSV() {
    List<String> filteredList =
        values.stream().flatMap(Optional::stream).collect(Collectors.toList());
    return String.join(", ", filteredList);
  }
}
