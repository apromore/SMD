//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.10.23 at 04:10:32 PM EST 
//


package org.apromore.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.lang.Equals;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy;
import org.jvnet.jaxb2_commons.lang.HashCode;
import org.jvnet.jaxb2_commons.lang.HashCodeStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBHashCodeStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBToStringStrategy;
import org.jvnet.jaxb2_commons.lang.ToString;
import org.jvnet.jaxb2_commons.lang.ToStringStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;


/**
 * <p>Java class for DistanceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DistanceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GED" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *       &lt;attribute name="FragmentId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DistanceType", propOrder = {
    "ged"
})
public class DistanceType
    implements Equals, HashCode, ToString
{

    @XmlElement(name = "GED")
    protected double ged;
    @XmlAttribute(name = "FragmentId")
    protected String fragmentId;

    /**
     * Gets the value of the ged property.
     * 
     */
    public double getGED() {
        return ged;
    }

    /**
     * Sets the value of the ged property.
     * 
     */
    public void setGED(double value) {
        this.ged = value;
    }

    /**
     * Gets the value of the fragmentId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFragmentId() {
        return fragmentId;
    }

    /**
     * Sets the value of the fragmentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFragmentId(String value) {
        this.fragmentId = value;
    }

    public String toString() {
        final ToStringStrategy strategy = JAXBToStringStrategy.INSTANCE;
        final StringBuilder buffer = new StringBuilder();
        append(null, buffer, strategy);
        return buffer.toString();
    }

    public StringBuilder append(ObjectLocator locator, StringBuilder buffer, ToStringStrategy strategy) {
        strategy.appendStart(locator, this, buffer);
        appendFields(locator, buffer, strategy);
        strategy.appendEnd(locator, this, buffer);
        return buffer;
    }

    public StringBuilder appendFields(ObjectLocator locator, StringBuilder buffer, ToStringStrategy strategy) {
        {
            double theGED;
            theGED = this.getGED();
            strategy.appendField(locator, this, "ged", buffer, theGED);
        }
        {
            String theFragmentId;
            theFragmentId = this.getFragmentId();
            strategy.appendField(locator, this, "fragmentId", buffer, theFragmentId);
        }
        return buffer;
    }

    public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy strategy) {
        if (!(object instanceof DistanceType)) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final DistanceType that = ((DistanceType) object);
        {
            double lhsGED;
            lhsGED = this.getGED();
            double rhsGED;
            rhsGED = that.getGED();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "ged", lhsGED), LocatorUtils.property(thatLocator, "ged", rhsGED), lhsGED, rhsGED)) {
                return false;
            }
        }
        {
            String lhsFragmentId;
            lhsFragmentId = this.getFragmentId();
            String rhsFragmentId;
            rhsFragmentId = that.getFragmentId();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "fragmentId", lhsFragmentId), LocatorUtils.property(thatLocator, "fragmentId", rhsFragmentId), lhsFragmentId, rhsFragmentId)) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object object) {
        final EqualsStrategy strategy = JAXBEqualsStrategy.INSTANCE;
        return equals(null, null, object, strategy);
    }

    public int hashCode(ObjectLocator locator, HashCodeStrategy strategy) {
        int currentHashCode = 1;
        {
            double theGED;
            theGED = this.getGED();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "ged", theGED), currentHashCode, theGED);
        }
        {
            String theFragmentId;
            theFragmentId = this.getFragmentId();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "fragmentId", theFragmentId), currentHashCode, theFragmentId);
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

}