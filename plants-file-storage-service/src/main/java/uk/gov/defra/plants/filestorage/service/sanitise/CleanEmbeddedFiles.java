package uk.gov.defra.plants.filestorage.service.sanitise;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationFileAttachment;

/**
 * For reference see:
 * https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/pdmodel/
 */
@Slf4j
public class CleanEmbeddedFiles extends BaseCleaner {

  public CleanEmbeddedFiles(PDDocument pdDocument) {
    super(pdDocument);
  }

  public PDDocument clean() {

    PDDocumentNameDictionary namesDictionary =
        new PDDocumentNameDictionary(pdDocument.getDocumentCatalog());
    PDEmbeddedFilesNameTreeNode efTree = namesDictionary.getEmbeddedFiles();

    if (efTree != null) {
      LOGGER.info("*found* embedded file(s)");
      namesDictionary.setEmbeddedFiles(null); // removes all embeds
      recordMetadata("sanitisedEmbeddedFiles");
    }

    pdDocument.getPages().forEach(page -> sanitisePage(page));

    return pdDocument;
  }

  private void sanitisePage(PDPage page) {
    List<PDAnnotation> sanitisedAnnotations = new ArrayList<>();

    try {
      for (PDAnnotation annotation : page.getAnnotations()) {
        if (annotation instanceof PDAnnotationFileAttachment) {
          LOGGER.info("*found* embedded file, will not add to sanitised list");
          recordMetadata("sanitisedAnnotationFileAttachment");

        } else {
          sanitisedAnnotations.add(annotation);
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error reading annotations, will continue processing",e);
    }
    page.setAnnotations(sanitisedAnnotations);
  }
}
