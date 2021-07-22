package uk.gov.defra.plants.filestorage.service.sanitise;

import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;

@AllArgsConstructor
abstract class BaseCleaner {

  final PDDocument pdDocument;

  void recordMetadata(String metadataName){
    pdDocument.getDocumentInformation().setCustomMetadataValue(metadataName,"");
  }
}
