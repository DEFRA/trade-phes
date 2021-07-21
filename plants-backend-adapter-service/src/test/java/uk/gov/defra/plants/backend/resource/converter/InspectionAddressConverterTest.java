package uk.gov.defra.plants.backend.resource.converter;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.PHEATS_INSPECTION_ADDRESSES;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.PHEATS_NOT_APPROVED_INSPECTION_ADDRESSES;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.TRADER_INSPECTION_ADDRESS;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.INSPECTION_ADDRESS;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.INSPECTION_ADDRESSES;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.TRADER_PHEATS_INSPECTION_ADDRESS;
import static uk.gov.defra.plants.backend.service.inspection.TradeAPIInspectionAddressTestData.TRADER_PHEATS_UNAPPROVED_INSPECTION_ADDRESS;

import java.util.List;
import org.junit.Test;
import uk.gov.defra.plants.backend.representation.inspection.TraderInspectionAddress;

public class InspectionAddressConverterTest {

  private InspectionAddressConverter converter;
  private List<TraderInspectionAddress> traderInspectionAddresses;
  private List<TraderInspectionAddress> traderPheatsInspectionAddresses;
  private List<TraderInspectionAddress> traderPheatsNotApprovedInspectionAddresses;
  private TraderInspectionAddress traderInspectionAddress;

  @Test
  public void convertsInspectionAddressesToExporterInspectionAddresses() {
    givenAConverter();
    whenICallToConvertAddresses();
    thenTheInspectionAddressesAreConverted();
  }

  @Test
  public void convertsInspectionAddressesToPheatsExporterInspectionAddresses() {
    givenAConverter();
    whenICallToConvertPheatsAddresses();
    thenThePheatsInspectionAddressesAreConverted();
  }

  @Test
  public void convertsInspectionAddressesToPheatsNotApprovedExporterInspectionAddresses() {
    givenAConverter();
    whenICallToConvertPheatsUnApprovedAddresses();
    thenTheUnApprovedInspectionAddressesAreConverted();
  }

  @Test
  public void convertsInspectionAddressToExporterInspectionAddress() {
    givenAConverter();
    whenICallToConvertAddress();
    thenTheInspectionAddressIsConverted();
  }

  private void givenAConverter() {
    converter = new InspectionAddressConverter();
  }

  private void whenICallToConvertAddresses() {
    traderInspectionAddresses = converter.convert(INSPECTION_ADDRESSES);
  }

  private void whenICallToConvertPheatsUnApprovedAddresses() {
    traderPheatsNotApprovedInspectionAddresses = converter.convert(PHEATS_NOT_APPROVED_INSPECTION_ADDRESSES);
  }

  private void whenICallToConvertPheatsAddresses() {
    traderPheatsInspectionAddresses = converter.convert(PHEATS_INSPECTION_ADDRESSES);
  }

  private void whenICallToConvertAddress() {
    traderInspectionAddress = converter.convert(INSPECTION_ADDRESS);
  }

  private void thenTheInspectionAddressesAreConverted() {
    assertThat(traderInspectionAddresses, hasSize(1));
    assertThat(traderInspectionAddresses, hasItem(TRADER_INSPECTION_ADDRESS));
  }

  private void thenTheUnApprovedInspectionAddressesAreConverted() {
    assertThat(traderPheatsNotApprovedInspectionAddresses, hasSize(1));
    assertThat(traderPheatsNotApprovedInspectionAddresses, hasItem(TRADER_PHEATS_UNAPPROVED_INSPECTION_ADDRESS));
  }

  private void thenThePheatsInspectionAddressesAreConverted() {
    assertThat(traderPheatsInspectionAddresses, hasSize(1));
    assertThat(traderPheatsInspectionAddresses, hasItem(TRADER_PHEATS_INSPECTION_ADDRESS));
  }

  private void thenTheInspectionAddressIsConverted() {
    assertTrue(traderInspectionAddress.equals(TRADER_INSPECTION_ADDRESS));
  }
}