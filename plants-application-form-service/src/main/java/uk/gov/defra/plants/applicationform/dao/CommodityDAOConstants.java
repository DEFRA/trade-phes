package uk.gov.defra.plants.applicationform.dao;

public class CommodityDAOConstants {

  private static final String SELECT_INITIAL_ID_CONSIGNMENT_ID =
      "SELECT id, consignmentId, commodityUuid, ";

  private CommodityDAOConstants() {}

  public static final String SQL_GET_UFM_COMMODITY_BASE =
      SELECT_INITIAL_ID_CONSIGNMENT_ID
          + " originCountry,"
          + " machineryType,"
          + " make,"
          + " model,"
          + " uniqueId "
          + " FROM commodityMachinery";

  public static final String SQL_GET_BOTANICAL_COMMODITY_BASE =
      SELECT_INITIAL_ID_CONSIGNMENT_ID
          + " originCountry,"
          + " genus,"
          + " species,"
          + " variety,"
          + " additionalCountries,"
          + " description,"
          + " commonName,"
          + " parentCommonName,"
          + " commodityType,"
          + " commodityClass,"
          + " quantityOrWeightPerPackage,"
          + " unitOfMeasurement,"
          + " numberOfPackages,"
          + " packagingType,"
          + " packagingMaterial,"
          + " distinguishingMarks,"
          + " eppoCode, "
          + " sampleReference "
          + " FROM commodityBotanical";

  public static final String SQL_GET_POTATO_COMMODITY_BASE =
      SELECT_INITIAL_ID_CONSIGNMENT_ID
          + " potatoType,"
          + " soilSamplingApplicationNumber,"
          + " stockNumber,"
          + " variety,"
          + " lotReference,"
          + " chemicalUsed,"
          + " quantity,"
          + " unitOfMeasurement,"
          + " numberOfPackages,"
          + " packagingType,"
          + " packagingMaterial,"
          + " distinguishingMarks"
          + " FROM commodityPotatoes";
}
