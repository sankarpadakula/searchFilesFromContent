package com.uk.wp.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.uk.wp.service.FileSearchservice;
import com.uk.wp.so.FileListSO;
import com.uk.wp.util.ContextListener;
import com.uk.wp.util.FileHelper;

public class FileSearchServiceImpl implements FileSearchservice {

  final static Logger LOGGER = Logger.getLogger(FileSearchServiceImpl.class);

  /**
   * look for the matching strings(no fuzzy word) and returning filenames and content
   * 
   */
  public FileListSO fileContentListBycontent(List<String> textToMatch) throws IOException {
    LOGGER.info("Filtering files with content which contains matching text " + textToMatch);
    FileListSO so = FileHelper.filterFilesByContentMatch(textToMatch);
    return so;
  }

  /**
   * delete existing index directory
   * re-create index files
   */
  public void refreshIndex() {
    LOGGER.info("refresh the indexDirectory");
    new File(FileHelper.INDEX_DIRECTORY).deleteOnExit();
    String rootDir = ContextListener.getProperites().getProperty(FileHelper.ROOT_DIRECTORY);
    FileHelper.createIndex(rootDir);
  }

  
}
