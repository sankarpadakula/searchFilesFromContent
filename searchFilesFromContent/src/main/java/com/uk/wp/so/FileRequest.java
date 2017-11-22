package com.uk.wp.so;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FileRequest {

  List<String> match;

  public List<String> getMatch() {
    return match;
  }

  public void setMatch(List<String> match) {
    this.match = match;
  }
  
  
}
