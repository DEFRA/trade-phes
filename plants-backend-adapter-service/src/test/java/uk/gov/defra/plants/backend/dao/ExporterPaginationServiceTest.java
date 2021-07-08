package uk.gov.defra.plants.backend.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import uk.gov.defra.plants.reference.adapter.ReferenceDataServiceAdapter;
import uk.gov.defra.plants.reference.representation.Country;

@RunWith(MockitoJUnitRunner.class)
public class ExporterPaginationServiceTest {

  @Mock
  private ReferenceDataServiceAdapter referenceDataServiceAdapter;

  @InjectMocks
  private ExporterPaginationService exporterPaginationService;

  private static final UUID organisationId = UUID
      .fromString("998976ac-4e50-4a5b-bed3-5ad37fd882ae");

  private static final UUID applicantId = UUID.fromString("cd235a5b-de4b-40bf-98e7-8b4bd330ffe4");

  @Test
  public void testGetFetchXMLForPaginatedApplications_noFilter_noAppFormId_noStatusCodes()
      throws Exception {
    String expected =
        "<fetch count=\"1\" distinct=\"true\" mapping=\"logical\" output-format=\"xml-platform\" page=\"1\" version=\"1.0\">\n"
            + "   <entity name=\"defraexp_exportapplication\">\n"
            + "      <attribute name=\"defraexp_caseid\" />\n"
            + "      <attribute name=\"defraexp_exportdate\" />\n"
            + "      <attribute name=\"statuscode\" />\n"
            + "      <attribute name=\"defraexp_submissionid\" />\n"
            + "      <attribute name=\"defraexp_exportapplicationid\" />\n"
            + "      <attribute name=\"defraexp_applicantreference\" />\n"
            + "      <attribute name=\"createdon\" />\n"
            + "      <link-entity from=\"contactid\" link-type=\"inner\" name=\"contact\" to=\"defraexp_applicantid\">\n"
            + "         <attribute alias=\"defraexp_fullname\" name=\"fullname\" />\n"
            + "         <attribute alias=\"defraexp_contactid\" name=\"contactid\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_countryid\" link-type=\"inner\" name=\"defraexp_country\" to=\"defraexp_destinationcountry\">\n"
            + "         <attribute alias=\"defraexp_destcountry\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_ehctemplateid\" link-type=\"inner\" name=\"defraexp_ehctemplate\" to=\"defraexp_ehctemplate\">\n"
            + "         <attribute alias=\"defraexp_ehc_template\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <filter type=\"and\">\n"
            + "         <condition attribute=\"defraexp_applicantid\" operator=\"eq\" value=\"{cd235a5b-de4b-40bf-98e7-8b4bd330ffe4}\" />\n"
            + "         <condition attribute=\"defraexp_organisation\" operator=\"eq\" value=\"{998976ac-4e50-4a5b-bed3-5ad37fd882ae}\" />\n"
            + "      </filter>\n"
            + "      <order attribute=\"createdon\" descending=\"true\" />\n"
            + "   </entity>\n"
            + "</fetch>";
    Document expectedDocument = generateDocumentFromString(expected);
    String fetchXml = exporterPaginationService.getFetchXMLForPaginatedApplications(
        1, 1, StringUtils.EMPTY, applicantId, organisationId, Collections.emptyList());
    Document actualDocument = generateDocumentFromString(fetchXml);
    assertThat(toString(actualDocument)).isEqualTo(toString(expectedDocument));
  }

  @Test
  public void testGetFetchXMLForPaginatedApplications_noFilter_noAppFormId_noStatusCodes_noOrg()
      throws Exception {
    String expected =
        "<fetch count=\"1\" distinct=\"true\" mapping=\"logical\" output-format=\"xml-platform\" page=\"1\" version=\"1.0\">\n"
            + "   <entity name=\"defraexp_exportapplication\">\n"
            + "      <attribute name=\"defraexp_caseid\" />\n"
            + "      <attribute name=\"defraexp_exportdate\" />\n"
            + "      <attribute name=\"statuscode\" />\n"
            + "      <attribute name=\"defraexp_submissionid\" />\n"
            + "      <attribute name=\"defraexp_exportapplicationid\" />\n"
            + "      <attribute name=\"defraexp_applicantreference\" />\n"
            + "      <attribute name=\"createdon\" />\n"
            + "      <link-entity from=\"contactid\" link-type=\"inner\" name=\"contact\" to=\"defraexp_applicantid\">\n"
            + "         <attribute alias=\"defraexp_fullname\" name=\"fullname\" />\n"
            + "         <attribute alias=\"defraexp_contactid\" name=\"contactid\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_countryid\" link-type=\"inner\" name=\"defraexp_country\" to=\"defraexp_destinationcountry\">\n"
            + "         <attribute alias=\"defraexp_destcountry\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_ehctemplateid\" link-type=\"inner\" name=\"defraexp_ehctemplate\" to=\"defraexp_ehctemplate\">\n"
            + "         <attribute alias=\"defraexp_ehc_template\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <filter type=\"and\">\n"
            + "         <condition attribute=\"defraexp_applicantid\" operator=\"eq\" value=\"{cd235a5b-de4b-40bf-98e7-8b4bd330ffe4}\" />\n"
            + "      </filter>\n"
            + "      <order attribute=\"createdon\" descending=\"true\" />\n"
            + "   </entity>\n"
            + "</fetch>";
    Document expectedDocument = generateDocumentFromString(expected);
    String fetchXml = exporterPaginationService.getFetchXMLForPaginatedApplications(
        1, 1, StringUtils.EMPTY, applicantId, null, Collections.emptyList());
    Document actualDocument = generateDocumentFromString(fetchXml);
    assertThat(toString(actualDocument)).isEqualTo(toString(expectedDocument));
  }

  @Test
  public void testGetFetchXMLForPaginatedApplications_By_filter_with_status_country()
      throws Exception {

    UUID countryId1 = UUID.fromString("7edad6cc-ee0b-4849-8968-01f3b0cc9583");
    UUID countryId2 = UUID.fromString("df2d488f-c53c-47e4-bdab-046635fc78f1");

    when(referenceDataServiceAdapter.getCountriesByPartialName(anyString()))
        .thenReturn(Arrays.asList(
            Country.builder().id(countryId1).build(), Country.builder().id(countryId2).build()));

    String expected =
        "<fetch count=\"1\" distinct=\"true\" mapping=\"logical\" output-format=\"xml-platform\" page=\"1\" version=\"1.0\">\n"
            + "   <entity name=\"defraexp_exportapplication\">\n"
            + "      <attribute name=\"defraexp_caseid\" />\n"
            + "      <attribute name=\"defraexp_exportdate\" />\n"
            + "      <attribute name=\"statuscode\" />\n"
            + "      <attribute name=\"defraexp_submissionid\" />\n"
            + "      <attribute name=\"defraexp_exportapplicationid\" />\n"
            + "      <attribute name=\"defraexp_applicantreference\" />\n"
            + "      <attribute name=\"createdon\" />\n"
            + "      <link-entity from=\"contactid\" link-type=\"inner\" name=\"contact\" to=\"defraexp_applicantid\">\n"
            + "         <attribute alias=\"defraexp_fullname\" name=\"fullname\" />\n"
            + "         <attribute alias=\"defraexp_contactid\" name=\"contactid\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_countryid\" link-type=\"inner\" name=\"defraexp_country\" to=\"defraexp_destinationcountry\">\n"
            + "         <attribute alias=\"defraexp_destcountry\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_ehctemplateid\" link-type=\"inner\" name=\"defraexp_ehctemplate\" to=\"defraexp_ehctemplate\">\n"
            + "         <attribute alias=\"defraexp_ehc_template\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <filter type=\"and\">\n"
            + "         <condition attribute=\"defraexp_applicantid\" operator=\"eq\" value=\"{cd235a5b-de4b-40bf-98e7-8b4bd330ffe4}\" />\n"
            + "         <condition attribute=\"defraexp_organisation\" operator=\"eq\" value=\"{998976ac-4e50-4a5b-bed3-5ad37fd882ae}\" />\n"
            + "         <condition attribute=\"statuscode\" operator=\"in\">\n"
            + "            <value>1</value>\n"
            + "         </condition>\n"
            + "         <filter type=\"or\">\n"
            + "            <condition attribute=\"defraexp_applicantreference\" operator=\"like\" value=\"%France%\" />\n"
            + "            <condition attribute=\"defraexp_destinationcountry\" operator=\"in\">\n"
            + "               <value>{7edad6cc-ee0b-4849-8968-01f3b0cc9583}</value>\n"
            + "               <value>{df2d488f-c53c-47e4-bdab-046635fc78f1}</value>\n"
            + "            </condition>\n"
            + "         </filter>\n"
            + "      </filter>\n"
            + "      <order attribute=\"createdon\" descending=\"true\" />\n"
            + "   </entity>\n"
            + "</fetch>";
    Document expectedDocument = generateDocumentFromString(expected);
    String fetchXml = exporterPaginationService.getFetchXMLForPaginatedApplications(
        1, 1, "France", applicantId, organisationId, Collections.singletonList(1));
    Document actualDocument = generateDocumentFromString(fetchXml);
    assertThat(toString(actualDocument)).isEqualTo(toString(expectedDocument));
  }

  @Test
  public void testGetFetchXMLForPaginatedApplications_By_filter_check_page_and_count()
      throws Exception {
    String expected =
        "<fetch count=\"5\" distinct=\"true\" mapping=\"logical\" output-format=\"xml-platform\" page=\"2\" version=\"1.0\">\n"
            + "   <entity name=\"defraexp_exportapplication\">\n"
            + "      <attribute name=\"defraexp_caseid\" />\n"
            + "      <attribute name=\"defraexp_exportdate\" />\n"
            + "      <attribute name=\"statuscode\" />\n"
            + "      <attribute name=\"defraexp_submissionid\" />\n"
            + "      <attribute name=\"defraexp_exportapplicationid\" />\n"
            + "      <attribute name=\"defraexp_applicantreference\" />\n"
            + "      <attribute name=\"createdon\" />\n"
            + "      <link-entity from=\"contactid\" link-type=\"inner\" name=\"contact\" to=\"defraexp_applicantid\">\n"
            + "         <attribute alias=\"defraexp_fullname\" name=\"fullname\" />\n"
            + "         <attribute alias=\"defraexp_contactid\" name=\"contactid\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_countryid\" link-type=\"inner\" name=\"defraexp_country\" to=\"defraexp_destinationcountry\">\n"
            + "         <attribute alias=\"defraexp_destcountry\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_ehctemplateid\" link-type=\"inner\" name=\"defraexp_ehctemplate\" to=\"defraexp_ehctemplate\">\n"
            + "         <attribute alias=\"defraexp_ehc_template\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <filter type=\"and\">\n"
            + "         <condition attribute=\"defraexp_applicantid\" operator=\"eq\" value=\"{cd235a5b-de4b-40bf-98e7-8b4bd330ffe4}\" />\n"
            + "         <condition attribute=\"defraexp_organisation\" operator=\"eq\" value=\"{998976ac-4e50-4a5b-bed3-5ad37fd882ae}\" />\n"
            + "         <filter type=\"or\">\n"
            + "            <condition attribute=\"defraexp_applicantreference\" operator=\"like\" value=\"%France%\" />\n"
            + "         </filter>\n"
            + "      </filter>\n"
            + "      <order attribute=\"createdon\" descending=\"true\" />\n"
            + "   </entity>\n"
            + "</fetch>";
    Document expectedDocument = generateDocumentFromString(expected);

    String fetchXml = exporterPaginationService.getFetchXMLForPaginatedApplications(
        2, 5, "France", applicantId, organisationId, Collections.emptyList());
    Document actualDocument = generateDocumentFromString(fetchXml);

    assertThat(toString(actualDocument)).isEqualTo(toString(expectedDocument));
  }

  @Test
  public void testGetFetchXMLForPaginatedApplications_noFilter_noStatusCode()
      throws Exception {
    String expected =
        "<fetch count=\"5\" distinct=\"true\" mapping=\"logical\" output-format=\"xml-platform\" page=\"2\" version=\"1.0\">\n"
            + "   <entity name=\"defraexp_exportapplication\">\n"
            + "      <attribute name=\"defraexp_caseid\" />\n"
            + "      <attribute name=\"defraexp_exportdate\" />\n"
            + "      <attribute name=\"statuscode\" />\n"
            + "      <attribute name=\"defraexp_submissionid\" />\n"
            + "      <attribute name=\"defraexp_exportapplicationid\" />\n"
            + "      <attribute name=\"defraexp_applicantreference\" />\n"
            + "      <attribute name=\"createdon\" />\n"
            + "      <link-entity from=\"contactid\" link-type=\"inner\" name=\"contact\" to=\"defraexp_applicantid\">\n"
            + "         <attribute alias=\"defraexp_fullname\" name=\"fullname\" />\n"
            + "         <attribute alias=\"defraexp_contactid\" name=\"contactid\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_countryid\" link-type=\"inner\" name=\"defraexp_country\" to=\"defraexp_destinationcountry\">\n"
            + "         <attribute alias=\"defraexp_destcountry\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_ehctemplateid\" link-type=\"inner\" name=\"defraexp_ehctemplate\" to=\"defraexp_ehctemplate\">\n"
            + "         <attribute alias=\"defraexp_ehc_template\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <filter type=\"and\">\n"
            + "         <condition attribute=\"defraexp_applicantid\" operator=\"eq\" value=\"{cd235a5b-de4b-40bf-98e7-8b4bd330ffe4}\" />\n"
            + "         <condition attribute=\"defraexp_organisation\" operator=\"eq\" value=\"{998976ac-4e50-4a5b-bed3-5ad37fd882ae}\" />\n"
            + "      </filter>\n"
            + "      <order attribute=\"createdon\" descending=\"true\" />\n"
            + "   </entity>\n"
            + "</fetch>";
    Document expectedDocument = generateDocumentFromString(expected);

    String fetchXml = exporterPaginationService.getFetchXMLForPaginatedApplications(
        2, 5, StringUtils.EMPTY, applicantId, organisationId, Collections.emptyList());
    Document actualDocument = generateDocumentFromString(fetchXml);

    assertThat(toString(actualDocument)).isEqualTo(toString(expectedDocument));
  }

  @Test
  public void testGetFetchXMLForPaginatedApplications_By_filter_and_no_AppFormId()
      throws Exception {
    String expected =
        "<fetch count=\"5\" distinct=\"true\" mapping=\"logical\" output-format=\"xml-platform\" page=\"2\" version=\"1.0\">\n"
            + "   <entity name=\"defraexp_exportapplication\">\n"
            + "      <attribute name=\"defraexp_caseid\" />\n"
            + "      <attribute name=\"defraexp_exportdate\" />\n"
            + "      <attribute name=\"statuscode\" />\n"
            + "      <attribute name=\"defraexp_submissionid\" />\n"
            + "      <attribute name=\"defraexp_exportapplicationid\" />\n"
            + "      <attribute name=\"defraexp_applicantreference\" />\n"
            + "      <attribute name=\"createdon\" />\n"
            + "      <link-entity from=\"contactid\" link-type=\"inner\" name=\"contact\" to=\"defraexp_applicantid\">\n"
            + "         <attribute alias=\"defraexp_fullname\" name=\"fullname\" />\n"
            + "         <attribute alias=\"defraexp_contactid\" name=\"contactid\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_countryid\" link-type=\"inner\" name=\"defraexp_country\" to=\"defraexp_destinationcountry\">\n"
            + "         <attribute alias=\"defraexp_destcountry\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_ehctemplateid\" link-type=\"inner\" name=\"defraexp_ehctemplate\" to=\"defraexp_ehctemplate\">\n"
            + "         <attribute alias=\"defraexp_ehc_template\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <filter type=\"and\">\n"
            + "         <condition attribute=\"defraexp_applicantid\" operator=\"eq\" value=\"{cd235a5b-de4b-40bf-98e7-8b4bd330ffe4}\" />\n"
            + "         <condition attribute=\"defraexp_organisation\" operator=\"eq\" value=\"{998976ac-4e50-4a5b-bed3-5ad37fd882ae}\" />\n"
            + "         <filter type=\"or\">\n"
            + "            <condition attribute=\"defraexp_applicantreference\" operator=\"like\" value=\"%France%\" />\n"
            + "         </filter>\n"
            + "      </filter>\n"
            + "      <order attribute=\"createdon\" descending=\"true\" />\n"
            + "   </entity>\n"
            + "</fetch>";
    Document expectedDocument = generateDocumentFromString(expected);

    String fetchXml = exporterPaginationService.getFetchXMLForPaginatedApplications(
        2, 5, "France", applicantId, organisationId, Collections.emptyList());
    Document actualDocument = generateDocumentFromString(fetchXml);

    assertThat(toString(actualDocument)).isEqualTo(toString(expectedDocument));
  }

  @Test
  public void testGetFetchXMLForPaginatedApplications_By_filter_and_noAppFormIds_with_status()
      throws Exception {
    String expected =
        "<fetch count=\"5\" distinct=\"true\" mapping=\"logical\" output-format=\"xml-platform\" page=\"2\" version=\"1.0\">\n"
            + "   <entity name=\"defraexp_exportapplication\">\n"
            + "      <attribute name=\"defraexp_caseid\" />\n"
            + "      <attribute name=\"defraexp_exportdate\" />\n"
            + "      <attribute name=\"statuscode\" />\n"
            + "      <attribute name=\"defraexp_submissionid\" />\n"
            + "      <attribute name=\"defraexp_exportapplicationid\" />\n"
            + "      <attribute name=\"defraexp_applicantreference\" />\n"
            + "      <attribute name=\"createdon\" />\n"
            + "      <link-entity from=\"contactid\" link-type=\"inner\" name=\"contact\" to=\"defraexp_applicantid\">\n"
            + "         <attribute alias=\"defraexp_fullname\" name=\"fullname\" />\n"
            + "         <attribute alias=\"defraexp_contactid\" name=\"contactid\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_countryid\" link-type=\"inner\" name=\"defraexp_country\" to=\"defraexp_destinationcountry\">\n"
            + "         <attribute alias=\"defraexp_destcountry\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_ehctemplateid\" link-type=\"inner\" name=\"defraexp_ehctemplate\" to=\"defraexp_ehctemplate\">\n"
            + "         <attribute alias=\"defraexp_ehc_template\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <filter type=\"and\">\n"
            + "         <condition attribute=\"defraexp_applicantid\" operator=\"eq\" value=\"{cd235a5b-de4b-40bf-98e7-8b4bd330ffe4}\" />\n"
            + "         <condition attribute=\"defraexp_organisation\" operator=\"eq\" value=\"{998976ac-4e50-4a5b-bed3-5ad37fd882ae}\" />\n"
            + "         <condition attribute=\"statuscode\" operator=\"in\">\n"
            + "            <value>1</value>\n"
            + "         </condition>\n"
            + "         <filter type=\"or\">\n"
            + "            <condition attribute=\"defraexp_applicantreference\" operator=\"like\" value=\"%France%\" />\n"
            + "         </filter>\n"
            + "      </filter>\n"
            + "      <order attribute=\"createdon\" descending=\"true\" />\n"
            + "   </entity>\n"
            + "</fetch>";
    Document expectedDocument = generateDocumentFromString(expected);

    String fetchXml = exporterPaginationService.getFetchXMLForPaginatedApplications(
        2, 5, "France", applicantId, organisationId, Collections.singletonList(1));
    Document actualDocument = generateDocumentFromString(fetchXml);

    assertThat(toString(actualDocument)).isEqualTo(toString(expectedDocument));
  }

  @Test
  public void testGetFetchXMLForPaginatedApplications_noFilter_noAppFormIds_with_status()
      throws Exception {
    String expected =
        "<fetch count=\"5\" distinct=\"true\" mapping=\"logical\" output-format=\"xml-platform\" page=\"2\" version=\"1.0\">\n"
            + "   <entity name=\"defraexp_exportapplication\">\n"
            + "      <attribute name=\"defraexp_caseid\" />\n"
            + "      <attribute name=\"defraexp_exportdate\" />\n"
            + "      <attribute name=\"statuscode\" />\n"
            + "      <attribute name=\"defraexp_submissionid\" />\n"
            + "      <attribute name=\"defraexp_exportapplicationid\" />\n"
            + "      <attribute name=\"defraexp_applicantreference\" />\n"
            + "      <attribute name=\"createdon\" />\n"
            + "      <link-entity from=\"contactid\" link-type=\"inner\" name=\"contact\" to=\"defraexp_applicantid\">\n"
            + "         <attribute alias=\"defraexp_fullname\" name=\"fullname\" />\n"
            + "         <attribute alias=\"defraexp_contactid\" name=\"contactid\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_countryid\" link-type=\"inner\" name=\"defraexp_country\" to=\"defraexp_destinationcountry\">\n"
            + "         <attribute alias=\"defraexp_destcountry\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_ehctemplateid\" link-type=\"inner\" name=\"defraexp_ehctemplate\" to=\"defraexp_ehctemplate\">\n"
            + "         <attribute alias=\"defraexp_ehc_template\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <filter type=\"and\">\n"
            + "         <condition attribute=\"defraexp_applicantid\" operator=\"eq\" value=\"{cd235a5b-de4b-40bf-98e7-8b4bd330ffe4}\" />\n"
            + "         <condition attribute=\"defraexp_organisation\" operator=\"eq\" value=\"{998976ac-4e50-4a5b-bed3-5ad37fd882ae}\" />\n"
            + "         <condition attribute=\"statuscode\" operator=\"in\">\n"
            + "            <value>1</value>\n"
            + "         </condition>\n"
            + "      </filter>\n"
            + "      <order attribute=\"createdon\" descending=\"true\" />\n"
            + "   </entity>\n"
            + "</fetch>";
    Document expectedDocument = generateDocumentFromString(expected);

    String fetchXml = exporterPaginationService.getFetchXMLForPaginatedApplications(
        2, 5, StringUtils.EMPTY, applicantId, organisationId, Collections.singletonList(1));
    Document actualDocument = generateDocumentFromString(fetchXml);

    assertThat(toString(actualDocument)).isEqualTo(toString(expectedDocument));
  }


  private Document generateDocumentFromString(String fetchXmlString) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(new InputSource(new StringReader(fetchXmlString)));
    return document;
  }

  @Test
  public void testGetFetchXMLForPaginatedApplications_By_SubmittedDateFilter_with_status()
      throws Exception {
    String expected =
        "<fetch count=\"5\" distinct=\"true\" mapping=\"logical\" output-format=\"xml-platform\" page=\"2\" version=\"1.0\">\n"
            + "   <entity name=\"defraexp_exportapplication\">\n"
            + "      <attribute name=\"defraexp_caseid\" />\n"
            + "      <attribute name=\"defraexp_exportdate\" />\n"
            + "      <attribute name=\"statuscode\" />\n"
            + "      <attribute name=\"defraexp_submissionid\" />\n"
            + "      <attribute name=\"defraexp_exportapplicationid\" />\n"
            + "      <attribute name=\"defraexp_applicantreference\" />\n"
            + "      <attribute name=\"createdon\" />\n"
            + "      <link-entity from=\"contactid\" link-type=\"inner\" name=\"contact\" to=\"defraexp_applicantid\">\n"
            + "         <attribute alias=\"defraexp_fullname\" name=\"fullname\" />\n"
            + "         <attribute alias=\"defraexp_contactid\" name=\"contactid\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_countryid\" link-type=\"inner\" name=\"defraexp_country\" to=\"defraexp_destinationcountry\">\n"
            + "         <attribute alias=\"defraexp_destcountry\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <link-entity from=\"defraexp_ehctemplateid\" link-type=\"inner\" name=\"defraexp_ehctemplate\" to=\"defraexp_ehctemplate\">\n"
            + "         <attribute alias=\"defraexp_ehc_template\" name=\"defraexp_name\" />\n"
            + "      </link-entity>\n"
            + "      <filter type=\"and\">\n"
            + "         <condition attribute=\"defraexp_applicantid\" operator=\"eq\" value=\"{cd235a5b-de4b-40bf-98e7-8b4bd330ffe4}\" />\n"
            + "         <condition attribute=\"defraexp_organisation\" operator=\"eq\" value=\"{998976ac-4e50-4a5b-bed3-5ad37fd882ae}\" />\n"
            + "         <condition attribute=\"statuscode\" operator=\"in\">\n"
            + "            <value>1</value>\n"
            + "         </condition>\n"
            + "         <filter type=\"or\">\n"
            + "         <filter type=\"and\">\n"
            + "            <condition attribute=\"createdon\" operator=\"on-or-after\" value=\"2020-05-01T00:00:00.000\" />\n"
            + "            <condition attribute=\"createdon\" operator=\"on-or-before\" value=\"2020-05-01T23:59:59.999\" />\n"
            + "         </filter>\n"
            + "            <condition attribute=\"defraexp_applicantreference\" operator=\"like\" value=\"%01 May 2020%\" />\n"
            + "         </filter>\n"
            + "      </filter>\n"
            + "      <order attribute=\"createdon\" descending=\"true\" />\n"
            + "   </entity>\n"
            + "</fetch>";
    Document expectedDocument = generateDocumentFromString(expected);

    String fetchXml = exporterPaginationService.getFetchXMLForPaginatedApplications(
        2, 5, "01 May 2020", applicantId, organisationId, Collections.singletonList(1));
    Document actualDocument = generateDocumentFromString(fetchXml);

    assertThat(toString(actualDocument)).isEqualTo(toString(expectedDocument));
  }


  private static String toString(Document doc) {
    try {
      StringWriter sw = new StringWriter();
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "no");
      transformer.transform(new DOMSource(doc), new StreamResult(sw));
      return sw.toString().replaceAll(">\\s+<", "><");
    } catch (Exception ex) {
      throw new RuntimeException("Error converting to String", ex);
    }
  }
}
