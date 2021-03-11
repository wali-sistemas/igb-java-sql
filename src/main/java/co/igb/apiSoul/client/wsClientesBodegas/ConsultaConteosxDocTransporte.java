
package co.igb.apiSoul.client.wsClientesBodegas;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="pUsuario" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pContrasena" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pDocTransporte" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "pUsuario",
    "pContrasena",
    "pDocTransporte"
})
@XmlRootElement(name = "ConsultaConteosxDocTransporte")
public class ConsultaConteosxDocTransporte {

    protected String pUsuario;
    protected String pContrasena;
    protected String pDocTransporte;

    /**
     * Obtiene el valor de la propiedad pUsuario.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPUsuario() {
        return pUsuario;
    }

    /**
     * Define el valor de la propiedad pUsuario.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPUsuario(String value) {
        this.pUsuario = value;
    }

    /**
     * Obtiene el valor de la propiedad pContrasena.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPContrasena() {
        return pContrasena;
    }

    /**
     * Define el valor de la propiedad pContrasena.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPContrasena(String value) {
        this.pContrasena = value;
    }

    /**
     * Obtiene el valor de la propiedad pDocTransporte.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPDocTransporte() {
        return pDocTransporte;
    }

    /**
     * Define el valor de la propiedad pDocTransporte.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPDocTransporte(String value) {
        this.pDocTransporte = value;
    }

}
