package com.uk.wp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.uk.wp.so.FileListSO;
import com.uk.wp.so.FileSO;

public class FileHelper {

  final static Logger LOGGER = Logger.getLogger(FileHelper.class);

  public static final String PROPERTY_FILENAME = "/WEB-INF/application.properties";
  public static final String INDEX_DIRECTORY = "../indexDirectory";
  public static final String ROOT_DIRECTORY = "file.root.directory";
  public static final String FIELD_PATH = "path";
  public static final String FIELD_MODIFIED = "modified";
  public static final String FIELD_CONTENTS = "contents";

  /**
   * Iterate the all folders and sub folders and list down get the file name
   * 
   * @param dir: parent directory to look for the files
   * @return list of file names
   */
  public static Collection<File> listFiles(File dir) {
    return FileUtils.listFiles(dir, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
  }

  /**
   * 
   * @param contentToSearch:list of words to search in the document
   * @return FileListSO, which has all words matched document path and content
   * @throws IOException
   */
  public static FileListSO filterFilesByContentMatch(List<String> contentToSearch) throws IOException {
    FileListSO listSO = new FileListSO();
    StringBuilder builder = new StringBuilder();
    for (String word : contentToSearch) {
      builder.append(word + " AND ");
    }
    IndexSearcher searcher = createSearcher();
    try {
      TopDocs topDocs = searchInContent(searcher, builder.substring(0, builder.length() - 5));
      for (ScoreDoc sd : topDocs.scoreDocs) {
        FileSO so = new FileSO();
        Document d = searcher.doc(sd.doc);
        so.setFileName(String.format(d.get(FIELD_PATH)));
        so.setContent(String.format(d.get(FIELD_CONTENTS)));
        listSO.addFile(so);
      }
    } catch (Exception e) {
      LOGGER.error("Exception on searching " + builder.toString());
    }
    return listSO;
  }

  private static IndexSearcher createSearcher() throws IOException {
    Directory dir = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
    IndexReader reader = DirectoryReader.open(dir);
    IndexSearcher searcher = new IndexSearcher(reader);
    return searcher;
  }

  public static TopDocs searchInContent(IndexSearcher searcher, String textToFind) throws Exception {
    // Create search query
    QueryParser qp = new QueryParser(FIELD_CONTENTS, new StandardAnalyzer());
    Query query = qp.parse(textToFind);

    // search the index
    TopDocs hits = searcher.search(query, 10);
    return hits;
  }

  /**
   * create index for the root directory
   * @param docsPath
   */
  public static void createIndex(String docsPath) {
    File docsdirectory = new File(docsPath);
    File indexDirectory = new File(INDEX_DIRECTORY);
    if (!docsdirectory.exists()) {
      docsdirectory.mkdir();
    }
    if (!indexDirectory.exists()) {
      indexDirectory.mkdir();
    }
    // Input Path Variable
    final Path docDir = Paths.get(docsPath);

    try {
      // org.apache.lucene.store.Directory instance
      Directory dir = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

      // analyzer with the default stop words
      Analyzer analyzer = new StandardAnalyzer();

      // IndexWriter Configuration
      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
      iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);

      // IndexWriter writes new index files to the directory
      final IndexWriter writer = new IndexWriter(dir, iwc);
      writer.deleteAll();
      // Its recursive method to iterate all files and directories

      // Directory?
      if (Files.isDirectory(docDir)) {
        // Iterate directory
        Files.walkFileTree(docDir, new SimpleFileVisitor<Path>() {
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            try {
              // Index this file
              indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
            } catch (IOException ioe) {
              ioe.printStackTrace();
            }
            return FileVisitResult.CONTINUE;
          }
        });
      } else {
        // Index this file
        indexDoc(writer, docDir, Files.getLastModifiedTime(docDir).toMillis());
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * index the each and every file in to a separate directory
   * @param writer
   * @param file
   * @param lastModified
   * @throws IOException
   */
  private static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
    try (InputStream stream = Files.newInputStream(file)) {
      // Create lucene Document
      Document doc = new Document();
      doc.add(new StringField(FIELD_PATH, file.toAbsolutePath().toString(), Field.Store.YES));
      doc.add(new LongField(FIELD_MODIFIED, lastModified, Store.YES));
      doc.add(new TextField(FIELD_CONTENTS, new String(Files.readAllBytes(file)), Store.YES));

      writer.updateDocument(new Term(FIELD_PATH, file.toString()), doc);
    }
  }

  /**
   * read application properties
   * @param realPath: path of the file to read the properties
   * @return
   */
  public static Properties loadProperties(String realPath) {
    Properties props = new Properties();
    try {
      props.load(new FileInputStream(new File(realPath)));
    } catch (IOException ex) {
    }
    return props;
  }

  @Deprecated
  public static List<FileSO> contentInFiles(String dir, List<String> textToMatch) {
    List<FileSO> files = new ArrayList<FileSO>();
    Collection<File> listOfFiles = FileHelper.listFiles(new File(dir));
    for (File file : listOfFiles) {
      try {
        String content = new String(Files.readAllBytes(Paths.get(file.getName())));
        if (wordsMatched(content, textToMatch)) {
          FileSO so = new FileSO();
          so.setFileName(file.getName());
          so.setContent(content);
          files.add(so);
        }
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }
    }
    return files;
  }

  @Deprecated
  public static FileSO contentInFiles(String dir, List<String> textToMatch, boolean requiredContnet) {
    FileSO so = new FileSO();
    Collection<File> listOfFiles = FileHelper.listFiles(new File(dir));
    FileOutputStream fos = null;
    ZipOutputStream zos = null;
    try {
      String fileName = textToMatch.toString().replaceAll(" ", "_") + ".zip";
      fos = new FileOutputStream(fileName);
      zos = new ZipOutputStream(fos);
      for (File file : listOfFiles) {
        try {
          String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
          if (wordsMatched(content, textToMatch)) {
            so.setFileName(fileName);
            if (requiredContnet) {
              addToZipFile(file, zos);
            }
          }
        } catch (IOException e) {
          System.out.println(e.getMessage());
        }
      }
    } catch (FileNotFoundException e1) {

    } finally {
      try {
        zos.close();
        fos.close();
      } catch (IOException e) {
      }
    }
    return so;
  }

  @Deprecated
  private static boolean wordsMatched(String content, List<String> textToMatch) {
    if (textToMatch.isEmpty())
      return false;
    for (String word : textToMatch) {
      if (!content.contains(word)) {
        return false;
      }
    }
    return true;
  }

  /**
   * archive the list of files
   * @param file
   * @param zos
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static void addToZipFile(File file, ZipOutputStream zos) throws FileNotFoundException, IOException {
    System.out.println("Writing '" + file.getName() + "' to zip file");

    FileInputStream fis = new FileInputStream(file);
    ZipEntry zipEntry = new ZipEntry(file.getName());
    zos.putNextEntry(zipEntry);

    byte[] bytes = new byte[1024];
    int length;
    while ((length = fis.read(bytes)) >= 0) {
      zos.write(bytes, 0, length);
    }
    zos.closeEntry();
    fis.close();
  }
}
