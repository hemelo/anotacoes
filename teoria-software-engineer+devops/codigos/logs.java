package snippets

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class MyLogger {
  static final Logger logger = LogManager.getLogger(MyLogger.class.getName());

  public void log(String logMessage) {
    logger.info(logMessage);
  }
}
