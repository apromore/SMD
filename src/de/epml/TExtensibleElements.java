//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.10.23 at 04:10:19 PM EST 
//


package de.epml;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java class for tExtensibleElements complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tExtensibleElements">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="documentation" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *         &lt;element name="toolInfo" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tExtensibleElements", propOrder = {
    "documentation",
    "toolInfo"
})
@XmlSeeAlso({
    TypeEPML.class,
    TypeDirectory.class,
    TypeAttrType.class,
    TypeSpecialization.class,
    TypeCoordinates.class,
    TypeEPC.class,
    TypeArc.class,
    TEpcElement.class,
    TypeDefinition.class
})
public class TExtensibleElements {

    protected Object documentation;
    protected Object toolInfo;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the documentation property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getDocumentation() {
        return documentation;
    }

    /**
     * Sets the value of the documentation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setDocumentation(Object value) {
        this.documentation = value;
    }

    /**
     * Gets the value of the toolInfo property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getToolInfo() {
        return toolInfo;
    }

    /**
     * Sets the value of the toolInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setToolInfo(Object value) {
        this.toolInfo = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
