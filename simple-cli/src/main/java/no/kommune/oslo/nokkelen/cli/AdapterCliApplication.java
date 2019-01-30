package no.kommune.oslo.nokkelen.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.kommune.oslo.nokkelen.sdk.AdapterController;
import no.kommune.oslo.nokkelen.sdk.JacksonFactory;
import no.kommune.oslo.nokkelen.sdk.LocalState;
import no.kommune.oslo.nokkelen.sdk.OslonokkelenClient;
import no.kommune.oslo.nokkelen.sdk.auth.AuthClient;
import no.kommune.oslo.nokkelen.sdk.auth.ClientCredentials;
import no.kommune.oslo.nokkelen.sdk.auth.KeycloakAuthClient;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AdapterCliApplication<C extends AdapterController> {

  private final static Logger log = LoggerFactory.getLogger(AdapterCliApplication.class);

  public final void start(String... args) {
    log.info("Booting application...");

    BannerPrinter.printBanner();
    Options options = createOptions();

    if (args.length == 0) {
      printHelpMessage(options);
    }

    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    CommandLineParser parser = new DefaultParser();
    LocalState state = new LocalState();

    try {
      log.info("Parsing command line arguments...");
      CommandLine commandLine = parser.parse(options, args);

      if (commandLine.hasOption("h")) {
        printHelpMessage(options);
      }

      C controller = createController(commandLine);
      defineThings(controller, state);

      OslonokkelenClient oslonokkelenClient = createWebsocketClient(commandLine, controller);

      log.info("Starting controller");
      controller.start(state, executorService);

      Thread clientThread = startWebsocket(state, oslonokkelenClient);

      log.info("Registering shutdown hook");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        executorService.shutdown();
        stopWebsocketClient(oslonokkelenClient, clientThread);
        stopController(controller);
        waitForExecutorToShutDown(executorService);
      }));

      log.info("Starting client thread..");
      clientThread.start();
      clientThread.join();
    }
    catch (ParseException ex) {
      log.error("Failed to parse command line arguments: {}", Arrays.toString(args), ex);
      System.exit(1);
    }
    catch (Exception ex) {
      log.error("Failed to boot", ex);
      System.exit(2);
    }
  }

  private Thread startWebsocket(LocalState state, OslonokkelenClient oslonokkelenClient) {
    Runnable runnable = () -> {
      try {
        log.info("Starting websocket..");
        oslonokkelenClient.start(state);
      }
      catch (Exception ex) {
        throw new IllegalStateException("Failed to start client", ex);
      }
    };

    return new Thread(runnable, "websocket-client");
  }

  /**
   * Stop the thing accepting new requests.
   *
   * Todo: Ideally we should do this in two steps
   *       a) Stop accepting new requests
   *       b) Shut it donw, but only when any pending requests are done
   */
  private void stopWebsocketClient(OslonokkelenClient oslonokkelenClient, Thread clientThread) {
    try {
      log.debug("Interrupting client");
      oslonokkelenClient.stop();
      clientThread.interrupt();
      clientThread.join();
    } catch (InterruptedException ignored) { }
  }

  private void waitForExecutorToShutDown(ScheduledExecutorService executorService) {
    try {
      executorService.awaitTermination(2, TimeUnit.SECONDS);
    } catch (InterruptedException ignored) { }
  }

  /**
   * Give the implementation a chance to do a graceful
   * shutdown of things it might have going.
   */
  private void stopController(C controller) {
    try {
      log.debug("Shutting down controller");
      controller.close();
    }
    catch (Exception ex) {
      log.warn("Error during controller shutdown", ex);
    }
  }



  private void printHelpMessage(Options options) {
    HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.printHelp("Main", options);
    System.exit(0);
  }

  private AuthClient createAuthClient(CommandLine commandLine) {
    URI baseURI = URI.create(commandLine.getOptionValue("au"));
    String authenticationRealm = commandLine.getOptionValue("ar", "oslonokkel");

    return new KeycloakAuthClient(baseURI, authenticationRealm);
  }

  private ClientCredentials createClientCredentials(CommandLine commandLine) {
    String clientId = commandLine.getOptionValue("i");
    String clientSecret = commandLine.getOptionValue("s");

    return new ClientCredentials(clientId, clientSecret);
  }

  protected abstract C createController(CommandLine commandLine);

  protected abstract void defineThings(C controller, LocalState state) throws Exception;

  private OslonokkelenClient createWebsocketClient(CommandLine commandLine, C controller) throws URISyntaxException {
    AuthClient authClient = createAuthClient(commandLine);
    ClientCredentials clientCredentials = createClientCredentials(commandLine);
    URI uri = new URI(commandLine.getOptionValue("ws"));
    ObjectMapper jackson = JacksonFactory.createObjectMapper();

    return new OslonokkelenClient(uri, jackson, authClient, clientCredentials, controller);
  }

  private Options createOptions() {
    Options options = new Options();
    options.addOption("h", "Please help me!!");

    options.addRequiredOption("ws", "oslonokkel-ws-uri", true, "Oslonøkkelen websocket uri");
    options.addRequiredOption("au", "auth-uri", true, "Authentication uri");
    options.addRequiredOption("ar", "auth-realm", true, "Authentication realm");
    options.addRequiredOption("s", "auth-secret", true, "Client authentication secret");
    options.addRequiredOption("i", "adapter-id", true, "Adapter id");

    configureCommandLineOptions(options);
    return options;
  }

  protected abstract void configureCommandLineOptions(Options options);

}
