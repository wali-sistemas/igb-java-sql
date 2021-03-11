package co.igb.persistence.facade;

import co.igb.ejb.IGBApplicationBean;
import co.igb.util.Constants;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jguisao
 */
@Stateless
public class ItemFacade {
    private static final Logger CONSOLE = Logger.getLogger(ItemFacade.class.getSimpleName());
    private static final String DB_TYPE = Constants.DATABASE_TYPE_MSSQL;

    @Inject
    private IGBApplicationBean applicationBean;

    @EJB
    private PersistenceConf persistenceConf;

    public ItemFacade() {

    }

    public String getItemName(String itemCode, String companyName, boolean testing) {
        StringBuilder sb = new StringBuilder();
        sb.append("select cast(itemname as varchar(200)) itemname from oitm where itemcode='");
        sb.append(itemCode);
        sb.append("'");
        try {
            return (String) persistenceConf.chooseSchema(companyName, testing, DB_TYPE).createNativeQuery(sb.toString()).getSingleResult();
        } catch (Exception e) {
            CONSOLE.log(Level.SEVERE, "Ocurrio un error al consultar el nombre del item " + itemCode, e);
            return null;
        }
    }

    public Object[] getItemAttributes(String itemCode, String companyName, boolean testing) {
        StringBuilder sb = new StringBuilder();
        sb.append("select cast(o.itemname as varchar(200)) as itemname, cast(m.Name as varchar(50)) as marca ");
        sb.append("from oitm o ");
        sb.append("left join [@MARCAS] m ON m.Code = o.U_Marca where itemcode = '");
        sb.append(itemCode);
        sb.append("'");

        try {
            return (Object[]) persistenceConf.chooseSchema(companyName, testing, DB_TYPE).createNativeQuery(sb.toString()).getSingleResult();
        } catch (Exception e) {
            CONSOLE.log(Level.SEVERE, "Ocurrio un error al consultar los atributos del item [" + itemCode + "]", e);
            return null;
        }
    }

    public List<Object[]> getItemStock(String itemCode, String binCode, String whsCode, String companyName, boolean pruebas) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT CAST(art.ItemCode AS varchar(20)) AS itemCode, CAST(art.ItemName AS varchar(200)) AS itemName, CAST(ubc.OnHandQty AS INT) AS Qty, ");
        sb.append("       CAST(ubc.WhsCode AS varchar(10)) AS whsCode, CAST(dub.BinCode AS varchar(22)) AS BinCode, ");
        sb.append("       CAST(alm.WhsName AS varchar(30)) AS whsName ");
        sb.append("FROM   OITM art ");
        sb.append("INNER  JOIN OITW sal ON sal.ItemCode = art.ItemCode ");
        sb.append("INNER  JOIN OIBQ ubc ON ubc.ItemCode = sal.ItemCode AND ubc.WhsCode = sal.WhsCode ");
        sb.append("INNER  JOIN OBIN dub ON dub.AbsEntry = ubc.BinAbs ");
        sb.append("INNER  JOIN OWHS alm ON alm.WhsCode = sal.WhsCode ");
        sb.append("WHERE  sal.OnHand > 0 AND ubc.OnHandQty > 0 AND (art.ItemCode = '");
        sb.append(itemCode);
        sb.append("' OR dub.BinCode = '");
        sb.append(binCode);
        sb.append("') AND sal.WhsCode = '");
        sb.append(whsCode);
        sb.append("' ORDER BY dub.BinCode");
        try {
            return (List<Object[]>) persistenceConf.chooseSchema(companyName, pruebas, DB_TYPE).createNativeQuery(sb.toString()).getResultList();
        } catch (Exception e) {
            CONSOLE.log(Level.SEVERE, "Ocurrio un error al consultar el stock del item " + itemCode + ".", e);
            return null;
        }
    }

    public BigDecimal getItemPrice(String itemCode, String companyName, boolean pruebas) {
        StringBuilder sb = new StringBuilder();
        sb.append("Select cast(Price as numeric(19,6)) as price From ITM1 where ItemCode = '");
        sb.append(itemCode);
        sb.append("' and PriceList = ");
        if (companyName.contains("VARROC")) {
            sb.append("1");
        } else {
            sb.append("4");
        }
        try {
            return (BigDecimal) persistenceConf.chooseSchema(companyName, pruebas, DB_TYPE).createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException ex) {
        } catch (Exception e) {
            CONSOLE.log(Level.SEVERE, "Ocurrio un error consultando el precio unitario para el item [" + itemCode + "]");
        }
        return new BigDecimal(0);
    }

    public Object getCheckOutStockItem(String itemCode, String location, String companyName, boolean pruebas) {
        EntityManager em = persistenceConf.chooseSchema(companyName, pruebas, DB_TYPE);
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT CAST(oi.ItemCode AS varchar(20)) AS itemCode, CAST(oi.WhsCode AS varchar(10)) AS whsCode, CAST(oi.OnHandQty AS INT) AS stockLoc ");
        sb.append("FROM   OBIN ob ");
        sb.append("INNER  JOIN OIBQ oi ON ob.AbsEntry = oi.BinAbs ");
        sb.append("WHERE  ob.BinCode = '");
        sb.append(location);
        sb.append("' AND oi.ItemCode = '");
        sb.append(itemCode);
        sb.append("'");
        try {
            return (Object) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (Exception e) {
            CONSOLE.log(Level.SEVERE, "Ocurrio un error al revisar el stock del item [" + itemCode + "] para la ubicacion [" + location + "]");
            return null;
        }
    }
}