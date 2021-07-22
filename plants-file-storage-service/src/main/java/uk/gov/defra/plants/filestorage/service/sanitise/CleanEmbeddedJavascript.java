package uk.gov.defra.plants.filestorage.service.sanitise;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDJavascriptNameTreeNode;
import org.apache.pdfbox.pdmodel.common.PDDestinationOrAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.action.PDDocumentCatalogAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTerminalField;

/**
 * For reference see:
 * https://stackoverflow.com/questions/34840299/finding-javascript-code-in-pdf-using-apache-pdfbox/34899156#34899156
 * http://mariomalwareanalysis.blogspot.com/2012/02/how-to-embed-javascript-into-pdf.html
 * https://code.google.com/archive/p/corkami/wikis/PDFTricks.wiki
 * https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/
 * https://www.adobe.com/content/dam/acom/en/devnet/pdf/pdf_reference_archive/PDFReference.pdf
 */
@Slf4j
public class CleanEmbeddedJavascript extends BaseCleaner {

  private static final String SANITISED_WIDGET_ACTION_JS = "sanitisedWidgetActionJS";
  private static final String SANITISED_FIELD_JS = "sanitisedFieldJS";
  private static final String SANITISED_OPEN_ACTION_JS = "sanitisedOpenActionJS";
  private static final String SANITISED_CATALOG_ADDITIONAL_ACTIONS_JS =
      "sanitisedCatalogAdditionalActionsJS";
  private static final String SANITISED_DOCUMENT_LEVEL_JS = "sanitisedDocumentLevelJS";

  public CleanEmbeddedJavascript(PDDocument pdDocument) {
    super(pdDocument);
  }

  /**
   * Cleans the PDDocument. Note: Rendition Actions are not supported in PDF box, therefore they are
   * not saved out.
   *
   * @return the updated PDDocument
   * @throws IOException
   */
  public PDDocument clean() throws IOException {

    PDDocumentCatalog dictionary = pdDocument.getDocumentCatalog();

    cleanDocumentLevelJsActions(dictionary);

    cleanOpenAction(dictionary);

    cleanDocumentCatalogAdditionalActions(dictionary);

    cleanFormFields();

    return pdDocument;
  }

  private void cleanDocumentCatalogAdditionalActions(PDDocumentCatalog dictionary) {
    PDDocumentCatalogAdditionalActions actions = dictionary.getActions();
    if (isJsAction(actions.getWC())
        || isJsAction(actions.getWS())
        || isJsAction(actions.getDS())
        || isJsAction(actions.getWP())
        || isJsAction(actions.getDP())) {
      dictionary.setActions(null);
      recordMetadata(SANITISED_CATALOG_ADDITIONAL_ACTIONS_JS);
      LOGGER.info("*found & removed* Javascript action " + actions.toString());
    }
  }

  private void cleanOpenAction(PDDocumentCatalog dictionary) throws IOException {
    PDDestinationOrAction action = dictionary.getOpenAction();

    if (action instanceof PDActionJavaScript) {
      dictionary.setOpenAction(null);
      LOGGER.info("*found & removed* JS under openAction!");
      recordMetadata(SANITISED_OPEN_ACTION_JS);
    } else {
      LOGGER.info("No JS found under openAction");
    }
  }

  private void cleanDocumentLevelJsActions(PDDocumentCatalog dictionary) throws IOException {
    PDDocumentNameDictionary nameDictionary = dictionary.getNames();
    PDJavascriptNameTreeNode jsTreeNode = null;

    if (nameDictionary != null) {
      jsTreeNode = nameDictionary.getJavaScript();
    }

    if (jsTreeNode != null) {
      final Map<String, PDActionJavaScript> jsMap = jsTreeNode.getNames();

      if (jsMap != null) {
        List<String> keysToRemove = new ArrayList<>();
        jsMap
            .entrySet()
            .forEach(
                entry -> {
                  LOGGER.info(
                      "*found* document level JS: "
                          + entry.getKey()
                          + " "
                          + entry.getValue().toString());
                  keysToRemove.add(entry.getKey());
                  recordMetadata(SANITISED_DOCUMENT_LEVEL_JS);
                });
        keysToRemove.forEach(key -> jsMap.remove(key));
      } else {
        LOGGER.info("No JS found under document level nameDictionary");
      }
    }
  }

  private void cleanFormFields() {
    PDDocumentCatalog docCatalog = pdDocument.getDocumentCatalog();
    PDAcroForm acroForm = docCatalog.getAcroForm();

    if (acroForm == null) {
      return;
    }

    acroForm.getFields().forEach(field -> processField(field));
  }

  private void processField(PDField field) {

    if (field instanceof PDTerminalField) {
      processTerminalField(field);
    }

    if (field instanceof PDNonTerminalField) {
      ((PDNonTerminalField) field).getChildren().forEach(child -> processField(child));
    }
  }

  private void processTerminalField(PDField field) {
    PDTerminalField termField = (PDTerminalField) field;
    PDFormFieldAdditionalActions fieldActions = field.getActions();

    if (fieldActions != null) {
      cleanField(termField, fieldActions);
    }

    termField.getWidgets().forEach(widgetAction -> cleanAnnotationWidget(field, widgetAction));
  }

  private void cleanAnnotationWidget(PDField field, PDAnnotationWidget widgetAction) {
    PDAction action = widgetAction.getAction();
    if (action instanceof PDActionJavaScript) {
      widgetAction.setActions(null);
      LOGGER.info(
          "*found* widget Javascript "
              + field.getFullyQualifiedName()
              + ": "
              + action.getClass().getSimpleName()
              + " js widget action:\n"
              + action.getCOSObject());
      recordMetadata(SANITISED_WIDGET_ACTION_JS);
    }
  }

  private void cleanField(PDTerminalField termField, PDFormFieldAdditionalActions fieldActions) {
    // assume this contains Javascript.
    termField.setActions(null);
    LOGGER.info("*found & removed* probable Javascript action on field " + fieldActions.toString());
    recordMetadata(SANITISED_FIELD_JS);
  }

  private boolean isJsAction(PDAction kAction) {
    return (kAction instanceof PDActionJavaScript);
  }
}
