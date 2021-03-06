//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.10.23 at 04:10:19 PM EST 
//


package de.epml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for typeGraphicsDefault complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="typeGraphicsDefault">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fill" type="{http://www.epml.de}typeFill" minOccurs="0"/>
 *         &lt;element name="line" type="{http://www.epml.de}typeLine" minOccurs="0"/>
 *         &lt;element name="font" type="{http://www.epml.de}typeFont" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "typeGraphicsDefault", propOrder = {
    "fill",
    "line",
    "font"
})
public class TypeGraphicsDefault {

    protected TypeFill fill;
    protected TypeLine line;
    protected TypeFont font;

    /**
     * Gets the value of the fill property.
     * 
     * @return
     *     possible object is
     *     {@link TypeFill }
     *     
     */
    public TypeFill getFill() {
        return fill;
    }

    /**
     * Sets the value of the fill property.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeFill }
     *     
     */
    public void setFill(TypeFill value) {
        this.fill = value;
    }

    /**
     * Gets the value of the line property.
     * 
     * @return
     *     possible object is
     *     {@link TypeLine }
     *     
     */
    public TypeLine getLine() {
        return line;
    }

    /**
     * Sets the value of the line property.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeLine }
     *     
     */
    public void setLine(TypeLine value) {
        this.line = value;
    }

    /**
     * Gets the value of the font property.
     * 
     * @return
     *     possible object is
     *     {@link TypeFont }
     *     
     */
    public TypeFont getFont() {
        return font;
    }

    /**
     * Sets the value of the font property.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeFont }
     *     
     */
    public void setFont(TypeFont value) {
        this.font = value;
    }

}
