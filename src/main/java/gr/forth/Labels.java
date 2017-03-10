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

/** The class contains the necessary resources, in terms of labels that are used 
 * within mappings. The main rationale is to keep all the labels that are declared here, 
 * and exploited in other classes, for reducing the updates, when changes are required.
 * 
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 */
public class Labels {
    public static final String ASSOC_TABLE="assocTable";
    public static final String BOUND="bound";
    public static final String CONSTANT="Constant";
    public static final String DIFF_TERMS_DELIMITER="diffTermsDelim";
    public static final String FORMAT="format";
    public static final String HTTP="http";
    public static final String INPUT="input";
    public static final String LITERAL="Literal";
    public static final String MERGE_WITH_ASSOCIATION_TABLE="mergeAssocWithRDF";
    public static final String OUTPUT="output";
    public static final String POLICY="policy";
    public static final String PREF_LABEL="prefLabel";
    public static final String PREFIX="prefix";
    public static final String RDF="rdf";
    public static final String RDF_NAMESPACE="http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RDFS="rdfs";
    public static final String RDFS_NAMESPACE="http://www.w3.org/2000/01/rdf-schema#";
    public static final String SAME_MERGING_DELIMITER="<SAME_DELIM>";
    public static final String SAME_TERM_DELIMITER="sameTermsDelim";
    public static final String TEXT="text";
    public static final String URI="URI";
    public static final String URIorUUID="URIorUUID";
    public static final String URN="URN";
    public static final String UUID="UUID";
    public static final String UUID_TEST_SIZE="uuidTestSize";
    public static final String X3ML="x3ml";
    public static final String XML="xml";
    public static final String XML_NAMESPACE="http://www.w3.org/XML/1998/namespace";
}
