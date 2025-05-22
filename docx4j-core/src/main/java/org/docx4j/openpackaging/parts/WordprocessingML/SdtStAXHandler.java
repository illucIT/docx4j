/**
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

 **/
package org.docx4j.openpackaging.parts.WordprocessingML;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.LocationAwareXMLStreamException;
import org.docx4j.openpackaging.parts.StAXHandlerAbstract;
import org.docx4j.wml.CTSdtCell;
import org.docx4j.wml.CTSdtRow;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * A framework for using StAX to find SDT elements, then using JAXB to manipulate them.
 * 
 * @author jharrop
 * @since 11.5.3
 */
public abstract class SdtStAXHandler extends StAXHandlerAbstract  {
	
	private static Logger log = LoggerFactory.getLogger(SdtStAXHandler.class);		
	public static JAXBContext context = org.docx4j.jaxb.Context.jc;
	
	protected Stack stack = new Stack();
	

	/**
	 * Manipulate the SDT as you wish.
	 * 
	 * @param sdt
	 * @throws Docx4JException
	 */
	protected abstract List<Object> handleSdt(SdtElement sdt) throws Docx4JException;
	
	
	@Override
	public void handle(XMLStreamReader xsr, XMLStreamWriter xmlWriter)
			throws LocationAwareXMLStreamException, XMLStreamException {

		boolean mustMove = true;  			
		while (xsr.hasNext()) {
			
			if (mustMove) {
				xsr.next();
			}
			int eventType = xsr.getEventType();

			if (eventType == XMLStreamReader.START_ELEMENT) {
				String localName = xsr.getLocalName();
		        log.debug("START_ELEMENT " + localName);
				if (xsr.getLocalName().equals("sdt")) {
//		            	log.debug("** found one **");
					Unmarshaller unmarshaller;
					Object o=null;
					try {
						unmarshaller = context.createUnmarshaller();
						// To unmarshall to the correct type of sdt, we need to know context
						if (stack.peek().equals("body")
								|| stack.peek().equals("tc")
								|| stack.peek().equals("txbxContent")
								) {
							o = unmarshaller.unmarshal(xsr, SdtBlock.class);
						} else if (stack.peek().equals("tbl")) {
							o = unmarshaller.unmarshal(xsr, CTSdtRow.class);								
						} else if (stack.peek().equals("tr")) {
							o = unmarshaller.unmarshal(xsr, CTSdtCell.class);								
						} else if (stack.peek().equals("p")) {
							o = unmarshaller.unmarshal(xsr, SdtRun.class);								
						} else {
							log.error("TODO stack.peek() " + stack.peek() );
						}
						// Unmarshalling will be done from this start event to the corresponding end event. 
						// If this method returns successfully, the reader will be pointing at the token right after the end event.
													
					} catch (JAXBException e) {
						throw new XMLStreamException(e.getMessage(), e);
					}
					o = XmlUtils.unwrap(o);
					log.debug(o.getClass().getName());   
					if (o instanceof SdtElement) {
						SdtElement sdt = (SdtElement)o;
						List<Object> results; 						
						try {
							results = handleSdt(sdt);
						} catch (Docx4JException e1) {
							throw new XMLStreamException(e1.getMessage(), e1);
						}
						// write results
						try {
							Marshaller m = context.createMarshaller();
							m.setProperty(Marshaller.JAXB_FRAGMENT,true);
							for(Object oo : results) {
								m.marshal(oo, xmlWriter);
							}
						} catch (JAXBException e) {
							throw new XMLStreamException(e);
						}
						mustMove = false; // don't do this in this case, since JAXB will have done it.
					} else {
						// Shouldn't happen
						log.error("Unexpected " + o.getClass().getName());							
					}
				} else /* not an sdt */ {
					this.write(xsr,xmlWriter);
					mustMove = true; // JAXB not involved
					stack.push(localName);  // we don't do this for an sdt, because we can't pop, since JAXB has already moved the reader to the next token
				}
			} else if (eventType == XMLStreamReader.END_ELEMENT) {
				stack.pop();
				this.write(xsr,xmlWriter);
				mustMove = true;
				
			} else  {
				this.write(xsr,xmlWriter);
				mustMove = true;
			}

		}
	}
	
	

	@Override
	public void handleCharacters(XMLStreamReader xmlr, XMLStreamWriter writer) throws XMLStreamException {

		StringBuilder sb = new StringBuilder();
		sb.append(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength());
		
		writer.writeCharacters(sb.toString() );
		
	}

    public static class Stack {
    	
        static ArrayList<String> list = new ArrayList<String>();

        public static boolean isEmpty() {
            return (list.size() == 0);
        }

        public static void push(String data) {
            list.add(data);
        }

        public static String pop() {
            if (isEmpty() == true)
                return null;
            return list.remove(list.size() - 1);
        }

        public static String peek() {
            if (isEmpty())
                return null;
            return list.get(list.size() - 1);
        }
    }	
}