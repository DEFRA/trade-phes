package uk.gov.defra.plants.formconfiguration.validation;

import static org.apache.commons.lang3.StringUtils.isBlank;

import javax.inject.Inject;
import uk.gov.defra.plants.formconfiguration.service.ExaDocumentService;
import uk.gov.defra.plants.formconfiguration.service.HealthCertificateService;
import uk.gov.defra.plants.common.validation.AbstractValidator;


public abstract class EhcExaNumberUniquenessValidator<T> extends AbstractValidator<T>{

  @Inject private ExaDocumentService exaDocumentService;

  @Inject private HealthCertificateService healthCertificateService;

  public boolean isUnique(String ehcOrExaNumber) {
    if(isBlank(ehcOrExaNumber)){
      return true;
    }
    return healthCertificateService.getByEhcNumber(ehcOrExaNumber).isEmpty()
        && exaDocumentService.get(ehcOrExaNumber).isEmpty();
  }
}
