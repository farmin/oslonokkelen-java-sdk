package no.kommune.oslo.nokkelen.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class BannerPrinter {

  private final static Logger log = LoggerFactory.getLogger(BannerPrinter.class);

  public static void printBanner() {
    try {
      URL resource = BannerPrinter.class.getResource("/banner.txt");
      if (resource == null) {
        return;
      }

      try (InputStream is = resource.openStream()) {
        Scanner scanner = new Scanner(is).useDelimiter("\\A");
        String banner = scanner.hasNext() ? scanner.next() : "";
        System.out.println(banner);
      }
    }
    catch (Exception ex) {
      log.warn("Failed to read banner", ex);
    }
  }

}
