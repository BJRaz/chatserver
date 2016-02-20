package tfud.parsers;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import java.util.regex.*;
import java.util.Vector;

/**
 * @author BJR
 */
public class TextParser {

    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;
    private Document doc;
    private String filename;

    private Vector regReplace;

    private Pattern pattern;
    private Matcher matcher;

    /**
     * Method TextParser
     *
     *
     * @param filename
     */
    public TextParser(String filename) {

        this.filename = filename;
        this.regReplace = new Vector();

        // TODO: Add your code here
        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);

            factory.setIgnoringElementContentWhitespace(true);

            builder = (factory).newDocumentBuilder();

            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {

                public void error(SAXParseException e) {
                    System.out.println("Error parsing xml: " + e.getMessage());
                }

                ;
    			public void fatalError(SAXParseException e) {
                    System.out.println("fatalError parsing xml: " + e.getMessage());
                }

                ;
    			public void warning(SAXParseException e) {
                    System.out.println("warning parsing xml: " + e.getMessage());
                }
            ;
        } );
    		
			
    	} catch (ParserConfigurationException e) {
            System.out.println("Error in Parser: " + e.getMessage());
        }
    }

    public String parseText(String text) {

        String result = text;

        //System.out.println("PARSER DBUG: " + text);
        try {

            result = result.replaceAll("<", "&lt;");

            /**
             * Notice .. this means that XML-file is read and parsed for every
             * data sendt via chatclient BAD design but allows for runtime
             * editing of patterns in the XML-file.
             */
            doc = builder.parse(new java.io.File(this.filename));

            Element root = doc.getDocumentElement();
            NodeList replace = doc.getElementsByTagName("replace");

            /*pattern = Pattern.compile("(:-\\)|:\\))");
			matcher = pattern.matcher(text);
             */
            for (int i = 0; i < replace.getLength(); i++) {
                Node regexp = replace.item(i).getFirstChild();
                Node repacestr = regexp.getNextSibling();
                result = result.replaceAll(regexp.getFirstChild().getNodeValue() + "", repacestr.getFirstChild().getNodeValue());
            }
        } catch (java.io.IOException io) {
            System.out.println("Error in IO: " + io.getMessage());
        } catch (SAXException sax) {
            System.out.println("Error in SAX-parsing: " + sax.getMessage());
        } catch (PatternSyntaxException se) {
            System.out.println("Error in Pattern-matching..: " + se.getMessage());
        } catch (Exception e) {
            System.out.println("Default error..: " + e.getMessage());
        }
        return result;
    }
}
