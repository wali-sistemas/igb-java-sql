package co.igb.util;

/**
 * @author dbotero
 */
public class Constants {
    public static final String INVOICE_SERIES = "igb.invoice.series";
    public static final String STOCK_TRANSFER_SERIES = "igb.stock-transfer.series";
    public static final String SALES_ORDER_SERIES = "igb.sales.order.series";
    public static final String DELIVERY_NOTE_SERIES = "igb.delivery.note.series";
    public static final String COMPANIES = "igb.login.companies";
    public static final String INITIAL_CONTEXT_FACTORY = "initial.context.factory";
    public static final String PROVIDER_URL = "provider.url";
    public static final String SECURITY_AUTHENTICATION = "security.authentication";
    public static final String SECURITY_PRINCIPAL_DOMAIN = "security.principal.domain";
    public static final String LDAP_QUERY_USER = "ldap.query.user";
    public static final String LDAP_QUERY_PASSWORD = "ldap.query.password";
    public static final String LDAP_USERS_CONTAINER = "OU=Usuarios,DC=igbcolombia,DC=local";
    public static final String LDAP_ENABLED_USERS_FILTER = "(&(objectClass=user)(!(userAccountControl:1.2.840.113556.1.4.803:=2)))";
    public static final String LDAP_USERNAME_FIELD = "sAMAccountName";
    public static final String LDAP_EMAIL_FIELD = "mail";
    public static final String LDAP_NAME_FIELD = "givenname";
    public static final String LDAP_LASTNAME_FIELD = "sn";
    public static final String LDAP_FULLNAME_FIELD = "cn";
    public static final String LDAP_MEMBEROF_FIELD = "memberOf";
    public static final String LDAP_MEMBEROF_FILTER = "CN=WMS,OU=Usuarios,DC=igbcolombia,DC=local";
    public static final String NO_FILTER_TEMPLATES = "igb.no-filter.templates";
    public static final String NO_FILTER_PATHS = "igb.no-filter.paths";
    public static final String EMAIL_HOST = "mail.host";
    public static final String EMAIL_PORT = "mail.port";
    public static final String EMAIL_USERNAME = "mail.username";
    public static final String EMAIL_PASSWORD = "mail.password";
    public static final String EMAIL_FROM_INVENTORY_INCONSISTENCY = "mail.from.inventory.inconsistency";
    public static final String EMAIL_SUBJECT_INVENTORY_INCONSISTENCY = "mail.subject.inventory.inconsistency";
    public static final String EMAIL_MSG_INVENTORY_INCONSISTENCY = "mail.msg.inventory.inconsistency";
    public static final String EMAIL_MSG_INVENTORY_INCONSISTENCY_BODY = "mail.msg.inventory.inconsistency.body";
    public static final String EMAIL_MSG_INVENTORY_INCONSISTENCY_HEAD = "mail.msg.inventory.inconsistency.head";
    public static final String EMAIL_MSG_INVENTORY_INCONSISTENCY_LINE = "mail.msg.inventory.inconsistency.line";
    public static final String EMAIL_TO_INVENTORY_INCONSISTENCY = "mail.to.inventory.inconsistency";
    public static final String EMAIL_BCC_INVENTORY_INCONSISTENCY = "mail.bcc.inventory.inconsistency";
    public static final String EMAIL_FROM_PACKING_LIST = "mail.from.packing.list";
    public static final String EMAIL_SUBJECT_PACKING_LIST = "mail.subject.packing.list";
    public static final String EMAIL_MSG_PACKING_LIST = "mail.msg.packing.list";
    public static final String EMAIL_CC_PACKING_LIST = "mail.cc.packing.list";
    public static final String EMAIL_FROM_PACKING_ERROR = "mail.from.packing.error";
    public static final String EMAIL_SUBJECT_PACKING_ERROR = "mail.subject.packing.error";
    public static final String EMAIL_MSG_PACKING_ERROR = "mail.msg.packing.error";
    public static final String EMAIL_TO_PACKING_ERROR = "mail.to.packing.error";
    public static final String EMAIL_CC_PACKING_ERROR = "mail.cc.packing.error";
    public static final String EMAIL_BCC_PACKING_ERROR = "mail.bcc.packing.error";
    public static final String STATUS_CLOSED = "closed";
    public static final String STATUS_OPEN = "open";
    public static final String SAP_STATUS_OPEN = "O";
    public static final String SAP_STATUS_NO = "tNO";
    public static final String TEMPORARY_PICKING_TTL = "igb.temporary.picking.ttl";
    public static final String B1WS_WSDL_URL = "igb.b1ws.wsdlUrl";
    public static final String B1WS_DATABASE_SERVER = "igb.b1ws.databaseServer";
    public static final String B1WS_DATABASE_NAME = "igb.b1ws.databaseName";
    public static final String B1WS_DATABASE_TYPE = "igb.b1ws.databaseType";
    public static final String B1WS_COMPANY_USERNAME = "igb.b1ws.companyUsername";
    public static final String B1WS_COMPANY_PASSWORD = "igb.b1ws.companyPassword";
    public static final String B1WS_LANGUAGE = "igb.b1ws.language";
    public static final String B1WS_LICENSE_SERVER = "igb.b1ws.licenseServer";
    public static final String B1WS_LOGIN_SERVICE = "LoginService";
    public static final String B1WS_DELIVERY_NOTE_SERVICE = "DeliveryNotesService";
    public static final String B1WS_ORDERS_SERVICE = "OrdersService";
    public static final String BIN_TYPE_PICKING = "PICKING";
    //public static final String DATABASE_TYPE_MYSQL = "mysql";
    public static final String DATABASE_TYPE_MSSQL = "mssql";
    public static final String DATABASE_TYPE_WALI = "WMS";
    public static final String BIN_ACTION_TYPE_FROM = "batFromWarehouse";
    public static final String BIN_ACTION_TYPE_TO = "batToWarehouse";
    public static final long CODE_FLETE_GRABABLE = 1l;
    public static final String[] LDAP_ATTR_IDS = {"name", "givenName", "description", "sAMAccountname", "userPrincipalName", "sn", "distinguishedName", "cn", "mail", "memberOf"};
    public static final String SOUL_USER = "811011909";
    public static final String SOUL_PASSWORD = "IgbMp2020*";
}