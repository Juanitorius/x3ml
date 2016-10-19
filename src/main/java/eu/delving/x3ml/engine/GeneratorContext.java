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
import static eu.delving.x3ml.X3MLEngine.exception;
import static eu.delving.x3ml.engine.X3ML.ArgValue;
import static eu.delving.x3ml.engine.X3ML.Condition;
import static eu.delving.x3ml.engine.X3ML.GeneratedValue;
import static eu.delving.x3ml.engine.X3ML.GeneratorElement;
import static eu.delving.x3ml.engine.X3ML.SourceType;
import gr.forth.ics.isl.x3ml_reverse_utils.AssociationTable;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import static org.joox.JOOX.$;
import org.w3c.dom.Attr;

/**
 * This abstract class is above Domain, Path, and Range and carries most of
 * their contextual information.
 *
 * @author Gerald de Jong &lt;gerald@delving.eu&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 */
public abstract class GeneratorContext {

    public final Root.Context context;
    public final GeneratorContext parent;
    public final Node node;
    public final int index;
    
    protected GeneratorContext(Root.Context context, GeneratorContext parent, Node node, int index) {
        this.context = context;
        this.parent = parent;
        this.node = node;
        this.index = index;
    }

    /**Retrieves the value that has been generated for the given variable in the given scope.
     * The scope can be either within the same mapping (WITHIN_MAPPING), or global (GLOBAL).
     * 
     * @param variable the name of the variable
     * @param scope the scope (either WITHIN_MAPPING or GLOBAL)
     * @return the generated value for the given variable */
    public GeneratedValue get(String variable, VariableScope scope) {
        if (parent == null) {
            throw exception("Parent context missing");
        }
        return parent.get(variable, scope);
    }

    /**Stores the value that has been generated for the given variable in the given scope.
     * The scope can be either within the same mapping (WITHIN_MAPPING), or global (GLOBAL).
     * 
     * @param variable the name of the variable
     * @param scope the scope (either WITHIN_MAPPING or GLOBAL) 
     * @param generatedValue the generated value */
    public void put(String variable, VariableScope scope, GeneratedValue generatedValue) {
        if (parent == null) {
            throw exception("Parent context missing");
        }
        parent.put(variable, scope, generatedValue);
    }

    public String evaluate(String expression) {
        return context.input().valueAt(node, expression);
    }

    /** Creates or retrieves a value for the node of the input (which is part of this instance). 
     * To this end it uses the given generator and variables declaration. The procedure is the following:
     * - If a global variable has been declared for the entity then it searches if a value has already been 
     * generated for this global variable. If not it generates the value.
     * - If a type-aware variable has been declared for the entity then it searches if a value has already been 
     * generated for this global variable. If not it generates the value.
     * - If a variable has been declared for the entity then it searches if a value has already been 
     * generated for this global variable. If not it generates the value.
     * - It generates (or retrieves) the value for the particular node
     * 
     * @param generator the declared generator element
     * @param globalVariable the name of the declared global variable
     * @param variable the name of the declared variable
     * @param typeAwareVar the name of the type-aware variable
     * @param unique a unique value (usually the type of additional/intermediates) for creating always new instances
     * @return the value that has been generated (either now, or previously )  */
    public GeneratedValue getInstance(final GeneratorElement generator, String globalVariable, String variable, String typeAwareVar, String unique) {
        if(generator == null){
            throw exception("Value generator missing");
        }
        GeneratedValue generatedValue;
        if(globalVariable != null){
            generatedValue = get(globalVariable, VariableScope.GLOBAL);
            if (generatedValue == null) {
                generatedValue = context.policy().generate(generator, new Generator.ArgValues() {
                    @Override
                    public ArgValue getArgValue(String name, SourceType sourceType, boolean mergeMultipleValues) {
                        return context.input().evaluateArgument(node, index, generator, name, sourceType, mergeMultipleValues);
                    }
                });
                put(globalVariable, VariableScope.GLOBAL, generatedValue);
            }
        }
        else if(typeAwareVar != null){
            if(variable!=null){
                generatedValue = get(variable, VariableScope.WITHIN_MAPPING);
                if (generatedValue == null) {
                    generatedValue = context.policy().generate(generator, new Generator.ArgValues() {
                        @Override
                        public ArgValue getArgValue(String name, SourceType sourceType, boolean mergeMultipleValues) {
                            return context.input().evaluateArgument(node, index, generator, name, sourceType, mergeMultipleValues);
                        }
                    });
                    put(variable,VariableScope.WITHIN_MAPPING, generatedValue);
                    context.putGeneratedValue(extractXPath(node) + unique+"-"+typeAwareVar, generatedValue);
                    this.createAssociationTable(generatedValue, null, extractAssocTableXPath(node));
                }
            }else{
//                String nodeName = extractXPath(node) + unique+"-"+typeAwareVar;
                String nodeName = extractXPath(Domain.domainNode) + unique+"-"+typeAwareVar;
                String xpathProper=extractAssocTableXPath(node);
                generatedValue = context.getGeneratedValue(nodeName);
                if (generatedValue == null) {
                    generatedValue = context.policy().generate(generator, new Generator.ArgValues() {
                        @Override
                        public ArgValue getArgValue(String name, SourceType sourceType, boolean mergeMultipleValues) {
                            return context.input().evaluateArgument(node, index, generator, name, sourceType, mergeMultipleValues);
                        }
                    });
                    GeneratedValue genArg=null;
                    if(generator.getName().equalsIgnoreCase("Literal")){
                        genArg = context.policy().generate(generator, new Generator.ArgValues() {
                            @Override
                            public ArgValue getArgValue(String name, SourceType sourceType, boolean mergeMultipleValues) {
                                return context.input().evaluateArgument2(node, index, generator, name, sourceType);

                            }
                        });
                    }
                    context.putGeneratedValue(nodeName, generatedValue);
                    this.createAssociationTable(generatedValue, genArg, xpathProper);
                }
            }
        }
        else{
            if(variable != null){
                generatedValue = get(variable, VariableScope.WITHIN_MAPPING);
                if (generatedValue == null) {
                    generatedValue = context.policy().generate(generator, new Generator.ArgValues() {
                        @Override
                        public ArgValue getArgValue(String name, SourceType sourceType, boolean mergeMultipleValues) {
                            return context.input().evaluateArgument(node, index, generator, name, sourceType, mergeMultipleValues);
                        }
                    });
                    /* After generating the value for the entity that has a variable associated with it, 
                    we have to also add the generated value (so that the value can be re-used when the same 
                    input is exploited). Related issue= #66 */
                    put(variable, VariableScope.WITHIN_MAPPING, generatedValue);
                    String nodeName = extractXPath(node) + unique;
                    context.putGeneratedValue(nodeName, generatedValue);
                }
            }
            else{
                String nodeName = extractXPath(node) + unique;
                String xpathProper=extractAssocTableXPath(node);
                generatedValue = context.getGeneratedValue(nodeName);
                if (generatedValue == null) {
                    generatedValue = context.policy().generate(generator, new Generator.ArgValues() {
                        @Override
                        public ArgValue getArgValue(String name, SourceType sourceType, boolean mergeMultipleValues) {
                            return context.input().evaluateArgument(node, index, generator, name, sourceType, mergeMultipleValues);
                        }
                    });
                    GeneratedValue genArg=null;
                    if(generator.getName().equalsIgnoreCase("Literal")){
                        genArg = context.policy().generate(generator, new Generator.ArgValues() {
                            @Override
                            public ArgValue getArgValue(String name, SourceType sourceType, boolean mergeMultipleValues) {
                                return context.input().evaluateArgument2(node, index, generator, name, sourceType);

                            }
                        });
                    }
                    context.putGeneratedValue(nodeName, generatedValue);
                    this.createAssociationTable(generatedValue, genArg, xpathProper);
                }
            }
        }
        if (generatedValue == null) {
            throw exception("Empty value produced");
        }
        return generatedValue;
    }

    public boolean conditionFails(Condition condition, GeneratorContext context) {
        return condition != null && condition.failure(context);
    }

    private void createAssociationTable(GeneratedValue generatedValue, GeneratedValue generatedArg, String xpathProper){
        String value="";
        if(generatedValue.type == X3ML.GeneratedType.LITERAL){
            value="\""+generatedValue.text+"\"";
            
            if(generatedArg!=null)
                xpathProper+="/"+generatedArg.text;
            else
                xpathProper+="/text()";
        }
        else if(generatedValue.type == X3ML.GeneratedType.URI)
            value=generatedValue.text;
            if(xpathProper!=null){  //Needs a little more inspection this
                AssociationTable.addEntry(xpathProper,value);
            }
    }
    
    /**Adds a new entry in the association table with the given XPATH expression and 
     * the given key (It is used in the case of joins).
     * 
     * @param xpathEpxr the XPATH expression from one of the tables that are joined
     * @param key the key that is being used for the join */
    public static void appendAssociationTable(String xpathEpxr, String key){
        xpathEpxr=xpathEpxr.replace("///", "/").replaceAll("//", "/");
        AssociationTable.addEntry(xpathEpxr,key);
    }
    
    /** Exports the contents of the association table in XML format.
     * The name of the file is defined from the given parameter.
     * 
     * @param filename the filename where the association table contents will be exported
     * @throws IOException if any error occurs during the exporting.*/
    public static void exportAssociationTable(String filename) throws IOException{
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
        writer.append(AssociationTable.exportAll());
        writer.flush();
        writer.close();
    }
    
    /** Exports the contents of the association table in XML format, and returns their 
     * String representation.
     * 
     * @return a string representation of the association table in XML format.*/
    public static String exportAssociationTableToString(){
        return AssociationTable.exportAll();
    }
    
    public String toString() {
        return extractXPath(node);
    }

    public String toStringAssoc() {
        return extractAssocTableXPath(node);
    }
        
    public static String extractXPath(Node node) {
        if (node == null || node.getNodeType() == Node.DOCUMENT_NODE) {
            return "/";
        } else {
            String soFar="";
            int sibNumber = 0;
            if(node.getNodeType()==Node.ATTRIBUTE_NODE){
                soFar= extractXPath(((Attr)node).getOwnerElement());
            }else{
                soFar = extractXPath(node.getParentNode());

                Node sib = node;
                while (sib.getPreviousSibling() != null) {
                    sib = sib.getPreviousSibling();
                    if (sib.getNodeType() == Node.ELEMENT_NODE) {
                        sibNumber++;
                    }
                }
            }
            return String.format(
                    "%s%s[%d]/",
                    soFar, node.getNodeName(), sibNumber
            );
        }
    }
    
    public static String extractAssocTableXPath(Node node) {
        return $(node).xpath();
    }
}
