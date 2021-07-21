package uk.gov.defra.plants.backend.mapper.dynamicscase;

import uk.gov.defra.plants.applicationform.representation.ReforwardingDetails;
import uk.gov.defra.plants.dynamics.representation.Reforwarding;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication.TradeAPIApplicationBuilder;

public class ReforwardingDetailsMapper implements CaseFieldMapper {

  @Override
  public void map(final CaseContext context, final TradeAPIApplicationBuilder builder) {
    final ReforwardingDetails reforwardingDetails =
        context.getApplicationForm().getReforwardingDetails();
    if (reforwardingDetails != null) {
      builder.reforwardingDetails(
          Reforwarding.builder()
              .importPhytoNumber(reforwardingDetails.getImportCertificateNumber())
              .countryOfOrigin(reforwardingDetails.getOriginCountry())
              .repackingContainer(reforwardingDetails.getConsignmentRepackaging().getTradeAPIName())
              .build());
    }
  }
}
