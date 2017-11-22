package com.uk.wp.so;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Files")
public class FileListSO {

  List<FileSO> files = new ArrayList<FileSO>();

  String remarks;

  public List<FileSO> getFiles() {
    return files;
  }

  public void setFiles(List<FileSO> fileList) {
    this.files = fileList;
  }

  public void addFile(FileSO file) {
    files.add(file);
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

}
