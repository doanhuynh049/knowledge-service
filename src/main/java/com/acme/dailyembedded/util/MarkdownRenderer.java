package com.acme.dailyembedded.util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.stereotype.Component;

@Component("markdownRenderer")
public class MarkdownRenderer {

  private final Parser parser;
  private final HtmlRenderer renderer;

  public MarkdownRenderer() {
    MutableDataSet options = new MutableDataSet();
    this.parser = Parser.builder(options).build();
    this.renderer = HtmlRenderer.builder(options).build();
  }

  public String render(String markdown) {
    if (markdown == null || markdown.trim().isEmpty()) {
      return "<p>No content available.</p>";
    }

    Document document = parser.parse(markdown);
    return renderer.render(document);
  }
}
