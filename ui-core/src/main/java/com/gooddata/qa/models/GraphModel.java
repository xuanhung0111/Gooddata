package com.gooddata.qa.models;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class GraphModel {
    private List<GraphNode> nodes;
    private List<GraphEdge> edges;

    private static final String NODE = "node";
    private static final String EDGE = "edge";

    private GraphModel() {
        //disable default constructor
    }

    private GraphModel(List<GraphNode> nodes, List<GraphEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<GraphNode> getNodes() {
        return nodes;
    }

    public List<GraphEdge> getEdges() {
        return edges;
    }

    public static GraphModel readGraphXPath(final File xmlFile) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            NodeList nodeList = (NodeList) xpath.evaluate("/svg/g/g", doc,
                    XPathConstants.NODESET);
            List<GraphNode> nodes = new ArrayList<>();
            List<GraphEdge> edges = new ArrayList<>();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    final String id = element.getAttribute("id");
                    final String title = node.getChildNodes().item(0).getTextContent();
                    final String nodeClass = element.getAttribute("class");

                    switch (nodeClass) {
                        case NODE:
                            nodes.add(new GraphNode(id, title, getLinkText(element)));
                            break;
                        case EDGE:
                            edges.add(new GraphEdge(id, title));
                            break;
                        default:
                            break;
                    }
                }
            }

            Collections.sort(nodes, (a, b) -> a.text.compareTo(b.text));

            List<GraphEdge> refinedEdges = postProcessEdges(nodes, edges);
            Collections.sort(refinedEdges, (a, b) -> a.text.compareTo(b.text));

            return new GraphModel(nodes, refinedEdges);
        } catch (Exception e) {
            throw new RuntimeException("Can not process graph file", e);
        }
    }

    private static List<GraphEdge> postProcessEdges(final List<GraphNode> nodes, final List<GraphEdge> edges) {
        Map<String, String> nodeMap = nodes.stream().collect(Collectors.toMap(x -> x.getId(), x -> x.getText()));

        List<GraphEdge> newEdges = new ArrayList<>();
        edges.forEach(e -> {
            String[] arr = e.getTitle().split("->");
            String startNode = arr[0];
            String endNode = arr[1];
            String text = String.format("%s->%s", nodeMap.get(startNode), nodeMap.get(endNode));
            newEdges.add(new GraphEdge(e.getId(), e.getTitle(), text));
        });

        return newEdges;
    }

    private static String getLinkText(Element root) throws RuntimeException {
        NodeList nl = root.getChildNodes();
        for (int j = 0; j < nl.getLength(); j++) {
            Node node = nl.item(j);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;
                if ("a".equals(node.getNodeName())) {
                    return e.getAttribute("xlink:title");
                } else if ("g".equals(node.getNodeName())) {
                    Element a = (Element) e.getFirstChild();
                    return a.getAttribute("xlink:title");
                }
            }
        }
        throw new RuntimeException("Link title not found at: " + root.getNodeName());
    }

    public static class GraphNode {
        private String id;
        private String title;
        private String text;

        private GraphNode(String id, String title, String text) {
            this.id = id;
            this.title = title;
            this.text = text;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getText() {
            return text;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof GraphNode)) {
                return false;
            }
            GraphNode node = (GraphNode) obj;
            return Objects.equals(this.text, node.getText());
        }

        @Override
        public int hashCode() {
            return Objects.hash(text);
        }

        @Override
        public String toString() {
            return "id=" + id + ";title=" + title + ";text:" + text;
        }
    }

    public static class GraphEdge {
        private String id;
        private String title;

        private String text;

        private GraphEdge(String id, String title) {
            this.id = id;
            this.title = title;
            this.text = title;
        }

        private GraphEdge(String id, String title, String text) {
            this.id = id;
            this.title = title;
            this.text = text;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getText() {
            return text;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof GraphEdge)) {
                return false;
            }
            GraphEdge edge = (GraphEdge) obj;
            return Objects.equals(this.text, edge.getText());
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.text);
        }

        @Override
        public String toString() {
            return "id=" + this.id + ";title=" + this.title + ";text=" + this.text;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof GraphModel)) {
            return false;
        }
        GraphModel graph = (GraphModel) obj;
        return Objects.equals(this.nodes, graph.getNodes()) && Objects.equals(this.edges, graph.getEdges());
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes, edges);
    }
}
