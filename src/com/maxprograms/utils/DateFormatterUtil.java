package com.maxprograms.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Contains utility methods to parse the date.
 * 
 * @author alex_smarandache
 */
public class DateFormatterUtil {

  /**
   * The hidden constructor. Private to avoid instantiations.
   *
   * @throws UnsupportedOperationException when invoked.
   */
  private DateFormatterUtil() {
    throw new UnsupportedOperationException("This class should not be instantied!");
  }

  
  /**
   * The supported date formats.
   */
  private static final List<String> DATE_FORMATS =
      Arrays.asList(
          "MMM dd, yyyy, h:mm:ss a",
          "yyyy-MM-dd HH:mm", 
          "yyyy-MM-dd", 
          "MM/dd/yyyy HH:mm",
          "MM/dd/yyyy", 
          "yyyy-MM-dd'T'HH:mm:ss",
          "MMMM dd, yyyy",
          "dd-MM-yyyy HH:mm", 
          "dd/MM/yyyy HH:mm"
          );

  /**
   * Parse the string into a date.
   * 
   * @param dateAsString The date in string format.
   * 
   * @return The parsed date.
   * 
   * @throws ParseException When the date cannot be parsed.
   */
  public static Date parseDate(String dateAsString) throws ParseException {
    ParseException ex = null;
    for (String format : DATE_FORMATS) {
      try {
        SimpleDateFormat sdf = createDateFormatFor(format);
        return sdf.parse(dateAsString);
      } catch (ParseException e) {
        if(ex == null) {
          ex = e;
        }
       
      }
    }
   
    throw ex != null ? ex : new ParseException("Date cannot be parsed", 0);
  }
  
  /**
   * Create the date format.
   * 
   * @param format    The format.
   * 
   * @return The created format.
   */
  private static SimpleDateFormat createDateFormatFor(String format) {
    // Check if the format might be ambiguous and choose the locale accordingly
    if (format.equals("MM/dd/yyyy") || format.equals("MM/dd/yyyy HH:mm")) {
        // U.S. format (MM/dd/yyyy)
        return new SimpleDateFormat(format, Locale.US);
    } else if (format.equals("dd/MM/yyyy") || format.equals("dd/MM/yyyy HH:mm")) {
        // European format (dd/MM/yyyy)
        return new SimpleDateFormat(format, Locale.UK);  // UK locale for day/month/year
    } else {
        // Default locale (English)
        return new SimpleDateFormat(format, Locale.ENGLISH);
    }
}

}
