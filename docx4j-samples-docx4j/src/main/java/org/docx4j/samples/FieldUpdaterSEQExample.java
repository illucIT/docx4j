package org.docx4j.samples;

import org.docx4j.XmlUtils;
import org.docx4j.model.fields.seq.FieldUpdaterSEQ;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Example showing how to renumber SEQ fields.  These are used
 * to number figures (in Word, References > Captions).
 * 
 * This updates the docx.  If you don't want to do
 * that, apply it to a clone instead.
 */
public class FieldUpdaterSEQExample {

	public static void main(String[] args) throws Docx4JException {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(
				new java.io.File(
						System.getProperty("user.dir") + "/OUT_TocAdd.docx")); 
		
		FieldUpdaterSEQ fu = new FieldUpdaterSEQ(wordMLPackage);
		fu.update();
		
		System.out.println(XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true));
		
        wordMLPackage.save(new java.io.File("OUT_FieldUpdaterSEQExample.docx") );
		
	}
	
}
