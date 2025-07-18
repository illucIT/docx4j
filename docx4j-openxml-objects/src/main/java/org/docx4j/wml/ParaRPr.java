/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml; 

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ParaRPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ParaRPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_ParaRPrTrackChanges" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_RPrBase" minOccurs="0"/>
 *         &lt;element name="rPrChange" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_ParaRPrChange" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ParaRPr", propOrder = {
    "ins",
    "del",
    "moveFrom",
    "moveTo",
    
    "rStyle",
    "rFonts",
    "b",
    "bCs",
    "i",
    "iCs",
    "caps",    
    "smallCaps",
    "strike",
    "dstrike",
    "outline",
    "shadow",
    "emboss",
    "imprint",
    "noProof",
    "snapToGrid",
    "vanish",
    "webHidden",
    "color",
    "spacing",
    "w",
    "kern",
    "position",
    "sz",
    "szCs",
    "highlight",
    "u",
    "effect",
    "bdr",
    "shd",
    "fitText",
    "vertAlign",
    "rtl",
    "cs",
    "em",
    "lang",
    "eastAsianLayout",
    "specVanish",
    "oMath",
    "glow",
    "shadow14",
    "reflection",
    "textOutline",
    "textFill",
    "scene3D",
    "props3D", 
    "ligatures",
    "numForm",
    "numSpacing",
    "stylisticSets",
    "cntxtAlts",    
    "rPrChange"
})
public class ParaRPr extends RPrAbstract implements Child
{

    protected CTTrackChange ins;
    protected CTTrackChange del;
    protected CTTrackChange moveFrom;
    protected CTTrackChange moveTo;
    
    protected ParaRPrChange rPrChange;
	
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the ins property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackChange }
     *     
     */
    public CTTrackChange getIns() {
        return ins;
    }

    /**
     * Sets the value of the ins property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackChange }
     *     
     */
    public void setIns(CTTrackChange value) {
        this.ins = value;
    }

    /**
     * Gets the value of the del property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackChange }
     *     
     */
    public CTTrackChange getDel() {
        return del;
    }

    /**
     * Sets the value of the del property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackChange }
     *     
     */
    public void setDel(CTTrackChange value) {
        this.del = value;
    }

    /**
     * Gets the value of the moveFrom property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackChange }
     *     
     */
    public CTTrackChange getMoveFrom() {
        return moveFrom;
    }

    /**
     * Sets the value of the moveFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackChange }
     *     
     */
    public void setMoveFrom(CTTrackChange value) {
        this.moveFrom = value;
    }

    /**
     * Gets the value of the moveTo property.
     * 
     * @return
     *     possible object is
     *     {@link CTTrackChange }
     *     
     */
    public CTTrackChange getMoveTo() {
        return moveTo;
    }

    /**
     * Sets the value of the moveTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTrackChange }
     *     
     */
    public void setMoveTo(CTTrackChange value) {
        this.moveTo = value;
    }

    /**
     * Gets the value of the rPrChange property.
     * 
     * @return
     *     possible object is
     *     {@link CTRPrChange }
     *     
     */
    public ParaRPrChange getRPrChange() {
        return rPrChange;
    }

    /**
     * Sets the value of the rPrChange property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRPrChange }
     *     
     */
    public void setRPrChange(ParaRPrChange value) {
        this.rPrChange = value;
    }
    

    /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     * 
     * @return
     *     The parent object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
     * 
     * @param parent
     *     The parent object in the object tree.
     * @param unmarshaller
     *     The unmarshaller that generated the instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        setParent(parent);
    }

}
