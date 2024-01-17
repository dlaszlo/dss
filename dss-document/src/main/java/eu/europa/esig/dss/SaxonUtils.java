package eu.europa.esig.dss;

import java.util.ArrayList;
import java.util.List;

import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.utils.Utils;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SaxonUtils {

  private final static Processor processor = new Processor(false);

  public static NodeList getNodeList(Node xmlNode, String xPathExpression) {
    try {
      XdmNode xdmNode = processor.newDocumentBuilder().wrap(xmlNode);
      XPathCompiler xPathCompiler = processor.newXPathCompiler();
      xPathCompiler.declareNamespace("ds", "http://www.w3.org/2000/09/xmldsig#");
      xPathCompiler.declareNamespace("xades", "http://uri.etsi.org/01903/v1.1.1#");
      xPathCompiler.declareNamespace("xades132", "http://uri.etsi.org/01903/v1.3.2#");
      xPathCompiler.declareNamespace("xades141", "http://uri.etsi.org/01903/v1.4.1#");
      XPathExecutable expression = null;
      expression = xPathCompiler.compile(xPathExpression);
      XPathSelector selector = expression.load();
      selector.setContextItem(xdmNode);
      List<XdmNode> nodeList = selector.stream().asListOfNodes();
      return new SimpleNodeList(nodeList);
    } catch (SaxonApiException e) {
      throw new DSSException(String.format("Unable to find a NodeList by the given xPathString '%s'. Reason : %s",
          xPathExpression, e.getMessage()), e);
    }
  }

  public static String getValue(Node xmlNode, String xPathExpression) {
    try {
      NodeList nodeList = getNodeList(xmlNode, xPathExpression);
      if (nodeList.getLength() == 0) {
        throw new NullPointerException();
      } else if (nodeList.getLength() > 1) {
        throw new IllegalArgumentException();
      } else {
        return Utils.trim(nodeList.item(0).getTextContent());
      }
    } catch (Exception e) {
      throw new DSSException(String.format("Unable to extract value of the node. Reason : %s", e.getMessage()), e);
    }
  }

  public static class SimpleNodeList implements NodeList {

    private final List<Node> nodeList;

    public SimpleNodeList(List<XdmNode> nodeList) {
      this.nodeList = new ArrayList<>(nodeList.size());
      for (XdmNode xdmNode : nodeList) {
        this.nodeList.add((Node) xdmNode.getExternalNode());
      }
    }

    @Override
    public Node item(int index) {
      return nodeList.get(index);
    }

    @Override
    public int getLength() {
      return nodeList.size();
    }
  }

}
