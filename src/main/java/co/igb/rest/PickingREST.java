package co.igb.rest;

import co.igb.dto.PickingWarningDTO;
import co.igb.dto.ResponseDTO;
import co.igb.dto.SortedStockDTO;
import co.igb.ejb.IGBApplicationBean;
import co.igb.ejb.SalesOrderEJB;
import co.igb.ejb.StockTransferEJB;
import co.igb.persistence.entity.AssignedOrder;
import co.igb.persistence.entity.PackingOrder;
import co.igb.persistence.entity.PackingOrderItem;
import co.igb.persistence.entity.PackingOrderItemBin;
import co.igb.persistence.facade.*;
import co.igb.util.Constants;

import javax.ejb.EJB;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author dbotero
 */
@Path("picking/v2")
public class PickingREST implements Serializable {
    private static final Logger CONSOLE = Logger.getLogger(PickingREST.class.getSimpleName());

    @EJB
    private SalesOrderFacade soFacade;
    @EJB
    private AssignedOrderFacade aoFacade;
    @EJB
    private PickingRecordFacade prFacade;
    @EJB
    private BinLocationFacade blFacade;
    @EJB
    private PackingOrderFacade poFacade;
    @EJB
    private StockTransferEJB stockTransferEJB;
    @EJB
    private SalesOrderEJB salesOrderEJB;
    @EJB
    private ItemFacade itemFacade;
    @Inject
    private IGBApplicationBean appBean;

    public PickingREST() {
    }

    @DELETE
    @Path("delete-temporary")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response deleteTemporaryRecords(@HeaderParam("X-Company-Name") String companyName,
                                           @HeaderParam("X-Warehouse-Code") String warehouseCode,
                                           @HeaderParam("X-Pruebas") boolean pruebas) {
        CONSOLE.log(Level.INFO, "Ejecutando proceso para eliminar registros de picking temporales. Empresa: {0}, Bodega: {1}",
                new Object[]{companyName, warehouseCode});
        List<Object[]> records = prFacade.findTemporaryRecords(companyName, pruebas);
        CONSOLE.log(Level.INFO, "Se encontraron {0} registros temporales para {1}-{2}",
                new Object[]{records.size(), companyName, warehouseCode});
        Date now = new Date();
        List<Integer> expiredRecords = new ArrayList<>();
        for (Object[] row : records) {
            Integer recordId = (Integer) row[0];
            Date expires = (Date) row[1];
            if (hasExpired(expires, now)) {
                expiredRecords.add(recordId);
            }
        }
        if (!expiredRecords.isEmpty()) {
            CONSOLE.log(Level.INFO, "Eliminando {0} registros vencidos", expiredRecords);
            prFacade.deleteExpiredRecords(expiredRecords, companyName, pruebas);
        }
        return Response.ok(new ResponseDTO(0, expiredRecords)).build();
    }

    private boolean hasExpired(Date expires, Date now) {
        return now.getTime() > expires.getTime();
    }

    private Object[] processPickingStatus(Integer orderNumber, Boolean excludeTemporary, String companyName, boolean pruebas) {
        //consultar los items pickineados
        Map<String, Map<Long, Integer>> pickedItems = prFacade.listPickedItems(orderNumber, excludeTemporary, companyName, pruebas);
        //consultar los items pendientes por entregar de cada orden
        LinkedHashMap<String, Integer> pendingItems = soFacade.listPendingItems(orderNumber, companyName, pruebas);

        //validar que si retorne datos de las dos instancias
        if (pickedItems.equals(null)) {
            CONSOLE.log(Level.WARNING, "Ocurrio un error consultando los items pickineados instancia(MySQL)");
            return null;
        }
        if (pendingItems.equals(null)) {
            CONSOLE.log(Level.WARNING, "Ocurrio un error consultando los items pendientes instancia(SAP)");
            return null;
        }

        //si un item existe en la lista de picking pero no en la de pendientes, no se tiene en cuenta
        for (int i = 0; i < pendingItems.keySet().size(); i++) {
            String itemCode = (String) pendingItems.keySet().toArray()[i];
            if (pickedItems.containsKey(itemCode)) {
                int totalPicked = 0;
                for (Long binAbs : pickedItems.get(itemCode).keySet()) {
                    totalPicked += pickedItems.get(itemCode).get(binAbs);
                }
                Integer pending = pendingItems.get(itemCode);
                if (pending != null && totalPicked >= pending) {
                    pendingItems.remove(itemCode);
                    i--;
                } else if (pending != null) {
                    pendingItems.put(itemCode, pending - totalPicked);
                }
            }
        }

        Set<String> skippedItems = prFacade.listSkippedItems(orderNumber, companyName, pruebas);
        return new Object[]{pendingItems, pickedItems, skippedItems};
    }

    @GET
    @Path("nextitem/{username}")
    @Consumes({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response findNextItemToPick(
            @PathParam("username") String username,
            @QueryParam("orderNumber") Integer orderNumber,
            @HeaderParam("X-Company-Name") String companyName,
            @HeaderParam("X-Warehouse-Code") String warehouseCode,
            @HeaderParam("X-Pruebas") boolean pruebas) {
        CONSOLE.log(Level.INFO, "Buscando siguiente item para packing para el usuario {0} ", username);
        List<PickingWarningDTO> warnings = new ArrayList<>();

        //buscar ordenes asignadas en estado open para el empleado en la empresa seleccionada
        List<AssignedOrder> orders = aoFacade.listOpenAssignationsByUserAndCompany(username, orderNumber, companyName, pruebas);

        //si no hay ordenes pendientes, mostrar alerta con mensaje de error
        if (orders == null || orders.isEmpty()) {
            CONSOLE.log(Level.WARNING, "No se encontraron ordenes de venta asignadas al usuario {0} en la empresa {1}", new Object[]{username, companyName});
            return Response.ok(new ResponseDTO(-2, "El usuario no tiene órdenes de venta asignadas pendientes por picking")).build();
        }

        boolean skippedItems = false;
        TreeSet<SortedStockDTO> sortedStock = new TreeSet<>();
        for (int i = 0; i < orders.size(); i++) {
            AssignedOrder order = orders.get(i);
            CONSOLE.log(Level.INFO, "Procesando nextPickingItem para orden {0}", order.toString());

            Integer orderDocEntry = soFacade.getOrderDocEntry(order.getOrderNumber(), companyName, pruebas);
            Object[] pickingStatus = processPickingStatus(order.getOrderNumber(), false, companyName, pruebas);
            //validar datos consultados de las dos instancias
            if (pickingStatus.equals(null)) {
                return Response.ok(new ResponseDTO(-1, "Ocurrio un error consultandos datos de alguna de las instancias(SAP-MySQL).")).build();
            }

            LinkedHashMap<String, Integer> pendingItems = (LinkedHashMap<String, Integer>) pickingStatus[0];
            HashMap<String, Map<Long, Integer>> pickedItems = (HashMap<String, Map<Long, Integer>>) pickingStatus[1];
            TreeSet<String> skipped = (TreeSet<String>) pickingStatus[2];

            if (pendingItems == null || pendingItems.isEmpty()) {
                CONSOLE.log(Level.WARNING, "La orden {0} no tiene items pendientes por despachar y se marca como cerrada. ", order.getOrderNumber());
                try {
                    closeAndPack(order, pickedItems, warehouseCode, companyName, pruebas);
                } catch (Exception e) {
                    CONSOLE.log(Level.SEVERE, "Ocurrio un error cerrando la orden " + order.getOrderNumber(), e);
                    return Response.ok(new ResponseDTO(-1, "Ocurrio un error cerrando la orden " + order.getOrderNumber())).build();
                }
                continue;
            }

            List<Object[]> orderStock = soFacade.findOrdersStockAvailability(order.getOrderNumber(), new ArrayList(pendingItems.keySet()), warehouseCode, companyName, pruebas);
            HashMap<String, List<Object[]>> availableStock = parseOrderAvailableStock(orderStock);
            HashSet<String> itemsMissing = new HashSet<>();

            for (String pendingItemcode : pendingItems.keySet()) {
                //Si no hay inventario y no se ha hecho picking para la referencia, la agrega a la lista de lineas para cerrar en la orden
                if (!availableStock.containsKey(pendingItemcode) && !pickedItems.containsKey(pendingItemcode)) {
                    itemsMissing.add(pendingItemcode);
                    continue;
                }
                if (!availableStock.containsKey(pendingItemcode) && pickedItems.containsKey(pendingItemcode)) {
                    //reprocesar orden para que se genere cierre si no hay mas items pendientes
                    salesOrderEJB.modifySalesOrderQuantity(companyName, orderDocEntry, pendingItemcode, getTotalPicked(pickedItems.get(pendingItemcode)));
                    //soFacade.modifySalesOrderQuantity(orderDocEntry, pendingItemcode, getTotalPicked(pickedItems.get(pendingItemcode)), getPriceItem(pendingItemcode, companyName, pruebas), companyName, pruebas);
                }
            }

            if (!itemsMissing.isEmpty()) {
                //TODO: Marcar lineas de orden cerradas para items sin saldo
                //ResponseDTO res = salesOrderEJB.closeOrderLines(companyName, orderDocEntry, itemsMissing);
                boolean res = soFacade.closeOrderLines(orderDocEntry, itemsMissing, companyName, pruebas);
                //if (res.getCode() < 0) {
                if (!res) {
                    PickingWarningDTO warning = new PickingWarningDTO();
                    warning.setItems(new ArrayList<>(itemsMissing));
                    warning.setMessage(
                            String.format(
                                    "Ocurrió un error al cerrar las líneas de la órden %s para los productos que no tienen saldo: %s",
                                    order.getOrderNumber(),
                                    Arrays.toString(itemsMissing.toArray())));
                    warning.setOrderNumber(orderDocEntry);
                    warnings.add(warning);
                }

                //notificar cierre de lineas
                if (itemsMissing.size() == pendingItems.size()) {
                    //Finaliza la orden de picking ya que no quedan items pendientes
                    CONSOLE.log(Level.WARNING, "La orden {0} no tiene saldo en picking para los items pendientes por despachar y se marca como cerrada. ", order.getOrderNumber());
                    try {
                        closeAndPack(order, pickedItems, warehouseCode, companyName, pruebas);
                    } catch (Exception e) {
                        CONSOLE.log(Level.SEVERE, "Ocurrio un error cerrando la orden " + order.getOrderNumber(), e);
                        return Response.ok(new ResponseDTO(-1, "Ocurrio un error cerrando la orden " + order.getOrderNumber())).build();
                    }
                    continue;
                }
            }

            //Agregar el inventario de la orden al set de stock
            for (Object[] row : orderStock) {
                SortedStockDTO sorted = new SortedStockDTO(row);
                if (avoidBin(skipped, sorted.getItemCode(), (String) row[10], Constants.BIN_TYPE_PICKING)) {
                    //No agrega el registro de saldo a la lista si el item solo debe ser tomado de zona de almacenamiento
                    CONSOLE.log(Level.INFO, "No se tiene en cuenta para picking el inventario en la ubicacion {0} para el item {1} porque la ubicacion NO es de almacenamiento",
                            new Object[]{sorted.getBinCode(), sorted.getItemCode()});
                    continue;
                }

                Integer pendingQuantity = pendingItems.get(sorted.getItemCode());
                if (pendingItems.containsKey(sorted.getItemCode()) && pendingQuantity > 0) {
                    sorted.setPendingQuantity(pendingQuantity);
                    sortedStock.add(sorted);
                }
            }
            continue;
        }

        //seleccionar y retornar el siguiente item para picking
        if (sortedStock.isEmpty()) {
            return Response.ok(new ResponseDTO(-1, warnings)).build();
        } else {
            //return Response.ok(new ResponseDTO(0, sortedStock.first())).build();
            return Response.ok(new ResponseDTO(0, sortedStock)).build();
        }
    }

    private HashMap<String, List<Object[]>> parseOrderAvailableStock(List<Object[]> stock) {
        HashMap<String, List<Object[]>> availableStock = new HashMap<>();
        for (Object[] row : stock) {
            String itemcode = (String) row[0];
            if (availableStock.containsKey(itemcode)) {
                availableStock.get(itemcode).add(row);
            } else {
                List<Object[]> data = new ArrayList<>();
                data.add(row);
                availableStock.put(itemcode, data);
            }
        }
        return availableStock;
    }

    private Integer getTotalPicked(Map<Long, Integer> picked) {
        int sum = 0;
        for (Integer quantity : picked.values()) {
            sum += quantity;
        }
        return sum;
    }

    private BigDecimal getPriceItem(String itemcode, String companyName, boolean testing) {
        return itemFacade.getItemPrice(itemcode, companyName, testing);
    }

    private boolean avoidBin(TreeSet<String> skippedItems, String itemCode, String binType, String binTypeToAvoid) {
        return skippedItems != null && skippedItems.contains(itemCode) && binType != null && binType.equals(binTypeToAvoid);
    }

    @PUT
    @Path("close/{username}")
    @Consumes({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response closeOrders(@PathParam("username") String username,
                                @QueryParam("orderNumber") Integer orderNumber,
                                @QueryParam("position") Integer position,
                                @HeaderParam("X-Company-Name") String companyName,
                                @HeaderParam("X-Warehouse-Code") String warehouseCode,
                                @HeaderParam("X-Pruebas") boolean pruebas) {
        CONSOLE.log(Level.INFO, "Procesando solicitud de cierre de orden {0}", orderNumber != null ? orderNumber : "multiple");

        //buscar ordenes asignadas en estado open para el empleado en la empresa seleccionada
        List<AssignedOrder> orders = aoFacade.listOpenAssignationsByUserAndCompany(username, orderNumber, companyName, pruebas);

        //si no hay ordenes pendientes, mostrar alerta con mensaje de error
        if (orders == null || orders.isEmpty()) {
            CONSOLE.log(Level.WARNING, "No se encontraron ordenes de venta asignadas al usuario {0} en la empresa {1}", new Object[]{username, companyName});
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        for (AssignedOrder order : orders) {
            CONSOLE.log(Level.INFO, "Procesando estado de picking para {0}", order.toString());
            Object[] pickingStatus = processPickingStatus(order.getOrderNumber(), true, companyName, pruebas);
            //validar datos consultados de las dos instancias
            if (pickingStatus.equals(null)) {
                return Response.ok(new ResponseDTO(-1, "Ocurrio un error consultandos datos de alguna de las instancias(SAP-MySQL).")).build();
            }

            LinkedHashMap<String, Integer> pendingItems = (LinkedHashMap<String, Integer>) pickingStatus[0];
            HashMap<String, Map<Long, Integer>> pickedItems = (HashMap<String, Map<Long, Integer>>) pickingStatus[1];

            //validar si este mapa trae items con valor cero o si solo incluye items que tengan algun valor
            //validar que todos los items pendientes tengan cantidad igual a cero
            for (String itemCode : pendingItems.keySet()) {
                if (pendingItems.get(itemCode) > 0) {
                    CONSOLE.log(Level.SEVERE, "Se intento marcar como finalizada una orden que aun tiene items pendientes por picking. orden={0}, usuario={1}, item={2}, pendiente={3}",
                            new Object[]{order.getOrderNumber(), username, itemCode, pendingItems.get(itemCode)});
                    return Response.ok(new ResponseDTO(-1, "La orden " + order.getOrderNumber() + " todavía tiene "
                            + pendingItems.get(itemCode) + " unidades pendientes del item " + itemCode)).build();
                }
            }

            try {
                closeAndPack(order, pickedItems, warehouseCode, companyName, pruebas);
            } catch (Exception e) {
                CONSOLE.log(Level.SEVERE, "Ocurrio un error cerrando la orden " + order.getOrderNumber(), e);
                return Response.ok(new ResponseDTO(-1, "Ocurrio un error cerrando la orden " + order.getOrderNumber())).build();
            }
        }
        return Response.ok(new ResponseDTO(0, "")).build();
    }

    private void closeAndPack(AssignedOrder order, Map<String, Map<Long, Integer>> pickedItems, String warehouseCode, String companyName, boolean pruebas) {
        try {
            HashMap<Long, String[]> bins = new HashMap<>();
            PackingOrder packingOrder = new PackingOrder();
            packingOrder.setCustomerId(order.getCustomerId());
            packingOrder.setCustomerName(order.getCustomerName());
            packingOrder.setOrderNumber(order.getOrderNumber());
            packingOrder.setStatus(Constants.STATUS_OPEN);
            packingOrder.setCompanyName(companyName);
            packingOrder.setWarehouseCode(warehouseCode);

            for (String itemCode : pickedItems.keySet()) {
                PackingOrderItem packingItem = new PackingOrderItem();
                packingItem.setItemCode(itemCode);
                packingItem.setPackingOrder(packingOrder);
                for (Long binAbs : pickedItems.get(itemCode).keySet()) {
                    PackingOrderItemBin bin = new PackingOrderItemBin();
                    bin.setBinAbs(binAbs);
                    if (!bins.containsKey(binAbs)) {
                        Object[] binAndName = blFacade.getBinCodeAndName(binAbs, companyName, pruebas);
                        bins.put(binAbs, Arrays.copyOf(binAndName, binAndName.length, String[].class));
                    }

                    bin.setBinCode(bins.get(binAbs)[0]);
                    bin.setBinName(bins.get(binAbs)[1]);

                    bin.setPackedQty(0);
                    bin.setPickedQty(pickedItems.get(itemCode).get(binAbs));
                    bin.setPackingOrderItem(packingItem);
                    packingItem.getBins().add(bin);
                }
                packingOrder.getItems().add(packingItem);
            }
            if (!pickedItems.isEmpty()) {
                poFacade.create(packingOrder, companyName, pruebas);
                CONSOLE.log(Level.INFO, "Se creo la orden de packing para la orden {0}", order.getOrderNumber());
            }

            order.setStatus(Constants.STATUS_CLOSED);
            aoFacade.edit(order, companyName, pruebas);
            CONSOLE.log(Level.INFO, "Cerro la asignacion de picking para la orden {0}", order.getOrderNumber());
        } catch (Exception e) {
            CONSOLE.log(Level.SEVERE, "Ocurrio un error al actualizar el estado de la asignacion de picking. ", e);
        }
    }

    private void moveItemsToPackingArea(Integer orderNumber, String companyName, boolean pruebas) {
        stockTransferEJB.transferClosedPickingToPackingArea(orderNumber, companyName, pruebas);
    }
}