/*
 *  Copyright 2011, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package org.docx4j.model.datastorage;

import static org.docx4j.XmlUtils.prepareJAXBResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.util.JAXBResult;

/**
 * Word (2007) can't open a docx if it has more than one
 * comment with the same ID.  Similarly for footnoteReference
 * and endnoteReference.
 * 
 * Bookmarks: duplicate, missing (or half missing) bookmarks
 * don't seem to trouble Word, so we don't check for these.
 * 
 * Since docx4j 3.0, two content control integrity checks are
 * also done here:
 * 
 * 1. w:tr/w:sdt must contain w:tc, and w:tc must be non-empty
 * 
 * 2. w:tc/w:sdt must be non-empty
 * 
 * Note that the w:sdts can be nested, so simple parent/child
 * checks aren't sufficient.
 * 
 * It also removes w15 repeats.
 * 
 * @author jharrop
 *
 */
public class OpenDoPEIntegrity {
	
	private static Logger log = LoggerFactory.getLogger(OpenDoPEIntegrity.class);	
	
	private HashMap<String, String> commentRangeStart;  // value doesn't matter
	private HashMap<String, String> commentRangeEnd;
	private HashMap<String, String> commentReference;
	
	private HashMap<String, String> footnoteReference;
	private HashMap<String, String> endnoteReference;
	
	static Templates xslt;			
	static {
		try {
			Source xsltSource = new StreamSource(
						org.docx4j.utils.ResourceUtils.getResource(
								"org/docx4j/model/datastorage/OpenDoPEIntegrity.xslt"));
			xslt = XmlUtils.getTransformerTemplate(xsltSource);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		
	}

	
	public static void log(String message ) {
		
		log.info(message);
	}

		
		public void process(WordprocessingMLPackage wordMLPackage) throws Docx4JException {
			
			commentRangeStart = new HashMap<String, String>();
			commentRangeEnd = new HashMap<String, String>();
			commentReference = new HashMap<String, String>();
			
			footnoteReference = new HashMap<String, String>();
			endnoteReference = new HashMap<String, String>();
			

			// A component can apply in both the main document part,
			// and in headers/footers. See further
			// http://forums.opendope.org/Support-components-in-headers-footers-tp2964174p2964174.html
			
			process(wordMLPackage.getMainDocumentPart());
	
			// Add headers/footers
			RelationshipsPart rp = wordMLPackage.getMainDocumentPart()
					.getRelationshipsPart();
			for (Relationship r : rp.getRelationships().getRelationship()) {
	
				if (r.getType().equals(Namespaces.HEADER)) {
					process((HeaderPart) rp.getPart(r));
				} else if (r.getType().equals(Namespaces.FOOTER)) {
					process((FooterPart) rp.getPart(r));
				}
			}
		}
	
		private void process(JaxbXmlPart part) throws Docx4JException {
			
			log.info("/n Processing " + part.getPartName().getName() );
			
			org.docx4j.openpackaging.packages.OpcPackage pkg 
				= part.getPackage();		
				// Binding is a concept which applies more broadly
				// than just Word documents.
			
			javax.xml.transform.Source source = null;		
			javax.xml.transform.Result result = null;
			
			// If we're using a StAXSource
			XMLStreamReader xmlReader = null;
//	        XMLStreamWriter xmlWriter = null; 
	        ByteArrayOutputStream baos = null;
	        
	        org.w3c.dom.Document doc = null;
			if ( ((JaxbXmlPart)part).isUnmarshalled() 
					|| /* don't want to use StAX */ !Docx4jProperties.getProperty("docx4j.model.datastorage.BindingHandler.Implementation", "BindingTraverserXSLT").equals("BindingTraverserStAX"))  {
				
				log.debug( ((JaxbXmlPart)part).getPartName().getName() + "; not using StAX.");		
				doc = XmlUtils.marshaltoW3CDomDocument(
					part.getJaxbElement() );
				source = new javax.xml.transform.dom.DOMSource(doc);				
				result = prepareJAXBResult(Context.jc);
				
			} else {
				log.debug( ((JaxbXmlPart)part).getPartName().getName() + " not yet unmarshalled; using StAX.");
				try {
					xmlReader = part.getXMLStreamReader(null);
					source = new StAXSource(xmlReader);
//			        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();            
			        baos = new ByteArrayOutputStream(); 
//					xmlWriter = outputFactory.createXMLStreamWriter(baos, "UTF-8");	
					result = new StreamResult(baos);  // Xalan TransformerImpl doesn't support StAXResult: https://issues.apache.org/jira/browse/XALANJ-2550 
//					result = new StAXResult(xmlWriter);
				} catch (Exception e) {
					throw new Docx4JException(e.getMessage(), e);
				}
			}
						
			Map<String, Object> transformParameters = new HashMap<String, Object>();
			transformParameters.put("OpenDoPEIntegrity", this);			
			
			try {
				
				org.docx4j.XmlUtils.transform(source, xslt, transformParameters, result);
				
				if (result instanceof JAXBResult) {
				
					try {
						part.setJaxbElement(((JAXBResult)result).getResult());
					
						// this will fail if there is unexpected content, 
						// since JaxbValidationEventHandler fails by default
						
					} catch (Exception e) {
	
						log.error(e.getMessage(), e);				
						log.error("Input in question:" + XmlUtils.w3CDomNodeToString(doc));				
						log.error("Now trying DOMResult..");				
						
						result = new DOMResult(); 
						org.docx4j.XmlUtils.transform(doc, xslt, transformParameters, result);
	
						if (log.isDebugEnabled()) {
							
							org.w3c.dom.Document docResult = ((org.w3c.dom.Document)((DOMResult)result).getNode());
							
							//log.debug("After ODI: " + XmlUtils.w3CDomNodeToString(docResult));
							
							Object o = XmlUtils.unmarshal(((org.w3c.dom.Document)((DOMResult)result).getNode()) );
							part.setJaxbElement(o);
						} else 
						{
							//part.unmarshal( ((org.w3c.dom.Document)result.getNode()).getDocumentElement() );
							Object o = XmlUtils.unmarshal(((org.w3c.dom.Document)((DOMResult)result).getNode()) );
							part.setJaxbElement(o);
						}
					}
						
				} else {
			        
			        try {
				        xmlReader.close();
				        baos.flush();
				        if (log.isDebugEnabled() ) {
				        	byte[] bytes = baos.toByteArray();
				        	log.debug(new String(bytes));
				        	((JaxbXmlPart)part).replacePartContent(bytes);
				        } else {
				        	((JaxbXmlPart)part).replacePartContent(baos.toByteArray());
				        }
				        baos.close(); 
					} catch (Exception e) {
						throw new Docx4JException(e.getMessage(), e);				
					}
					
				}					
				
			} catch (Exception e) {
				
				throw new Docx4JException("Problems ensuring integrity", e);			
			}
					
		}

		public static boolean encountered(OpenDoPEIntegrity odIntegrityInstance, String elementName, String id) {
			
			boolean previouslyEncountered = false;
			
			if (elementName.equals("commentRangeStart")) {
			
				previouslyEncountered = odIntegrityInstance.commentRangeStart.containsKey(id);
				if (!previouslyEncountered) {
					odIntegrityInstance.commentRangeStart.put(id, id); 
				}
				return previouslyEncountered; 
			}
			
			if (elementName.equals("commentRangeEnd")) {
				
				previouslyEncountered = odIntegrityInstance.commentRangeEnd.containsKey(id);
				if (!previouslyEncountered) {
					odIntegrityInstance.commentRangeEnd.put(id, id); 
				}
				return previouslyEncountered; 
			}
			
			if (elementName.equals("commentReference")) {
				
				previouslyEncountered = odIntegrityInstance.commentReference.containsKey(id);
				if (!previouslyEncountered) {
					odIntegrityInstance.commentReference.put(id, id); 
				}
				return previouslyEncountered; 
			}
				 
			if (elementName.equals("footnoteReference")) {
				
				previouslyEncountered = odIntegrityInstance.footnoteReference.containsKey(id);
				if (!previouslyEncountered) {
					odIntegrityInstance.footnoteReference.put(id, id); 
				}
				return previouslyEncountered; 
			}
				
			if (elementName.equals("endnoteReference")) {
				
				previouslyEncountered = odIntegrityInstance.endnoteReference.containsKey(id);
				if (!previouslyEncountered) {
					odIntegrityInstance.endnoteReference.put(id, id); 
				}
				return previouslyEncountered; 
			}
			
			log.error("Unexpected elementName: " + elementName);
			return false;
		}

}
