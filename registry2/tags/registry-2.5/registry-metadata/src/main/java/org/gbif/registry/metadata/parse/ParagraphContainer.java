package org.gbif.registry.metadata.parse;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Class to temporarily keep paragraph strings before they are used as a single,
 * concatenated string argument in other rules.
 * Digester needs public access to this otherwise package scoped class.
 */
public class ParagraphContainer {

  private static Joiner BR_JOIN = Joiner.on("<br/>");
  private List<String> paragraphs = Lists.newArrayList();

  public void appendParagraph(String para) {
    if (!Strings.isNullOrEmpty(para)) {
      paragraphs.add(para.trim());
    }
  }

  public String toString() {
    return BR_JOIN.join(paragraphs);
  }
}