package org.docx4j.model.datastorage;

import java.math.BigInteger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.LocationAwareXMLStreamException;
import org.docx4j.openpackaging.parts.StAXHandlerAbstract;
import org.docx4j.openpackaging.parts.WordprocessingML.SdtStAXHandler;
import org.docx4j.wml.CTBookmark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

public class BookmarkHandlerStAX extends StAXHandlerAbstract  {
	
	private static Logger log = LoggerFactory.getLogger(SdtStAXHandler.class);		
	public static JAXBContext context = org.docx4j.jaxb.Context.jc;
	
	private int highestId = 0;
	
	public int getHighestId() {
		return highestId;
	}



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
				if (xsr.getLocalName().equals("bookmarkStart")) {
//		            	log.debug("** found one **");
					Unmarshaller unmarshaller;
					Object o=null;
					try {
						unmarshaller = context.createUnmarshaller();
							o = unmarshaller.unmarshal(xsr, CTBookmark.class);
						// Unmarshalling will be done from this start event to the corresponding end event. 
						// If this method returns successfully, the reader will be pointing at the token right after the end event.
													
					} catch (JAXBException e) {
						throw new XMLStreamException(e.getMessage(), e);
					}
					o = XmlUtils.unwrap(o);
					log.debug(o.getClass().getName());   
					if (o instanceof CTBookmark) {
						CTBookmark bm = (CTBookmark)o;
						BigInteger id = bm.getId();
						if (id!=null && id.intValue()>highestId) {
							highestId = id.intValue();
						}

						// write CTBookmark (unaltered)
						try {
							Marshaller m = context.createMarshaller();
							m.setProperty(Marshaller.JAXB_FRAGMENT,true);
							m.marshal(bm, xmlWriter);
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
				}
			} else if (eventType == XMLStreamReader.END_ELEMENT) {
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
}