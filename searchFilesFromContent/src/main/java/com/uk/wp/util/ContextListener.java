package com.uk.wp.util;

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

public class ContextListener implements ServletContextListener {

  final static Logger LOGGER = Logger.getLogger(ContextListener.class);

  static Properties properties;

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    new File(FileHelper.INDEX_DIRECTORY).deleteOnExit();
  }

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    String propertyFilename = servletContextEvent.getServletContext().getRealPath(FileHelper.PROPERTY_FILENAME);
    properties = FileHelper.loadProperties(propertyFilename);
    String root = properties.getProperty(FileHelper.ROOT_DIRECTORY);
    LOGGER.info("Creating index for the directory " + root);
    FileHelper.createIndex(root);
  }

  public static Properties getProperites() {
    return properties;
  }
}
