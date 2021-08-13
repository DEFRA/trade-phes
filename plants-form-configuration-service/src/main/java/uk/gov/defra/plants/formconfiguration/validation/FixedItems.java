package uk.gov.defra.plants.formconfiguration.validation;

public enum FixedItems {
  COUNTRY_OF_EXPORT("ukcountryofexport"),
  APPLICANT_NAME("applicantname"),
  INSPECTION_DATE("inspectiondate"),
  DESTINATION_COUNTRY("destinationcountry");

  private String value;

  FixedItems(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
