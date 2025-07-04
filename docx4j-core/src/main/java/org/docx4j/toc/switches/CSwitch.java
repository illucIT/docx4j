/*
 *  Copyright 2013-2016, Plutext Pty Ltd.
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
package org.docx4j.toc.switches;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.NotImplementedException;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.finders.InstrTextFinder;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.model.fields.SimpleFieldLocator;
import org.docx4j.toc.TocEntry;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.P;
import org.docx4j.wml.Style;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * \c field-argument
 * 
 * Includes figures, tables, charts, and other items that are numbered 
 * by a SEQ field (§2.16.5.63). 
 * 
 * The sequence identifier designated by text in this switch's field-argument, 
 * which corresponds to the caption label, shall match the identifier 
 * in the corresponding SEQ field.
 * 
 * @since 11.5.4
 */
public class CSwitch extends SelectorSwitch {
	
	// eg  TOC \h \z \c "Figure"
	
	private static Logger log = LoggerFactory.getLogger(CSwitch.class);					
	

    public static final String ID = "\\c";
    private static final int PRIORITY = 7;
    
    /**
     * identifier is the name assigned to the series of items that are to be numbered. 
     * [Example: identifier might be Equation, Figure, Table, or Thing, as the user deems appropriate for a caption. end example] 
     * 
     * identifier shall start with a Latin letter and shall consist of no more than 40 Latin letters, Arabic digits, and underscores. 
     * 
     * (See the TOC field (§2.16.5.75) switches \c and \s for uses of identifier.)
     */
    private String itemIdentifier;
    
    public String getItemIdentifier() {
		return itemIdentifier;
	}


	@Override
    public String parseFieldArgument(String fieldArgument){
        this.fieldArgument = fieldArgument;
        itemIdentifier = prepareArgument(fieldArgument);
        return EMPTY;
    }
    
    
    @Override
    public boolean hasFieldArgument() {
        return true;
    }

    @Override
    public void process(Style s, SwitchProcessorInterface sp) {

    	throw new NotImplementedException("For ToC C swtich, don't use this method.");
    	
    }

    public void process(P p, SwitchProcessorInterface sp) {
    	
		// Does the paragraph contain  <w:fldSimple w:instr=" SEQ Figure \* ARABIC ">
		// or equivalent complex field:  <w:instrText xml:space="preserve"> SEQ Figure \* ARABIC </w:instrText>
    	// JAXBElement<Text>(_RInstrText_QNAME, Text.class, R.class, value)
    	
    	// Simple fields
		SimpleFieldLocator fl = new SimpleFieldLocator();
		new TraversalUtil(p, fl);
		
		for( CTSimpleField simpleField : fl.simpleFields ) {
			
			//System.out.println(XmlUtils.marshaltoString(simpleField, true, true));
//			System.out.println(simpleField.getInstr());
			String fldSimpleName = FormattingSwitchHelper.getFldSimpleName(simpleField.getInstr());
			if ("SEQ".equals(fldSimpleName)) {
				FldSimpleModel fsm = new FldSimpleModel(); //gets reused
				try {
					fsm.build(simpleField.getInstr());
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String key = fsm.getFldParameters().get(0);  
				if (log.isDebugEnabled()) {
					log.debug(simpleField.getInstr() + " ----> " + key);
				}
    			
    			// if key matches \c item identifier
    			if (key.equals(this.getItemIdentifier())) {
    				
    		        TocEntry te = sp.getEntry(); // creates it       	
    		        te.setEntryLevel(1); // ??
    		    	sp.setSelected(true);  // important 
    		    	return;
    			}
			}
		
		}
    	
    	// Complex fields
		InstrTextFinder complexFinder = new InstrTextFinder(); 
		new TraversalUtil(p, complexFinder);
		for (Object o : complexFinder.results) {
			Text t = (Text)XmlUtils.unwrap(o);
			String key = t.getValue();
			// if key matches \c item identifier
			if (key.contains("SEQ") && key.contains(this.getItemIdentifier())) {
				
		        TocEntry te = sp.getEntry(); // creates it       	
		        te.setEntryLevel(1); // ??
		    	sp.setSelected(true);  // important 
		    	return;
			}			
		}
		log.debug("P doesn't contain SEQ");
    	
    }
    
    
	@Override
	public int getPriority() {
		return PRIORITY;
	}

}
