package uk.gov.defra.plants.formconfiguration.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.EnumUtils;
import uk.gov.defra.plants.common.validation.AbstractValidator;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.CommodityGroup;
import uk.gov.defra.plants.formconfiguration.representation.healthcertificate.HealthCertificate;

public class CommodityGroupValidator extends AbstractValidator<HealthCertificate>
    implements ConstraintValidator<CommodityGroupValid, HealthCertificate> {

  @Override
  public void initialize(CommodityGroupValid constraint) {
    // nothing to initialize, adding this comment to make SonarQube happy
  }

  @Override
  public boolean isValid(HealthCertificate healthCertificate, ConstraintValidatorContext context) {

    if (null == healthCertificate.getCommodityGroup()
        || !EnumUtils.isValidEnum(CommodityGroup.class, healthCertificate.getCommodityGroup())) {
      addViolation(context, "commodityGroup", "Enter a valid commodity group");
      return false;
    }

    return true;
  }
}
