package com.uk.wp.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.uk.wp.service.FileSearchservice;
import com.uk.wp.service.impl.FileSearchServiceImpl;
import com.uk.wp.so.FileListSO;
import com.uk.wp.util.FileHelper;

public class FileSearchServiceTest {

  FileSearchservice service = new FileSearchServiceImpl();

  @BeforeClass
  public static void init() {
    FileHelper.createIndex("src/main/resources/data");
  }

  @Test
  public void testFileContentList_NoMatching() throws IOException {
    FileListSO listSO = service.fileContentListBycontent(Arrays.asList("zyx"));
    Assert.assertTrue(listSO.getFiles().isEmpty());
  }

  @Test
  public void testFileContentList_Matching() throws IOException {
    FileListSO listSO = service.fileContentListBycontent(Arrays.asList("private"));
    Assert.assertNotNull(listSO.getFiles());
    Assert.assertFalse(listSO.getFiles().isEmpty());
  }

  @Test
  public void testFileContentList_ComplexMatching() throws IOException {
    FileListSO listSO = service.fileContentListBycontent(Arrays.asList("private", "girl"));
    Assert.assertFalse(listSO.getFiles().isEmpty());

  }

  @Test
  public void testFileContentListBycontent_invalidContent() throws IOException {
    final Random random = new Random(System.nanoTime());
    FileListSO fileListSO = service.fileContentListBycontent(Arrays.asList(random.nextLong() + ""));
    Assert.assertTrue("Filed to get matching files", fileListSO.getFiles().isEmpty());
  }
}
