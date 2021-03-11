
package co.igb.apiSoul.client.wsClientesBodegas;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para anonymous complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ReporteConteoResult" type="{http://tempuri.org/}RespReporteConteo" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "reporteConteoResult"
})
@XmlRootElement(name = "ReporteConteoResponse")
public class ReporteConteoResponse {

    @XmlElement(name = "ReporteConteoResult")
    protected RespReporteConteo reporteConteoResult;

    /**
     * Obtiene el valor de la propiedad reporteConteoResult.
     * 
     * @return
     *     possible object is
     *     {@link RespReporteConteo }
     *     
     */
    public RespReporteConteo getReporteConteoResult() {
        return reporteConteoResult;
    }

    /**
     * Define el valor de la propiedad reporteConteoResult.
     * 
     * @param value
     *     allowed object is
     *     {@link RespReporteConteo }
     *     
     */
    public void setReporteConteoResult(RespReporteConteo value) {
        this.reporteConteoResult = value;
    }

}
