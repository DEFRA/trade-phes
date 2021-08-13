package uk.gov.defra.plants.applicationform.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import uk.gov.defra.plants.common.representation.CertificateApplicationError;

import java.util.List;
import java.util.Optional;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class MultiplesApplicationValidationErrors {

    Optional<List<String>> commonErrors;
    Optional<List<CertificateApplicationError>> certificateApplicationErrors;

    public boolean hasErrors(){
        return commonErrors.isPresent() || certificateApplicationErrors.isPresent();
    }

}
