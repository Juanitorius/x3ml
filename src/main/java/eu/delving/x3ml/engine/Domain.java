/*==============================================================================
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
==============================================================================*/
package eu.delving.x3ml.engine;

import org.w3c.dom.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static eu.delving.x3ml.X3MLEngine.exception;
import static eu.delving.x3ml.engine.X3ML.DomainElement;
import static eu.delving.x3ml.engine.X3ML.GeneratedValue;
import static eu.delving.x3ml.engine.X3ML.LinkElement;
import static eu.delving.x3ml.engine.X3ML.PathElement;
import static eu.delving.x3ml.engine.X3ML.RangeElement;
import gr.forth.Utils;
import static org.joox.JOOX.$;

/**
 * The domain entity handled here. Resolution delegated. Holding variables here.
 *
 * @author Gerald de Jong <gerald@delving.eu>
 * @author Nikos Minadakis <minadakn@ics.forth.gr>
 * @author Yannis Marketakis <marketak@ics.forth.gr>
 */
public class Domain extends GeneratorContext {

    public final DomainElement domain;
    public EntityResolver entityResolver;
    private Map<String, X3ML.GeneratedValue> variables = new TreeMap<String, X3ML.GeneratedValue>();

    public Domain(Root.Context context, DomainElement domain, Node node, int index) {
        super(context, null, node, index);
        this.domain = domain;
    }

    @Override
    public GeneratedValue get(String variable) {
        return variables.get(variable);
    }

    @Override
    public void put(String variable, GeneratedValue generatedValue) {
        variables.put(variable, generatedValue);
    }

    public boolean resolve() {
        if (conditionFails(domain.target_node.condition, this)) {
            return false;
        }
        entityResolver = new EntityResolver(context.output(), domain.target_node.entityElement, this);
        return entityResolver.resolve(0,0);
    }

    public List<Link> createLinkContexts(LinkElement linkElement, String domainForeignKey, String rangePrimaryKey,
            String intermediateFirst, String intermediateSecond, String node_inside) {

        PathElement pathElement = linkElement.path;

        String pathExpression = pathElement.source_relation.relation.get(0).expression;

        RangeElement rangeElement = linkElement.range;

        String rangeExpression = rangeElement.source_node.expression;

        if (rangeExpression == null) {
            throw exception("Range source absent: " + linkElement);
        }
        List<Link> links = new ArrayList<Link>();
        int index = 1;


        int size = context.input().countNodes(node.getParentNode(), node_inside + "//" + intermediateFirst + "/text()");

        for (int count = 1; count <= size; count++) {
            if (context.input().valueAt(node.getParentNode(),
                    node_inside + "[" + count + "]//" + intermediateFirst + "/text()")
                    .equals(context.input().valueAt(node, domainForeignKey + "/text()"))) {
                
                String intermediateValue=context.input().valueAt(node, node_inside + "[" + count + "]//" + intermediateSecond + "/text()");
                if(intermediateValue.isEmpty()){
                    Utils.printErrorMessages("ERROR FOUND: Empty value for "+node_inside+"/"+intermediateSecond+". The node from the XML input is:\n"+$(node).toString());
                }
                List<Node> rangeNodes = context.input().rootNodeList(
                        domain.source_node.expression,
                        pathExpression,
                        context.input().valueAt(node, node_inside + "[" + count + "]//" + intermediateSecond + "/text()"),
                        rangeExpression,
                        rangePrimaryKey + "/text()"
                );

                for (Node rangeNode : rangeNodes) {

                    Path path = new Path(context, this, pathElement, node, index);

                    Range range = new Range(context, path, rangeElement, rangeNode, index);

                    GeneratorContext.appendAssociationTable("/"+node.getParentNode().getNodeName()+"[1]"+"/"+node_inside + "[" + count + "]/" + intermediateFirst.replaceAll("//", "/"), context.input().valueAt(node, domainForeignKey + "/text()"));
                    GeneratorContext.appendAssociationTable("/"+node.getParentNode().getNodeName()+"[1]"+"/"+node_inside + "[" + count + "]//" + intermediateSecond.replaceAll("//", "/"), context.input().valueAt(rangeNode, rangePrimaryKey + "/text()"));
                    GeneratorContext.appendAssociationTable(path.toStringAssoc()+"/"+domainForeignKey+"-FOREIGNKEY", context.input().valueAt(rangeNode, rangePrimaryKey + "/text()"));
                    GeneratorContext.appendAssociationTable(range.toStringAssoc()+"/"+rangePrimaryKey+"-FOREIGNKEY", context.input().valueAt(node, domainForeignKey + "/text()"));
                    
                    Link link = new Link(path, range);
                    if (link.resolve()) {
                        links.add(link);
                    }
                    index++;
                }

            }

        }

        return links;
    }

    public List<Link> createLinkContexts(LinkElement linkElement, String domainForeignKey, String rangePrimaryKey) {
        PathElement pathElement = linkElement.path;

        String pathExpression = pathElement.source_relation.relation.get(0).expression;
        RangeElement rangeElement = linkElement.range;
        String rangeExpression = rangeElement.source_node.expression;
        if (rangeExpression == null) {
            throw exception("Range source absent: " + linkElement);
        }
        List<Link> links = new ArrayList<Link>();
        int index = 1;
        List<Node> rangeNodes = context.input().rootNodeList(
                domain.source_node.expression,
                pathExpression,
                context.input().valueAt(node, domainForeignKey + "/text()"),
                rangeExpression,
                rangePrimaryKey + "/text()"
        );
        for (Node rangeNode : rangeNodes) {
            Path path = new Path(context, this, pathElement, node, index);
            Range range = new Range(context, path, rangeElement, rangeNode, index);
            
            GeneratorContext.appendAssociationTable(path.toStringAssoc()+"/"+domainForeignKey+"-FOREIGNKEY", context.input().valueAt(node, domainForeignKey + "/text()"));
            GeneratorContext.appendAssociationTable(range.toStringAssoc()+"/"+rangePrimaryKey+"-FOREIGNKEY", context.input().valueAt(node, domainForeignKey + "/text()"));
            
            Link link = new Link(path, range);
            if (link.resolve()) {
                links.add(link);
            }
            index++;
        }
        return links;
    }

    public List<Path> createPathContexts(PathElement path) {
        if (path.source_relation == null) {
            throw exception("Path source absent");
        }
        List<Path> paths = new ArrayList<Path>();
        int index = 1;
        for (Node pathNode : context.input().nodeList(node, path.source_relation.relation.get(0).expression)) {
            Path pathContext = new Path(context, this, path, pathNode, index++);
            if (pathContext.resolve()) {
                paths.add(pathContext);
            }
        }
        return paths;
    }

    public void link() {
        entityResolver.link();
    }
}
