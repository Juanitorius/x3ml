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

package gr.forth;

import static eu.delving.x3ml.X3MLEngine.exception;
import eu.delving.x3ml.X3MLGeneratorPolicy.CustomGeneratorException;
import eu.delving.x3ml.X3MLGeneratorPolicy.CustomGenerator;
import java.util.Map;
import java.util.TreeMap;

/** The generator is responsible for constructing values (either URIs, or literals)
 *  by concatenating multiple elements (that have the same tag name). More specifically 
 * the generator defines that the values of a particular element should be used for 
 * generating the value; they are defined in terms of appropriate XPath expressions 
 * (i.e. ELEMENT_A/text()). If there are more than one such elements then then their values 
 * will be concatenated, using a particular delimeter (which can also be specified by the user).
 * The generator requires the following arguments:
 * <ul><li>prefix: It is the prefix that should be used before the merging of the values.
 * It is defined as a constant and can be either the prefix of a URL, any String value, or empty </li>
 * <li> text#: the text argument followed by a number (i.e. text1). The user can add
 * multiple such arguments, by incrementing the number suffix (i.e. text2, text3, etc.). The number 
 * suffixes indicate also the merging execution row. </li>
 * <li> delimiter: for indicating which is the string that will be used as a delimiter 
 * between the merged values</li></ul>
 * 
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 */
public class RemoveTerm implements CustomGenerator{
    private String text;
    private String termToRemove;

    @Override
    public void setArg(String name, String value) throws CustomGeneratorException {
        if(name.equals(Labels.TERM_TO_REMOVE)){
            this.termToRemove=value;
        }else if(name.equals(Labels.TEXT)){
            this.text=value;
        }else{
            throw new CustomGeneratorException("Unrecognized argument name: "+ name);
        }
    }
    
    /** Returns the value of the generator.
     * 
     * @return the value of the given generator
     * @throws CustomGeneratorException if the argument of the generator is missing or null*/
    @Override
    public String getValue() throws CustomGeneratorException {
        if(text.isEmpty()){
            throw new CustomGeneratorException("Missing text arguments");
        }
        else {
            return this.text.replace(termToRemove, "");
        }
    }

    /** Returns the type of the generated value. The generator is responsible for constructing 
     * identifiers, and labels therefore it is expected to return either a URI or a Literal value
     * 
     * @return the type of the generated value (i.e. URI or UUID)
     * @throws CustomGeneratorException if the argument is missing or null */
    @Override
    public String getValueType() throws CustomGeneratorException {
        if(this.text!=null && this.text.startsWith(Labels.HTTP+":")){
            return Labels.URI;
        }else{
            return Labels.LITERAL;
        }
    }

    /** Returns a boolean flag (with value set to false) indicating that this 
     * generator supports merging values from similar elements
     * (elements having the same name). 
     * 
     * @return true*/    
    @Override
    public boolean mergeMultipleValues(){
        return true;
    }
}