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

import org.docx4j.model.PropertyResolver;
import org.docx4j.toc.TocEntry;
import org.docx4j.wml.Style;

/**
 * page Numbers switch
 * 
 * Per http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/TOC.html
 * 
 * Without field-argument, omits page numbers from the table of contents. 
 * 
 * Page numbers are omitted from all levels unless a range of entry levels 
 * is specified by text in this switch's field-argument. 
 * 
 * A range is specified as for \l.
 */
public class NSwitch extends FormattingSwitch {

    public static final String ID = "\\n";
    private static final int PRIORITY = 6;

    @Override
    public boolean hasFieldArgument() {
        return true;
    }

    @Override
    public void process(Style s, SwitchProcessorInterface sp) {
        TocEntry te = sp.getEntry();
        if(fieldArgument == null){
            sp.setPageNumbers(false);
            te.addPageNumber(false);
            return;
        }
        int entryLevel = te.getEntryLevel();
        if(entryLevel != -1){
            if(!pageNumbers()){
                te.addPageNumber(false);
            }
            if(entryLevel >= getStartLevel() && entryLevel <= getEndLevel()){
                te.addPageNumber(false); // ???
            }
        }
    }
    
    @Override
    public String parseFieldArgument(String fieldArgument) {
        this.fieldArgument = fieldArgument;
        if(fieldArgument != null){
            if(getStartLevel() == -1 || getEndLevel() == -1){
                return ERROR_NOT_VALID_HEADING_LEVEL;
            }
        }
        return EMPTY;
    }
    
    public boolean pageNumbers(){
        return fieldArgument != null;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

}
