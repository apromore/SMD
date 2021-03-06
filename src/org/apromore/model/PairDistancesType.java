//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.10.23 at 04:10:32 PM EST 
//


package org.apromore.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 * <p>Java class for PairDistancesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PairDistancesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PiarDistance" type="{urn:qut-edu-au:schema:apromore:manager}PiarDistanceType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PairDistancesType", propOrder = {
    "piarDistance"
})
public class PairDistancesType
    implements Equals, HashCode, ToString
{

    @XmlElement(name = "PiarDistance")
    protected List<PiarDistanceType> piarDistance;

    /**
     * Gets the value of the piarDistance property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the piarDistance property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPiarDistance().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PiarDistanceType }
     * 
     * 
     */
    public List<PiarDistanceType> getPiarDistance() {
        if (piarDistance == null) {
            piarDistance = new ArrayList<PiarDistanceType>();
        }
        return this.piarDistance;
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
            List<PiarDistanceType> thePiarDistance;
            thePiarDistance = this.getPiarDistance();
            strategy.appendField(locator, this, "piarDistance", buffer, thePiarDistance);
        }
        return buffer;
    }

    public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy strategy) {
        if (!(object instanceof PairDistancesType)) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final PairDistancesType that = ((PairDistancesType) object);
        {
            List<PiarDistanceType> lhsPiarDistance;
            lhsPiarDistance = this.getPiarDistance();
            List<PiarDistanceType> rhsPiarDistance;
            rhsPiarDistance = that.getPiarDistance();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "piarDistance", lhsPiarDistance), LocatorUtils.property(thatLocator, "piarDistance", rhsPiarDistance), lhsPiarDistance, rhsPiarDistance)) {
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
            List<PiarDistanceType> thePiarDistance;
            thePiarDistance = this.getPiarDistance();
            currentHashCode = strategy.hashCode(LocatorUtils.property(locator, "piarDistance", thePiarDistance), currentHashCode, thePiarDistance);
        }
        return currentHashCode;
    }

    public int hashCode() {
        final HashCodeStrategy strategy = JAXBHashCodeStrategy.INSTANCE;
        return this.hashCode(null, strategy);
    }

}
