module docx4j_samples_docx4_export_fo {

	requires org.slf4j;
	requires org.docx4j.core;
	requires org.docx4j.openxml_objects;
	
	requires org.docx4j.export_fo;
	requires fop; // FOP 2.7
	
	// necessary for broken FOP 2.9 but not 2.8 or earlier
//	requires fop.core;
//	requires fop.events;
	
	// necessary for FOP 2.10, but broken
//	requires org.apache.xmlgraphics.fop.core;
//	requires org.apache.xmlgraphics.fop.events;
//	requires org.apache.xmlgraphics.fop.util;
	
	requires jakarta.xml.bind;

	
	//requires docx4j_JAXB_ReferenceImpl;
    
//	requires docx4j_JAXB_MOXy;
//	requires org.eclipse.persistence.moxy;
//	requires org.eclipse.persistence.core;
	
	
}
