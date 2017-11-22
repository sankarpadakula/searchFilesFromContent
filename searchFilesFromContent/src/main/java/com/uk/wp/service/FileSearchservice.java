package com.uk.wp.service;

import java.io.IOException;
import java.util.List;

import com.uk.wp.so.FileListSO;

public interface FileSearchservice {

  FileListSO fileContentListBycontent(List<String> contentForfilter) throws IOException;

  void refreshIndex();
}
