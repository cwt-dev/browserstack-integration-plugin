package com.browserstack.automate.ci.common.report;

import com.browserstack.automate.ci.common.AutomateTestCase;
import com.browserstack.automate.ci.common.model.BrowserStackSession;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Shirish Kamath
 * @author Anirudha Khanna
 */
public class XmlReporter {
  public static void main(String[] args) throws IOException {
    parse(new File(
//            "/Users/eladsulami/browserstack-integration-plugin/src/test/resources/REPORT-com.browserstack.automate.application.tests.TestCaseWithFourUniqueSessions.xml"
//            "/Users/eladsulami/browserstack-integration-plugin/src/test/resources/REPORT-CHROME_67.0.3396.62_WINDOWS_oauth-token-tests.xml"
            "/Users/eladsulami/browserstack-integration-plugin/src/test/resources/REPORT-CHROME_77.0.3865.75_WINDOWS_HomeCards.xml"
    ));
  }

  public static Map<String, String> parse(File f) throws IOException {
    Map<String, String> testSessionMap = new HashMap<String, String>();
    Document doc;

    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      doc = dBuilder.parse(f);
    } catch (Exception e) {
      throw new IOException(e.getMessage());
    }

    Element documentElement = doc.getDocumentElement();
    if (documentElement.getTagName().equals("testsuites")){
      documentElement = ((Element)documentElement.getElementsByTagName("testsuite").item(0));
    }
    NodeList testCaseNodes = documentElement.getElementsByTagName("testcase");

    for (int i = 0; i < testCaseNodes.getLength(); i++) {
      Node n = testCaseNodes.item(i);

      if (n.getNodeType() == Node.ELEMENT_NODE) {
        Element el = (Element) n;
        if (el.hasChildNodes()) {
          String testId = el.getAttribute("id");
          if (testId == null || testId.equals("")){
            testId = getDisplayName(el);
          }
          NodeList sessionNode = el.getElementsByTagName("session");
          if (sessionNode.getLength() > 0
              && sessionNode.item(0).getNodeType() == Node.ELEMENT_NODE) {
            NodeList projectTypeNode = el.getElementsByTagName("projectType");
            String projectType =
                projectTypeNode.getLength() > 0 ? projectTypeNode.item(0).getTextContent() : "";
            Gson gson = new GsonBuilder().create();

            BrowserStackSession session =
                new BrowserStackSession(sessionNode.item(0).getTextContent(), projectType);
            testSessionMap.put(testId, gson.toJson(session));
          }
        }
      }
    }

    return testSessionMap;
  }

  private static String getDisplayName(Element testCase){
    String testClassName = testCase.getAttribute("classname");
    String nameAttr = testCase.getAttribute("name");
    if (nameAttr.contains(".")) {
      testClassName = nameAttr.substring(0, nameAttr.lastIndexOf(46));
      nameAttr = nameAttr.substring(nameAttr.lastIndexOf(46) + 1);
    }


    return testClassName +
            "." +
            AutomateTestCase.stripTestParams(nameAttr) +
                   "{0}";
  }
}
