package uk.gov.defra.plants.applicationform.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormItem;
import uk.gov.defra.plants.applicationform.representation.ApplicationFormStatus;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class PersistentApplicationForm {

  Long id;

  Long cloneParentId;

  @NonNull
  PersistentApplicationFormData data;
  @NonNull
  ApplicationFormStatus status;

  LocalDateTime created;

  LocalDateTime submitted;

  String destinationCountry;

  String commodityGroup;

  UUID exporterOrganisation;

  @Nullable
  UUID agencyOrganisation;

  boolean intermediary;
  Boolean pheats;
  UUID applicant;
  LocalDateTime lastUpdated;
  String reference;
  UUID applicationFormId;
  LocalDateTime dateNeeded;
  String inspectionContactName;
  String inspectionContactPhoneNumber;
  String inspectionContactEmail;
  UUID inspectionLocationId;
  String transportMode;
  String transportModeReferenceNumber;
  LocalDateTime inspectionDate;
  String inspectionSpecificLocation;

  @ColumnName("ehcNumber")
  String ehcNumber;

  @Nullable
  PersistentReforwardingDetails persistentReforwardingDetails;

  @Nullable
  List<PersistentConsignment> persistentConsignments;

  @Nullable
  PersistentPackerDetails persistentPackerDetails;

  public PersistentApplicationForm deletePageOccurrence(
      Integer pageNumber, Integer pageOccurrence) {

    PersistentApplicationFormData pafd = this.getData();

    List<ApplicationFormItem> applicationFormItems = pafd.getResponseItems();
    // remove all the unwanted ones and ensure that pageOccurrence remains continuous for the
    // given page:
    applicationFormItems =
        applicationFormItems.stream()
            .filter(
                afi ->
                    !(afi.getPageNumber().equals(pageNumber)
                        && afi.getPageOccurrence().equals(pageOccurrence)))
            .map(
                afi -> {
                  if (afi.getPageNumber().equals(pageNumber)
                      && afi.getPageOccurrence() > pageOccurrence) {
                    return afi.toBuilder().pageOccurrence(afi.getPageOccurrence() - 1).build();
                  } else {
                    return afi;
                  }
                })
            .collect(Collectors.toList());

    PersistentApplicationFormData modifiedData =
        pafd.toBuilder()
            .clearResponseItems()
            .responseItems(applicationFormItems)
            .build();

    return this.toBuilder().data(modifiedData).build();
  }
}
