package org.docx4j.toc.switches;

import org.docx4j.model.PropertyResolver;
import org.docx4j.toc.StyleBasedOnHelper;
import org.docx4j.toc.TocEntry;

public interface SwitchProcessorInterface {
	
	public TocEntry getEntry();
	
	public void setPageNumbers(boolean pageNumbers);

	public void setSelected(boolean selected);
	public boolean isSelected();

	public StyleBasedOnHelper getStyleBasedOnHelper();
	
	public PropertyResolver getPropertyResolver();
	
}
