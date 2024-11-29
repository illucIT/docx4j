module org.docx4j.export_fo {

	requires org.slf4j;
	requires org.docx4j.core;
	requires org.docx4j.openxml_objects;
	requires org.plutext.jaxb.xslfo;

	//requires fop; // that's an uber/shaded jar, but it lists fop-core etc as deps, so can either use them directly, or use this, but exclude them
	//requires fop.util;
	//requires fop.events;
	// requires qdox;

	requires fop;  // FOP 2.7
	// deps of org.apache.xmlgraphics:fop-core:jar:2.5
		requires batik.anim;
		requires batik.css;
		requires batik.dom;
		requires batik.ext;
		requires batik.parser;
		requires batik.shared.resources;
		requires batik.svg.dom;
		requires batik.util;
		requires batik.constants;
		requires batik.i18n;
//		requires batik.awt.util;
		requires batik.bridge;
//		//requires batik.script;
//		requires batik.extension;
//		requires batik.gvt;
//		requires batik.transcoder;
//		requires batik.svggen;
		requires org.apache.fontbox;
		requires jakarta.xml.bind; // has an Automatic-Module-Name in its MANIFEST.MF
		//requires jai.core;
		//requires jai.codec;
			
	// necessary for FOP 2.9 but not 2.9 or earlier
//	requires fop.core; 
		
// FOP 2.10
//		requires org.apache.xmlgraphics.fop.core;
//		requires org.apache.xmlgraphics.fop.events;
//		requires org.apache.xmlgraphics.fop.util;
//		
//		// deps of org.apache.xmlgraphics:fop-core:jar:2.5
//			requires org.apache.xmlgraphics.batik.anim;
//			requires org.apache.xmlgraphics.batik.css;
//			requires org.apache.xmlgraphics.batik.dom;
//			requires org.apache.xmlgraphics.batik.ext;
//			requires org.apache.xmlgraphics.batik.parser;
////			requires org.apache.xmlgraphics.batik.shared.resources;
////			requires org.apache.xmlgraphics.batik.svg.dom;
//			requires org.apache.xmlgraphics.batik.util;
//			requires org.apache.xmlgraphics.batik.constants;
//			requires org.apache.xmlgraphics.batik.i18n;
		

	
	exports org.docx4j.convert.out.fo;
	exports org.docx4j.convert.out.fo.renderers;
	exports org.docx4j.convert.out.pdf;
	exports org.docx4j.convert.out.pdf.viaXSLFO;
	exports org.docx4j.convert.out.XSLFO;
	
	// The package org.apache.fop.configuration is accessible from more than one module: fop, fop.core
	// Since fop duplicates fop-core, so remove fop from pom.
	
	/*
		Error occurred during initialization of boot layer
		java.lang.module.FindException: Unable to derive module descriptor for /home/jharrop/.m2/repository/org/apache/xmlgraphics/batik-script/1.13/batik-script-1.13.jar
		Caused by: java.lang.module.InvalidModuleDescriptorException: Provider class org.apache.batik.bridge.RhinoInterpreterFactory not in module
		
		So comment out batik.script
	*/
	
	/*
		Error occurred during initialization of boot layer
		java.lang.module.ResolutionException: Modules fop.core and fop.events export package org.apache.fop.tools to module error.prone.annotations
		
		Solution to this is to use only a single module, which means using the fop (all) jar.
	*/

    // Resource folders must be open! See https://stackoverflow.com/questions/45166757/loading-classes-and-resources-in-java-9/45173837#45173837  
	opens org.docx4j.convert.out.fo;
	opens org.docx4j.convert.out.fo.renderers;
	
}
