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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.docx4j.TraversalUtil;
import org.docx4j.finders.RangeFinder;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.toc.StyleBasedOnHelper;
import org.docx4j.toc.TocEntry;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.PStyle;
import org.docx4j.wml.STTabTlc;
import org.docx4j.wml.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBElement;

public class SwitchProcessor implements SwitchProcessorInterface {
	
	private static Logger log = LoggerFactory.getLogger(SwitchProcessor.class);				

    protected PropertyResolver propertyResolver;
    public PropertyResolver getPropertyResolver() {
		return propertyResolver;
	}

	protected StyleBasedOnHelper styleBasedOnHelper;
    public StyleBasedOnHelper getStyleBasedOnHelper() {
		return styleBasedOnHelper;
	}

	private PageDimensions pageDimensions;

    private static final String TOC_PREFIX = "_Toc";
    private static final int MILLION = 1000000;
    
    private TocEntry entry = null;
    private STTabTlc leader;
    
    private boolean selected = false;
    public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
    
    private boolean pageNumbers = true;
        
//    public SwitchProcessor(PageDimensions pageDimensions) {
//    
//    	this.pageDimensions = pageDimensions;
//    }
    
    public SwitchProcessor(PageDimensions pageDimensions, STTabTlc leader) {
        
    	this.pageDimensions = pageDimensions;
    	this.leader = leader;
    }
    
    /**
     * Return a list consisting of those p in pList which are determined to
     * form part of the ToC.
     * 
     * @param wordMLPackage
     * @param pList
     * @param switchesList
     * @param pNumbersMap
     * @return
     */
    public List<TocEntry> processSwitches(WordprocessingMLPackage wordMLPackage, List<P> pList, 
    		List<SwitchInterface> switchesList,
    		Map<P, ResultTriple> pNumbersMap){
    	
        List<TocEntry> tocEntries = new ArrayList<TocEntry>();
        
        PPr ppr;
        PStyle pStyle;
        Style s;
        
        propertyResolver = wordMLPackage.getMainDocumentPart().getPropertyResolver();
        styleBasedOnHelper = new StyleBasedOnHelper(propertyResolver);

        int randomSeed = (int)(Math.random()*MILLION);
        AtomicInteger seedIndex = getBookmarkId(wordMLPackage);
        
        String anchorValue = "";
        for(P p: pList){

            ppr = p.getPPr();
            if(ppr == null){ // TODO FIXME: Normal could be required in TOC
                continue;
            }
            pStyle = ppr.getPStyle();
            if(pStyle == null){
            	// TODO FIXME an outline level may be enough for this p to appear in the TOC?
            	log.debug("P has no style; ignoring..");
                continue;
            }
            
            /*
             * Could redo this.
             * 
             * Keep a cache of known TOC'd styles.
             * (If we don't, consider adding caches in T and O switches)
             * 
             * Only U can cause exclusion, so handle this first:
             * If one of the switches is U, check pPr for outline level,
             * and set !proceed if appropriate.
             * 
             * Otherwise, check the cache for this style.
             * If not cached, work it out, and cache the result.
             */
            
            s = propertyResolver.getStyle(pStyle.getVal());
            log.debug("Processing switches for P with stylename " + pStyle.getVal() );
            if (s==null) {
            	log.warn("Style " + pStyle.getVal() + " is not defined in styles part!");
            	continue; 
            	/*
            	 * TODO It would be possible to do some switch processing even though
            	 * the style is not defined, since we could base some processing
            	 * on the stylename.  However doing this would require passing
            	 * stylename into the process method so we could fall back to it
            	 * when s==null   
            	 */            	
            }
            
            OSwitch oSwitch = null;
            for(SwitchInterface sw: switchesList){
            	if (sw instanceof OSwitch) {
            		oSwitch = (OSwitch)sw;
            	}
            }
            
            for(SwitchInterface sw: switchesList){
            	log.debug(sw.getClass().getName());
            	
            	if (sw instanceof CSwitch) {

            		// C switch is sensitive to paragraph content containing "SEQ" field.
            		((CSwitch)sw).process(p, this);
            		
            		if (((CSwitch)sw).isDetected()) {
            			// P contains SEQ, so return a P where that is converted to sequence number
            			p = ((CSwitch)sw).postprocess(p, wordMLPackage);
            		}
            		
            	} else if (sw instanceof USwitch) {            		
            		// Need pPr
            		((USwitch)sw).process(s, this, ppr, oSwitch);
            	} else {
            		sw.process(s, this);
            	}
            	                
            }
            
            
            if (selected){
            	
                // create the visible entry text
            	entry.setEntryValue(p);  // C, O, T, and U switches instantiate entry. 
            	
            	// number the paragraph, if necessary
            	entry.numberEntry(pNumbersMap.get(p));	
                
                // If the p is empty, omit it UNLESS it is numbered,
                // in which case Word includes it.
                if (entry.getEntryValue()==null
                		|| entry.getEntryValue().size()==0) { 
                	
                	// TODO FIXME should exclude:
                    /* <w:r>
                         <w:t></w:t>
                       </w:r> 
                       */                	
                	
                    log.debug("Not adding p to toc since it is empty.  (stylename " + pStyle.getVal() );
                
                } else {
                	
                    log.debug("Adding p to toc with stylename " + pStyle.getVal() );

                    anchorValue = TOC_PREFIX + randomSeed + seedIndex;
                    bookmarkStyleText(p, anchorValue, seedIndex, entry);
                	
	                entry.setAnchorValue(anchorValue);
	                tocEntries.add(entry);
	                //seedIndex++;                
                }
            } 
            //set default for new iteration
            entry = null;
            selected = false;  // default to exclude
        }

        return tocEntries;
    }

    private void bookmarkStyleText(P p, String anchorValue, AtomicInteger id, TocEntry te){
        CTBookmark bookmark = null;
//        R r = null;
//        Text t;
        
        for(Object o : p.getContent()){
            if(o instanceof CTBookmark){
                bookmark = (CTBookmark)o;
                break;
            } 
        }
//            else if(o instanceof R){
//                r = (R)o;
//                Object o2= XmlUtils.unwrap(r.getContent().get(0));
//                if (o2 instanceof Text) {
//                    t = (Text)o2;
////                            if(t == null){
////                                return false;
////                            }                	
//                    entryValue = t.getValue();
//                } else {
//                	log.warn("Unexpected entry in run " + o2.getClass().getName());
//                	return false;
//                }
//            }
//        }
//
//        if(r == null){
//            return false;
//        }

        if(bookmark == null){
            bookmarkP(p, anchorValue, id.getAndIncrement());
        } else {
            bookmark.setId(BigInteger.valueOf(id.getAndIncrement()));
            bookmark.setName(anchorValue);
        }
        
    }

    
	/**
	 * Surround the specified r in the specified p
	 * with a bookmark (with specified name and id)
	 * @param p
	 * @param r
	 * @param name
	 * @param id
	 */
	public void bookmarkP(P p, String name, int id) {
		
		// Enhancement: If the P was already bookmarked,
		// it would be nice to be able to just re-use 
		// the existing bookmark here.
		
//		// Find the index
//		int index = p.getContent().indexOf(r);
//	
//		if (index<0) {
//			System.out.println("P does not contain R!");
//			return;
//		}
		
		ObjectFactory factory = Context.getWmlObjectFactory();
		BigInteger ID = BigInteger.valueOf(id);
		
		
		// Add bookmark end first
		CTMarkupRange mr = factory.createCTMarkupRange();
		mr.setId(ID);
		JAXBElement<CTMarkupRange> bmEnd = factory.createBodyBookmarkEnd(mr);
		p.getContent().add(bmEnd); 
		
		// Next, bookmark start
		CTBookmark bm = factory.createCTBookmark();
		bm.setId(ID);
		bm.setName(name);		
		JAXBElement<CTBookmark> bmStart =  factory.createBodyBookmarkStart(bm);
		p.getContent().add(0, bmStart);
		
	}
    
    
    public void setPageNumbers(boolean pageNumbers){
        this.pageNumbers = pageNumbers;
    }

    public boolean pageNumbers(){
        return pageNumbers;
    }

    public TocEntry getEntry(){
        if(entry == null){
            entry = new TocEntry(propertyResolver, pageDimensions, leader);
        }
        return entry;
    }
    
//    public void proceed(boolean proceed){
//        this.proceed = proceed;
//    }

    
	private AtomicInteger bookmarkId = null;
	
	/**
	 * Provide a way to set the starting bookmark ID number.
	 * 
	 * For efficiency, user code needs to pass this value through.
	 * 
	 * If it isn't, the value will be calculated (less efficient).
	 *   
	 * @param bookmarkId
	 * @since 3.3.0
	 */
	public void setStartingIdForNewBookmarks(AtomicInteger bookmarkId) {
		this.bookmarkId = bookmarkId;
		
	}	
	
	protected AtomicInteger getBookmarkId(WordprocessingMLPackage wordMLPackage) {
		
		if (bookmarkId==null) {
			// Work out starting ID
			bookmarkId = new AtomicInteger(initBookmarkIdStart(wordMLPackage));
		}
		return bookmarkId;
	}

	private int initBookmarkIdStart(WordprocessingMLPackage wordMLPackage) {

		int highestId = 0;
		
		RangeFinder rt = new RangeFinder();
		new TraversalUtil(wordMLPackage.getMainDocumentPart().getContent(), rt);
		
		for (CTBookmark bm : rt.getStarts()) {
			
			BigInteger id = bm.getId();
			if (id!=null && id.intValue()>highestId) {
				highestId = id.intValue();
			}
		}
		return highestId +1;
	}	    
}
