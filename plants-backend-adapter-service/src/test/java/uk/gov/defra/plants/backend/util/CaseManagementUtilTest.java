package uk.gov.defra.plants.backend.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class CaseManagementUtilTest {

  @Test
  public void shouldSayFilterISForCertificateSerialNumber(){

    List<String> strings = Arrays.asList(
        "123456",
        "111111",
        "/123456",
        "asasasasasasasasa/234567",
        "asas/asasa/sa/sa/sasa/234567",
        "19/2/234567"
        );

    strings.forEach(
        s -> assertThat( CaseManagementUtil.isACertificateSerialNumberFilter(s)).isTrue());
  }

  @Test
  public void shouldSayFilterIsNOTForCertificateSerialNumber(){

    List<String> strings = Arrays.asList(
        "12345a",
        "a11111",
        "/12345a",
        "12345",
        "/12345",
        "123456/",
        "asasasasasasasasa/a34567");

    strings.forEach(
        s -> assertThat( CaseManagementUtil.isACertificateSerialNumberFilter(s)).isFalse());
  }

  @Test
  public void shouldSayFilterIsApplicationNumber(){

    String applicationNumber = "1111111111111";
    assertThat( CaseManagementUtil.isApplicationNumberFilter(applicationNumber)).isTrue();
  }

  @Test
  public void shouldSayFilterIsNotApplicationNumber(){

    String applicationNumber = "1111";
    String applicationNumberChars = "xxxxxxxxxxxxx";
    assertThat( CaseManagementUtil.isApplicationNumberFilter(applicationNumber)).isFalse();
    assertThat( CaseManagementUtil.isApplicationNumberFilter(applicationNumberChars)).isFalse();
  }

}
