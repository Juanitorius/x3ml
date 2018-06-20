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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphSimpleMem;
import com.hp.hpl.jena.sparql.core.Quad;
import javax.xml.namespace.NamespaceContext;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static eu.delving.x3ml.X3MLEngine.Output;
import static eu.delving.x3ml.X3MLEngine.exception;
import static eu.delving.x3ml.engine.X3ML.TypeElement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import java.util.Iterator;
import java.io.OutputStream;


/**
 * The output sent to a Jena graph model.
 *
 * @author Gerald de Jong &lt;gerald@delving.eu&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 */
public class ModelOutput implements Output {

    public static final DatasetGraph quadGraph=new DatasetGraphSimpleMem();
    private final Model model;
    private final NamespaceContext namespaceContext;

    public ModelOutput(Model model, NamespaceContext namespaceContext) {
        this.model = model;
        this.namespaceContext = namespaceContext;
    }

    @Override
    public Model getModel() {
        return model;
    }
    
    public String getNamespace(TypeElement typeElement){
        if (typeElement == null) {
            throw exception("Missing qualified name");
        }
        if (typeElement.getLocalName().startsWith("http:")){
            return typeElement.getLocalName();
        }else{
            String typeElementNamespace = namespaceContext.getNamespaceURI(typeElement.getPrefix());
            return typeElementNamespace+typeElement.getLocalName();
        }
    }

    public Resource createTypedResource(String uriString, TypeElement typeElement) {
        if (typeElement == null) {
            throw exception("Missing qualified name");
        }
        if (typeElement.getLocalName().startsWith("http:")){
            String typeElementNamespace = "";
            return model.createResource(uriString, model.createResource(typeElementNamespace + typeElement.getLocalName()));
        }else{
            String typeElementNamespace = namespaceContext.getNamespaceURI(typeElement.getPrefix());
            if(typeElementNamespace==null){
                throw exception("The namespace with prefix \""+typeElement.getPrefix()+"\" has not been declared");
            }
            return model.createResource(uriString, model.createResource(typeElementNamespace + typeElement.getLocalName()));
        }
    }
    
    /* Used for creating labels (rdfs:label or skos:label) */
    public Property createProperty(TypeElement typeElement) {
        if (typeElement == null) {
            throw exception("Missing qualified name");
        }
        
        if (typeElement.getLocalName().startsWith("http:")){
            String typeElementNamespace = "";
            return model.createProperty(typeElementNamespace, typeElement.getLocalName());
        }else{ 
            String typeElementNamespace = namespaceContext.getNamespaceURI(typeElement.getPrefix());
            if(typeElementNamespace==null){
                throw exception("The namespace with prefix \""+typeElement.getPrefix()+"\" has not been declared");
            }
            return model.createProperty(typeElementNamespace, typeElement.getLocalName());
        }
        
    }

    public Property createProperty(X3ML.Relationship relationship) {
        if (relationship == null) {
            throw exception("Missing qualified name");
        }
        if (relationship.getLocalName().startsWith("http:")){
            String propertyNamespace = "";
            return model.createProperty(propertyNamespace, relationship.getLocalName());
        }else if (relationship.getLocalName().equals("MERGE")){
            return null;
        }
        else{ 
            String propertyNamespace = namespaceContext.getNamespaceURI(relationship.getPrefix());
            if(propertyNamespace==null){
                throw exception("The namespace with prefix \""+relationship.getPrefix()+"\" has not been declared");
            }
            return model.createProperty(propertyNamespace, relationship.getLocalName());
        }
    }
    

    public Literal createLiteral(String value, String language) {
        return model.createLiteral(value, language);
    }

    public Literal createTypedLiteral(String value, TypeElement typeElement) {
        String literalNamespace = namespaceContext.getNamespaceURI(typeElement.getPrefix());
        String typeUri = literalNamespace + typeElement.getLocalName();
        if(literalNamespace == null) {  //we have a fully qualified namespace (e.g. http://www.w3.org/2001/XMLSchema#dateTime)
            typeUri=typeElement.getLocalName();
        }
        return model.createTypedLiteral(value, typeUri);
    }

    @Override
    public void writeXML(OutputStream out) {
        if(X3ML.RootElement.hasNamedGraphs){
            this.updateNamedgraphRefs(XPathInput.entireInputExportedRefUri);
            this.writeQuads(out);
        }else{
            model.write(out, "RDF/XML-ABBREV");
        }
    }
    
    private void updateNamedgraphRefs(String uri){
        Iterator<Quad> qIter=quadGraph.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
        while(qIter.hasNext())
            quadGraph.add(new ResourceImpl("http://default").asNode(), 
                          new ResourceImpl(uri).asNode(), 
                          new ResourceImpl("http://PX_is_refered_by").asNode(), 
                          new ResourceImpl(qIter.next().getGraph().getURI()).asNode());
    }
    
    public void writeXMLPlain(OutputStream out) {
        model.write(out, "RDF/XML");
    }
    
    public void writeNTRIPLE(OutputStream out) {
        model.write(out, "N-TRIPLE");
    }

    public void writeTURTLE(OutputStream out) {
        model.write(out, "TURTLE");
    }

    @Override
    public void write(OutputStream out, String format) {
        if ("application/n-triples".equalsIgnoreCase(format)) {
            writeNTRIPLE(out);
        } else if ("text/turtle".equalsIgnoreCase(format)) {
            writeTURTLE(out);
        } else if ("application/rdf+xml".equalsIgnoreCase(format)) {
            writeXML(out);
        } else if ("application/rdf+xml_plain".equalsIgnoreCase(format)){
            writeXMLPlain(out);
        }else {
            writeXML(out);
        }
    }
    
    public void writeQuads(OutputStream out){
        StmtIterator stIter=model.listStatements();
        String defaultGraphSpace="http://default";
        if(X3ML.Mappings.namedgraphProduced!=null && !X3ML.Mappings.namedgraphProduced.isEmpty()){
            defaultGraphSpace=X3ML.Mappings.namedgraphProduced;
        }
        Node defgraph=new ResourceImpl(defaultGraphSpace).asNode();
        while(stIter.hasNext()){
            Statement st=stIter.next();
            quadGraph.add(defgraph, st.getSubject().asNode(), st.getPredicate().asNode(), st.getObject().asNode());
        }
        RDFDataMgr.write(out, quadGraph, Lang.TRIG); // or NQUADS
        
    }

    @Override
    public String[] toStringArray() {
        return toString().split("\n");
    }

    @Override
    public String toString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeNTRIPLE(new PrintStream(baos));
        return new String(baos.toByteArray());
    }
}
