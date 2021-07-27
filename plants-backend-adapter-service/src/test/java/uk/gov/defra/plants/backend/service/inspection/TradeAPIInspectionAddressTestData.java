package uk.gov.defra.plants.backend.service.inspection;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import uk.gov.defra.plants.backend.representation.inspection.Approval;
import uk.gov.defra.plants.backend.representation.inspection.InspectionAddress;
import uk.gov.defra.plants.backend.representation.inspection.PostalAddress;
import uk.gov.defra.plants.backend.representation.inspection.TraderInspectionAddress;

public class TradeAPIInspectionAddressTestData {

  public static Approval pheatsApproval = Approval.builder().type("Pheats").status("true").build();
  public static Approval pheatsNotApproved = Approval.builder().type("Pheats").status("false").build();

  public static PostalAddress POSTAL_ADDRESS =
      PostalAddress.builder()
          .id(4)
          .postalAddressId(UUID.fromString("305c7aa9-4a00-4013-a5fa-4262a7f4a158"))
          .uniquePropertyReferenceNumber("200001275355")
          .uniqueDeliveryPointReferenceNumber("27466567")
          .addressLine1("BITCHA GREEN FARM")
          .addressLine2("FARNDALE")
          .addressLine3("KIRKBYMOORSIDE")
          .town("YORK")
          .province(null)
          .country("UK")
          .postalCode("YO62 7LB")
          .defraAreaCodeCode("NE")
          .locationId(0)
          .createdOn(new Date())
          .createdBy("api")
          .createdSystem("CustExt.UI")
          .lastUpdated(new Date())
          .lastUpdatedBy(null)
          .lastUpdatedSystem(null)
          .isActive(true)
          .build();

  public static InspectionAddress INSPECTION_ADDRESS =
      InspectionAddress.builder()
        .id(7)
        .locationId(UUID.fromString("facadb07-4734-4177-a07e-1e39c25a182d"))
        .tradeAndTransportLocationId(0)
        .gridReference("SE675965")
        .approvals(null)
        .northing(496681)
        .easting(467395)
        .postalAddress(POSTAL_ADDRESS)
        .build();

  public static InspectionAddress PHEATS_INSPECTION_ADDRESS =
      InspectionAddress.builder()
          .id(8)
          .locationId(UUID.fromString("8d8b8911-d876-468a-90bb-16e8f3d8cc3e"))
          .tradeAndTransportLocationId(0)
          .gridReference("SE675965")
          .approvals(Collections.singletonList(pheatsApproval))
          .northing(496681)
          .easting(467395)
          .postalAddress(POSTAL_ADDRESS)
          .build();

  public static InspectionAddress PHEATS_INSPECTION_ADDRESS_NOT_APPROVED =
      InspectionAddress.builder()
          .id(8)
          .locationId(UUID.fromString("8d8b8911-d876-468a-90bb-16e8f3d8cc3e"))
          .tradeAndTransportLocationId(0)
          .gridReference("SE675965")
          .approvals(Collections.singletonList(pheatsNotApproved))
          .northing(496681)
          .easting(467395)
          .postalAddress(POSTAL_ADDRESS)
          .build();


  public static final TraderInspectionAddress TRADER_INSPECTION_ADDRESS =
      TraderInspectionAddress.builder()
          .locationId(INSPECTION_ADDRESS.getLocationId())
          .addressLine1(POSTAL_ADDRESS.getAddressLine1())
          .addressLine2(POSTAL_ADDRESS.getAddressLine2())
          .addressLine3(POSTAL_ADDRESS.getAddressLine3())
          .town(POSTAL_ADDRESS.getTown())
          .country(POSTAL_ADDRESS.getCountry())
          .postalCode(POSTAL_ADDRESS.getPostalCode())
          .province(POSTAL_ADDRESS.getProvince())
          .active(true)
          .build();

  public static final TraderInspectionAddress TRADER_PHEATS_INSPECTION_ADDRESS =
      TraderInspectionAddress.builder()
          .locationId(PHEATS_INSPECTION_ADDRESS.getLocationId())
          .addressLine1(POSTAL_ADDRESS.getAddressLine1())
          .addressLine2(POSTAL_ADDRESS.getAddressLine2())
          .addressLine3(POSTAL_ADDRESS.getAddressLine3())
          .town(POSTAL_ADDRESS.getTown())
          .pheats(true)
          .country(POSTAL_ADDRESS.getCountry())
          .postalCode(POSTAL_ADDRESS.getPostalCode())
          .province(POSTAL_ADDRESS.getProvince())
          .active(true)
          .build();

  public static final TraderInspectionAddress TRADER_PHEATS_UNAPPROVED_INSPECTION_ADDRESS =
      TraderInspectionAddress.builder()
          .locationId(PHEATS_INSPECTION_ADDRESS_NOT_APPROVED.getLocationId())
          .addressLine1(POSTAL_ADDRESS.getAddressLine1())
          .addressLine2(POSTAL_ADDRESS.getAddressLine2())
          .addressLine3(POSTAL_ADDRESS.getAddressLine3())
          .town(POSTAL_ADDRESS.getTown())
          .pheats(false)
          .country(POSTAL_ADDRESS.getCountry())
          .postalCode(POSTAL_ADDRESS.getPostalCode())
          .province(POSTAL_ADDRESS.getProvince())
          .active(true)
          .build();


  public static List<InspectionAddress> INSPECTION_ADDRESSES = Arrays.asList(INSPECTION_ADDRESS);

  public static List<InspectionAddress> PHEATS_INSPECTION_ADDRESSES = Arrays.asList(PHEATS_INSPECTION_ADDRESS);

  public static List<InspectionAddress> PHEATS_NOT_APPROVED_INSPECTION_ADDRESSES = Arrays.asList(PHEATS_INSPECTION_ADDRESS_NOT_APPROVED);

  public static List<TraderInspectionAddress> TRADER_INSPECTION_ADDRESSES = Arrays.asList(
      TRADER_INSPECTION_ADDRESS);

  public static List<TraderInspectionAddress> TRADER_PHEATS_INSPECTION_ADDRESSES = Arrays.asList(
      TRADER_PHEATS_INSPECTION_ADDRESS);

}
