package uk.gov.defra.plants.backend.util;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CaseManagementUtil {

  private static final Pattern _6_DIGITS_PATTERN = Pattern.compile("\\d{6}");
  private static final Pattern _13_DIGITS_PATTERN = Pattern.compile("\\d{13}");

  public static boolean isACertificateSerialNumberFilter( String filter ){

    return filter !=null && (isExactly6Digits(filter) || endsWithSlashAnd6Digits(filter));
  }

  public static boolean isApplicationNumberFilter( String filter){
    return filter !=null && (isExactly13Digits(filter));
  }

  private static boolean isExactly6Digits(String filter) {

    return _6_DIGITS_PATTERN.matcher(filter).matches();
  }

  private static boolean isExactly13Digits(String filter) {
    return _13_DIGITS_PATTERN.matcher(filter).matches();
  }

  private static boolean endsWithSlashAnd6Digits(String filter) {

    String afterSlash = StringUtils.substringAfterLast(filter, "/");
    return _6_DIGITS_PATTERN.matcher(afterSlash).matches();
  }

}
