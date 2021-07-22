package uk.gov.defra.plants.filestorage.utils;

import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionSound;
import org.apache.pdfbox.pdmodel.interactive.action.PDAnnotationAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationFileAttachment;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

@Slf4j
public class PdfFactory {

  private static final String DUMMY_ATTACHMENT_FILE_NAME = "Test.txt";
  private static final String DUMMY_TEXT = "This is the contents of the embedded file";
  private static final String EXAMPLE_JS = "app.alert('JavaScript has just been executed!');";

  PDFont DEFAULT_FONT = PDType1Font.HELVETICA_BOLD;

  

  public PDDocument buildPdfWithEmbeddedFile() throws IOException {

    try (PDDocument document = new PDDocument()) {

      PDPage page = new PDPage();
      document.addPage(page);

      addFormToPage(document, page);

      addTextToPage("This document has an attachment.", document, page);
      PDEmbeddedFilesNameTreeNode efTree = buildEmbeddedTreeNode(document);

      PDDocumentNameDictionary names = new PDDocumentNameDictionary(document.getDocumentCatalog());
      names.setEmbeddedFiles(efTree);
      document.getDocumentCatalog().setNames(names);

      return document;
    }
  }

  public PDDocument buildPdfWithEmbeddedFileAnnotation() throws IOException {

    try (PDDocument document = new PDDocument()) {

      PDPage page = new PDPage();
      document.addPage(page);

      addTextToPage("This document has an attachment.", document, page);

      page.setAnnotations(
          ImmutableList.of(new PDAnnotationFileAttachment(), new PDAnnotationText()));

      return document;
    }
  }

  public PDDocument buildPdfWithEmbeddedJavascript() throws IOException {
    try (PDDocument document = new PDDocument()) {
      PDPage page = new PDPage();
      document.addPage(page);

      addTextToPage("This document has embedded JavaScript!", document, page);
      addJavascriptToDocument(document, EXAMPLE_JS);

      addFormToPage(document, page);

      return document;
    }
  }

  private void addFormToPage(PDDocument document, PDPage page) throws IOException {
    PDFont font = PDType1Font.HELVETICA;
    PDResources resources = new PDResources();
    resources.put(COSName.getPDFName("Helv"), font);

    // Add a new AcroForm and add that to the document
    PDAcroForm acroForm = new PDAcroForm(document);
    document.getDocumentCatalog().setAcroForm(acroForm);

    // Add and set the resources and default appearance at the form level
    acroForm.setDefaultResources(resources);

    // Acrobat sets the font size on the form level to be
    // auto sized as default. This is done by setting the font size to '0'
    String defaultAppearanceString = "/Helv 0 Tf 0 g";
    acroForm.setDefaultAppearance(defaultAppearanceString);

    // Add a form field to the form.
    PDTextField textBox = new PDTextField(acroForm);
    textBox.setPartialName("SampleField");

    // setting font size to 12
    defaultAppearanceString = "/Helv 12 Tf 0 g";
    textBox.setDefaultAppearance(defaultAppearanceString);

    // add the field to the acroform
    acroForm.getFields().add(textBox);

    // Specify the annotation associated with the field
    PDAnnotationWidget widget = textBox.getWidgets().get(0);
    PDRectangle rect = new PDRectangle(50, 750, 200, 50);
    widget.setRectangle(rect);
    widget.setPage(page);

    PDAnnotationAdditionalActions annotationActions = new PDAnnotationAdditionalActions();

    PDActionJavaScript jsEnterAction = new PDActionJavaScript();
    jsEnterAction.setAction("app.alert(\"You have 'entered' the text box!\")");
    annotationActions.setE(jsEnterAction);
    widget.setActions(annotationActions);

    // make sure the annotation is visible on screen and paper
    widget.setPrinted(true);

    // Add the annotation to the page
    page.getAnnotations().add(widget);

    // set the field value
    textBox.setValue("Sample field");
  }

  public PDDocument buildBasicPdDocument() throws IOException {
    try (PDDocument document = new PDDocument()) {
      PDPage page = new PDPage();
      document.addPage(page);
      addTextToPage(
          "This document has NO JavaScript, but it does have an open action.", document, page);

      document.getDocumentCatalog().setOpenAction(new PDActionSound());

      return document;
    }
  }

  private void addJavascriptToDocument(PDDocument document, String scriptText) {
    PDActionJavaScript javascript = new PDActionJavaScript(scriptText);
    document.getDocumentCatalog().setOpenAction(javascript);
  }

  private PDEmbeddedFilesNameTreeNode buildEmbeddedTreeNode(PDDocument doc) throws IOException {

    PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();

    PDComplexFileSpecification fileSpecification = new PDComplexFileSpecification();
    fileSpecification.setFile(DUMMY_ATTACHMENT_FILE_NAME);
    PDEmbeddedFile embeddedFile = buildPdEmbeddedFile(doc);
    fileSpecification.setEmbeddedFile(embeddedFile);

    PDEmbeddedFilesNameTreeNode treeNode = new PDEmbeddedFilesNameTreeNode();
    treeNode.setNames(Collections.singletonMap("An attachment", fileSpecification));

    List<PDEmbeddedFilesNameTreeNode> kids = new ArrayList<>();
    kids.add(treeNode);
    efTree.setKids(kids);
    return efTree;
  }

  private PDEmbeddedFile buildPdEmbeddedFile(PDDocument doc) throws IOException {
    byte[] data = DUMMY_TEXT.getBytes(StandardCharsets.ISO_8859_1);
    ByteArrayInputStream fakeFile = new ByteArrayInputStream(data);
    PDEmbeddedFile embeddedFile = new PDEmbeddedFile(doc, fakeFile);
    embeddedFile.setSubtype("text/plain");
    embeddedFile.setSize(data.length);
    embeddedFile.setCreationDate(new GregorianCalendar());
    return embeddedFile;
  }

  private void addTextToPage(String text, PDDocument doc, PDPage page) throws IOException {

    try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
      contentStream.beginText();
      contentStream.setFont(DEFAULT_FONT, 12);
      contentStream.newLineAtOffset(100, 700);
      contentStream.showText(text);
      contentStream.endText();
    }
  }
}
