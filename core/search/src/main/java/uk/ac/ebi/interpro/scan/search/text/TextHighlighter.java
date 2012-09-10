package uk.ac.ebi.interpro.scan.search.text;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

/**
 * Highlight query terms in search results.
 * This class can be removed when this functionality is made available in the EBI Search API.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public final class TextHighlighter {

    private static final Version LUCENE_VERSION = Version.LUCENE_35;
    private static final String  FIELD_CONTENTS = "contents";

    private static final Analyzer    ANALYZER   = new EnglishAnalyzer(LUCENE_VERSION);
    private static final QueryParser PARSER     = new QueryParser(LUCENE_VERSION, FIELD_CONTENTS, ANALYZER);

    private static final Formatter   TITLE_FORMATTER       = new SimpleHTMLFormatter("<span class='entry_title_high'>", "</span>");
    private static final Formatter   DESCRIPTION_FORMATTER = new SimpleHTMLFormatter("<span class='entry_sum_high'>", "</span>");

    private static final int SNIPPET_MAX    = 3;
    private static final int SNIPPET_LEN    = 60;
    private static final String SNIPPET_SEP = "...";
    private static final Pattern MID_SENTANCE_PATTERN = Pattern.compile("^[a-z].*");

    private final Highlighter titleHighlighter;
    private final Highlighter descriptionHighlighter;

    public TextHighlighter(String query) {

        if (query.isEmpty()) {
            throw new IllegalArgumentException("Query must not be empty");
        }

        Query q;
        try {
            q = PARSER.parse(query);
        }
        catch (ParseException e) {
            throw new IllegalArgumentException("Could not parse " + query, e);
        }

        QueryScorer scorer = new QueryScorer(q, FIELD_CONTENTS, FIELD_CONTENTS);

        titleHighlighter = new Highlighter(TITLE_FORMATTER, scorer);
        titleHighlighter.setTextFragmenter(new SimpleFragmenter(Integer.MAX_VALUE));

        descriptionHighlighter = new Highlighter(DESCRIPTION_FORMATTER, scorer);
        descriptionHighlighter.setTextFragmenter(new SimpleFragmenter(SNIPPET_LEN));

    }

    public String highlightTitle(String title) {
        return highlight(title, titleHighlighter, title);
    }

    public String highlightDescription(String description) {
        if (description.equals("")) {
            return description;
        }
        int len = Math.min(description.length(), 180);
        description = highlight(description, descriptionHighlighter, description.substring(0, len - 1) + SNIPPET_SEP);
        return tidyFragments(description);

    }

    private String highlight(String text, Highlighter highlighter, String defaultText) {
        TokenStream tokenStream = ANALYZER.tokenStream(FIELD_CONTENTS, new StringReader(text));
        String fragments = "";
        try {
            fragments = highlighter.getBestFragments(tokenStream, text, SNIPPET_MAX, SNIPPET_SEP);
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not highlight " + text, e);
        }
        catch (InvalidTokenOffsetsException e) {
            throw new IllegalStateException("Could not highlight " + text, e);
        }
        return fragments.length() == 0 ? defaultText : fragments;
    }

    /**
     * Perform final tidying of the fragments text ready for display to the end user
     * @param fragments Piece of text containing all fragments
     * @return Tidied fragments text
     */
    private String tidyFragments(String fragments) {
        if (fragments.startsWith(",")) {
            fragments = fragments.substring(1);
        }
        fragments = fragments.trim();
        if(MID_SENTANCE_PATTERN.matcher(fragments).matches()) {
            // Not at the start of a sentence so manually add the "..."
            fragments = "..." + fragments;
        }
        if (!fragments.endsWith(".")) {
            // Not the end of a sentence so manually add the "..."
            fragments = fragments + "...";
        }
        return fragments;
    }

}
