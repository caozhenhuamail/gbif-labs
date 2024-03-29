package org.gbif.registry2.ws.util;

import javax.ws.rs.core.CacheControl;

/**
 * Class containing constant values used across legacy resources, in answering legacy web service requests (GBRDS/IPT).
 */
public class LegacyResourceConstants {

  /**
   * Empty constructor.
   */
  LegacyResourceConstants() {
  }

  // request / response key names
  public static final String KEY_PARAM = "key";
  public static final String ORGANIZATION_KEY_PARAM = "organisationKey";
  public static final String IPT_KEY_PARAM = "iptKey";
  public static final String NAME_PARAM = "name";
  public static final String NAME_LANGUAGE_PARAM = "nameLanguage";
  public static final String DESCRIPTION_PARAM = "description";
  public static final String DESCRIPTION_LANGUAGE_PARAM = "descriptionLanguage";
  public static final String LOGO_URL_PARAM = "logoURL";
  public static final String HOMEPAGE_URL_PARAM = "homepageURL";
  public static final String PRIMARY_CONTACT_NAME_PARAM = "primaryContactName";
  public static final String PRIMARY_CONTACT_EMAIL_PARAM = "primaryContactEmail";
  public static final String PRIMARY_CONTACT_TYPE_PARAM = "primaryContactType";
  public static final String PRIMARY_CONTACT_ADDRESS_PARAM = "primaryContactAddress";
  public static final String PRIMARY_CONTACT_PHONE_PARAM = "primaryContactPhone";
  public static final String PRIMARY_CONTACT_DESCRIPTION_PARAM = "primaryContactDescription";
  public static final String SERVICE_TYPES_PARAM = "serviceTypes";
  public static final String SERVICE_URLS_PARAM = "serviceURLs";
  public static final String WS_PASSWORD_PARAM = "wsPassword";
  public static final String NODE_NAME_PARAM = "nodeName";
  public static final String NODE_KEY_PARAM = "nodeKey";
  public static final String NODE_CONTACT_EMAIL = "nodeContactEmail";
  public static final String RESOURCE_KEY_PARAM = "resourceKey";
  public static final String TYPE_PARAM = "type";
  public static final String TYPE_DESCRIPTION_PARAM = "typeDescription";
  public static final String ACCESS_POINT_URL_PARAM = "accessPointURL";

  // request / response value names
  public static final String ADMINISTRATIVE_CONTACT_TYPE = "administrative";
  public static final String TECHNICAL_CONTACT_TYPE = "technical";
  public static final String CHECKLIST_SERVICE_TYPE_1 = "DWC-ARCHIVE-CHECKLIST";
  public static final String CHECKLIST_SERVICE_TYPE_2 = "DWC_ARCHIVE_CHECKLIST";
  public static final String OCCURRENCE_SERVICE_TYPE_1 = "DWC-ARCHIVE-OCCURRENCE";
  public static final String OCCURRENCE_SERVICE_TYPE_2 = "DWC_ARCHIVE_OCCURRENCE";

  // used to ensure Response is not cached, forcing the IPT to make a new request
  public static final CacheControl CACHE_CONTROL_DISABLED = CacheControl.valueOf("no-cache");
  // web service paging request size
  public static final int WS_PAGE_SIZE = 100;

  // TODO: remove once security implemented
  public static final String USER = "GBIF Registry Web Services";
}
