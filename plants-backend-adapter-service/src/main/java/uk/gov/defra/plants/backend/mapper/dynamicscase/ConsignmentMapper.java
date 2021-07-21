package uk.gov.defra.plants.backend.mapper.dynamicscase;

import static uk.gov.defra.plants.backend.util.CaseMapperUtil.getAnswerForMergedFormQuestion;
import static uk.gov.defra.plants.common.constants.TradeMappedFields.CONSIGNEE_ADDRESS;
import static uk.gov.defra.plants.common.constants.TradeMappedFields.CONSIGNEE_NAME;
import static uk.gov.defra.plants.common.constants.TradeMappedFields.CONSIGNEE_VALUE;
import static uk.gov.defra.plants.common.constants.TradeMappedFields.IMPORT_PERMIT_NUMBER;
import static uk.gov.defra.plants.common.constants.TradeMappedFields.POINT_OF_ENTRY;
import static uk.gov.defra.plants.common.constants.TradeMappedFields.TRANSPORT_MODE;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.plants.applicationform.representation.Consignment;
import uk.gov.defra.plants.dynamics.representation.CommodityTradeGroup;
import uk.gov.defra.plants.dynamics.representation.Consignee;
import uk.gov.defra.plants.dynamics.representation.OtherConsignmentDetails;
import uk.gov.defra.plants.dynamics.representation.TradeAPIApplication.TradeAPIApplicationBuilder;
import uk.gov.defra.plants.dynamics.representation.TransportMode;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class ConsignmentMapper implements CaseFieldMapper {

  private final TradeAPICommodityBuilderFactory tradeAPICommodityBuilderFactory;

  @Override
  public void map(final CaseContext context, final TradeAPIApplicationBuilder builder) {
    Consignment firstConsignment = context.getApplicationForm().getConsignments().get(0);

    builder.consignment(
        uk.gov.defra.plants.dynamics.representation.Consignment.builder()
            .consignmentId(firstConsignment.getConsignmentId())
            .commodityGroup(CommodityTradeGroup.fromString(context.getApplicationForm().getCommodityGroup()))
            .commodities(buildCommodities(context, firstConsignment))
            .consignee(buildConsignee(context))
            .otherDetails(
                OtherConsignmentDetails.builder()
                    .valueAmountInPounds(getConsigneeValue(context))
                    .pointOfEntry(
                        getAnswerForMergedFormQuestion(POINT_OF_ENTRY.getMappingName(), context))
                    .transportMode(getTransportMode(context))
                    .build())
            .build());
  }

  private TransportMode getTransportMode(CaseContext context) {
    return TransportMode.forValue(
        Objects.requireNonNull(
            getAnswerForMergedFormQuestion(TRANSPORT_MODE.getMappingName(), context)));
  }

  private Number getConsigneeValue(CaseContext context) {
    String consigneeValueAnswer = getAnswerForMergedFormQuestion(CONSIGNEE_VALUE.getMappingName(),
        context);
    Number consigneeValue = null;

    if (StringUtils.isNotEmpty(consigneeValueAnswer)) {
      boolean matchesDecimalPattern = Pattern.compile("^[0-9]*\\.[0-9]+$")
          .matcher(consigneeValueAnswer)
          .matches();

      if (matchesDecimalPattern) {
        consigneeValue = Double.valueOf(consigneeValueAnswer);
      } else {
        consigneeValue = Long.valueOf(consigneeValueAnswer);
      }
    }
    return consigneeValue;
  }

  private Consignee buildConsignee(CaseContext context) {
    return Consignee.builder()
        .consigneeName(getAnswerForMergedFormQuestion(CONSIGNEE_NAME.getMappingName(), context))
        .consigneeAddressLine1(
            getAnswerForMergedFormQuestion(CONSIGNEE_ADDRESS.getMappingName(), context))
        .importPermitNumber(
            getAnswerForMergedFormQuestion(IMPORT_PERMIT_NUMBER.getMappingName(), context))
        .build();
  }

  private List<Object> buildCommodities(
      CaseContext context, Consignment consignment) {
    return consignment.getCommodities().stream()
        .map(
            commodity -> {
              String commodityGroup = context.getApplicationForm().getCommodityGroup();
              return tradeAPICommodityBuilderFactory
                  .getTradeAPICommodityBuilder(CommodityGroup.valueOf(commodityGroup))
                  .buildCommodity(commodity);
            })
        .collect(Collectors.toList());
  }
}
