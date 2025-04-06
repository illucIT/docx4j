/*
 *  Copyright 2025, Plutext Pty Ltd.
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

package org.docx4j.samples;


import java.io.File;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.LocationAwareXMLStreamException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.StAXHandlerAbstract;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;


/**
 * 
 * Proof of concept of using StaX to process the MDP,
 * looking for content control elements 
 * (which can then be handled in docx4j in the usual way).
 * 
 * Be sure to use 
 
		<dependency>
			<groupId>com.fasterxml.woodstox</groupId>
			<artifactId>woodstox-core</artifactId>
			<version>7.1.0</version>
		</dependency>
		
		 
 * since com.sun.xml.internal.stream.writers.XMLStreamWriterImpl
 * seems to be a bit broken.
 * 
 * @author jharrop
 *
 */
public class ContentControlsViaStAX extends AbstractSample {

	public static JAXBContext context = org.docx4j.jaxb.Context.jc;

	public static void main(String[] args) throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/databinding/invoice.docx";

		// You'll want
		// docx4j.openpackaging.parts.JaxbXmlPartXPathAware.binder.eager.MainDocumentPart=false
		System.out.println(
				Docx4jProperties.getProperty("docx4j.openpackaging.parts.JaxbXmlPartXPathAware.binder.eager.MainDocumentPart"));
		
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

		// Use StAX; be sure you are using https://github.com/FasterXML/woodstox
		documentPart.pipe(new MyStaXHandler(), null);  // no real need for a filter here, see https://docs.oracle.com/cd/E19575-01/819-3669/bnbgh/index.html

		// prove we changed it
		// System.out.println(XmlUtils.marshaltoString(documentPart.getJaxbElement(), true, true));  // this would unmarshall the whole part
		
		Docx4J.save(wordMLPackage, new File("out.docx"), 0);
		
	}
	
	public static class MyStaXHandler extends StAXHandlerAbstract  {

		@Override
		public void handle(XMLStreamReader xsr, XMLStreamWriter xmlWriter)
				throws LocationAwareXMLStreamException, XMLStreamException {

			while (xsr.hasNext()) {
				
				int eventType = xsr.next();

				if (eventType == XMLStreamReader.START_ELEMENT) {
//			          System.out.println(xsr.getLocalName());
					if (xsr.getLocalName().equals("sdt")) {
//			            	System.out.println("** found one **");
						Unmarshaller unmarshaller;
						Object o;
						try {
							unmarshaller = context.createUnmarshaller();
							o = unmarshaller.unmarshal(xsr);
						} catch (JAXBException e) {
							throw new XMLStreamException(e);
						}
						System.out.println(o.getClass().getName());
						if (o instanceof SdtElement) {
							SdtElement sdt = (SdtElement)o;
							// do something
							SdtPr pr = sdt.getSdtPr();
							if (pr.getDataBinding()!=null) {
								System.out.println(pr.getDataBinding().getXpath());								
								pr.getDataBinding().setXpath("CHANGED");
							}
							// write it
							try {
								Marshaller m = context.createMarshaller();
								m.setProperty(Marshaller.JAXB_FRAGMENT,true);									
								m.marshal(o, xmlWriter);
							} catch (JAXBException e) {
								throw new XMLStreamException(e);
							}
						} else {
							// Shouldn't happen
							System.out.println("Unexpected " + o.getClass().getName());							
						}
					} else {
						this.write(xsr,xmlWriter);
					}
				} else {
					this.write(xsr,xmlWriter);					
				}

			}
		}

		@Override
		public void handleCharacters(XMLStreamReader xmlr, XMLStreamWriter writer) throws XMLStreamException {

			StringBuilder sb = new StringBuilder();
			sb.append(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength());
			
			writer.writeCharacters(sb.toString() );
			
		}

	}
		

}
