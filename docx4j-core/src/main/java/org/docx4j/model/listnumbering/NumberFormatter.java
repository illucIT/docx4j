package org.docx4j.model.listnumbering;

import org.docx4j.wml.NumberFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberFormatter {
	
	protected static Logger log = LoggerFactory.getLogger(NumberFormatter.class);

    /**
     * The current number, formatted using numFmt.
     */
    public static String getCurrentValueFormatted(NumberFormat numFmt, String num)
    {

    	try {
    		return getCurrentValueFormatted(numFmt, Integer.parseInt(num));
    	} catch (NumberFormatException e) {
    		log.error("'" + num + "' is NaN");
    		return "1";
    	}
    } 
    /**
     * The current number, formatted using numFmt.
     */
    public static String getCurrentValueFormatted(NumberFormat numFmt, int current)
    {
    	/*
    	 * If you look at the OpenXML spec or
    	 * STNumberFormat.java, you'll see there are some 60 number formats.
    	 * 
    	 * Of these, we currently aim to support:
    	 * 
		 *     decimal
		 *     upperRoman
		 *     lowerRoman
		 *     upperLetter
		 *     lowerLetter
		 *     bullet
		 *     none
		 *     
		 * What about?
		 *     
		 *     ordinal
		 *     cardinalText
		 *     ordinalText
    	 * 
    	 */
    	
    	if (numFmt.equals( NumberFormat.DECIMAL ) ) {
    		return current+"";
    	}
    	
    	if (numFmt.equals( NumberFormat.NONE ) ) {
    		return "";        		
    	}

    	if (numFmt.equals( NumberFormat.BULLET ) ) {
    		
    		// TODO - revisit how this is handled.
    		// The code elsewhere for handling bullets
    		// overlaps with this numFmt stuff.
    		return "*";        		
    	}
    	        	    	
    	if (numFmt.equals( NumberFormat.UPPER_ROMAN ) ) {        		
    		NumberFormatRomanUpper converter = new NumberFormatRomanUpper(); 
    		return converter.format(current);
    	}
    	if (numFmt.equals( NumberFormat.LOWER_ROMAN ) ) {        		
    		NumberFormatRomanLower converter = new NumberFormatRomanLower(); 
    		return converter.format(current);
    	}
    	if (numFmt.equals( NumberFormat.LOWER_LETTER ) ) {        		
    		NumberFormatLowerLetter converter = new NumberFormatLowerLetter(); 
    		return converter.format(current);
    	}
    	if (numFmt.equals( NumberFormat.UPPER_LETTER ) ) {        		
    		NumberFormatLowerLetter converter = new NumberFormatLowerLetter(); 
    		return converter.format(current).toUpperCase();
    	}        	
    	if (numFmt.equals( NumberFormat.DECIMAL_ZERO ) ) {        		
    		NumberFormatDecimalZero converter = new NumberFormatDecimalZero(); 
    		return converter.format(current);
    	}
        // These two are the same in Chinese
        // no need to be processed separately
    	if (numFmt.equals( NumberFormat.CHINESE_COUNTING ) ||
                numFmt.equals( NumberFormat.CHINESE_COUNTING_THOUSAND ) ) {
    		NumberFormatChineseLower converter = new NumberFormatChineseLower();
    		return converter.format(current);
    	}
        // This one means use upper Chinese number characters
        if (numFmt.equals( NumberFormat.CHINESE_LEGAL_SIMPLIFIED ) ) {
            NumberFormatChineseUpper converter = new NumberFormatChineseUpper();
            return converter.format(current);
        }
        // These two are the same
        // just to adapt to documents in Chinese
        if (numFmt.equals( NumberFormat.DECIMAL_ENCLOSED_CIRCLE ) ||
                numFmt.equals( NumberFormat.DECIMAL_ENCLOSED_CIRCLE_CHINESE )) {
            NumberFormatDecimalEnclosedCircle converter = new NumberFormatDecimalEnclosedCircle();
            return converter.format(current);
        }
    	
    	log.error("Unhandled numFmt: " + numFmt.name() );
        return current+"";
    }	
}
