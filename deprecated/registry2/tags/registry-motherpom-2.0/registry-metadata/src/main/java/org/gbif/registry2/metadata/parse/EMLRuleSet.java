package org.gbif.registry2.metadata.parse;

import org.gbif.api.model.common.InterpretedEnum;
import org.gbif.api.model.registry2.Citation;
import org.gbif.api.model.registry2.Contact;
import org.gbif.api.model.registry2.Identifier;
import org.gbif.api.model.registry2.eml.Collection;
import org.gbif.api.model.registry2.eml.DataDescription;
import org.gbif.api.model.registry2.eml.KeywordCollection;
import org.gbif.api.model.registry2.eml.Project;
import org.gbif.api.model.registry2.eml.SamplingDescription;
import org.gbif.api.model.registry2.eml.TaxonomicCoverage;
import org.gbif.api.model.registry2.eml.TaxonomicCoverages;
import org.gbif.api.model.registry2.eml.curatorial.CuratorialUnitComposite;
import org.gbif.api.model.registry2.eml.geospatial.BoundingBox;
import org.gbif.api.model.registry2.eml.geospatial.GeospatialCoverage;
import org.gbif.api.model.registry2.eml.temporal.DateRange;
import org.gbif.api.model.registry2.eml.temporal.SingleDate;
import org.gbif.api.model.registry2.eml.temporal.VerbatimTimePeriod;
import org.gbif.api.model.registry2.eml.temporal.VerbatimTimePeriodType;
import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.Language;
import org.gbif.api.vocabulary.PreservationMethodType;
import org.gbif.api.vocabulary.Rank;
import org.gbif.api.vocabulary.registry2.ContactType;
import org.gbif.api.vocabulary.registry2.IdentifierType;
import org.gbif.registry2.metadata.parse.converter.ContactTypeConverter;
import org.gbif.registry2.metadata.parse.converter.CountryTypeConverter;
import org.gbif.registry2.metadata.parse.converter.DateConverter;
import org.gbif.registry2.metadata.parse.converter.GreedyUriConverter;
import org.gbif.registry2.metadata.parse.converter.IdentifierTypeConverter;
import org.gbif.registry2.metadata.parse.converter.LanguageTypeConverter;
import org.gbif.registry2.metadata.parse.converter.PreservationMethodTypeConverter;

import java.net.URI;
import java.util.Date;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.digester3.AbstractObjectCreationFactory;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.RuleSetBase;
import org.apache.commons.digester3.SetNextRule;
import org.apache.commons.digester3.SetRootRule;
import org.xml.sax.Attributes;

/**
 * Digester rules to parse EML dataset metadata documents together with a DatasetDelegator digester model.
 * The rules here ignore any namespace to be able to work with any eml versions after 2.0.
 */
public class EMLRuleSet extends RuleSetBase {

  private void setupTypeConverters() {

    GreedyUriConverter uriConverter = new GreedyUriConverter();
    ConvertUtils.register(uriConverter, URI.class);

    LanguageTypeConverter langConverter = new LanguageTypeConverter();
    ConvertUtils.register(langConverter, Language.class);


    // handles the Identifier.type string -> IdentifierType conversion
    IdentifierTypeConverter identifierTypeConverter = new IdentifierTypeConverter(IdentifierType.UNKNOWN);
    ConvertUtils.register(identifierTypeConverter, IdentifierType.class);

    DateConverter dateConverter = new DateConverter();
    ConvertUtils.register(dateConverter, Date.class);

    CountryTypeConverter countryTypeConverter = new CountryTypeConverter();
    ConvertUtils.register(countryTypeConverter, Country.class);

    ContactTypeConverter typeConverter = new ContactTypeConverter(ContactType.ADMINISTRATIVE_POINT_OF_CONTACT);
    ConvertUtils.register(typeConverter, ContactType.class);

    // Collection with CuratorialUnits
    Converter intConverter = new IntegerConverter();
    ConvertUtils.register(intConverter, Integer.class);

    // handles the Collection.specimenPreservationType string -> PreservationMethodType enum conversion
    PreservationMethodTypeConverter preservationMethodTypeConverter = new PreservationMethodTypeConverter(PreservationMethodType.OTHER);
    ConvertUtils.register(preservationMethodTypeConverter, PreservationMethodType.class);
  }
  /**
   * Creates a new digester with all rules to parse an EML document.
   */
  public void addRuleInstances(Digester digester) {

    setupTypeConverters();

    // language as xml:lang attribute
    digester.addCallMethod("eml", "setLanguage", 1, new Class[]{Language.class} );
    digester.addCallParam("eml", 0, "xml:lang");

    // // guid as packageId attribute
    // digester.addCallMethod("eml", "setPackageId", 1);
    // digester.addCallParam("eml", 0, "packageId");

    // identifiers
    digester.addObjectCreate("eml/dataset/alternateIdentifier", Identifier.class);
    digester.addBeanPropertySetter("eml/dataset/alternateIdentifier", "identifier");
    digester.addBeanPropertySetter("eml/dataset/alternateIdentifier", "type");
    digester.addSetNext("eml/dataset/alternateIdentifier", "addIdentifier");

    // title (no language support in Dataset)
    digester.addBeanPropertySetter("eml/dataset/title", "title");

    // SamplingDescription
    addSamplingDescriptionRules(digester, "eml/dataset/methods", "setSamplingDescription");

    // WritableDataset properties
    digester.addBeanPropertySetter("eml/dataset/language", "dataLanguage");
    digester.addBeanPropertySetter("eml/dataset/abstract/para", "description");
    digester.addBeanPropertySetter("eml/dataset/distribution/online/url", "homepage");
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/resourceLogoUrl", "logoURL");

    // Citation
    addCitationRules(digester, "eml/additionalMetadata/metadata/gbif/citation", "setCitation");

    // digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/hierarchyLevel", "hierarchyLevel");

    // Publication date
    digester.addCallMethod("eml/dataset/pubDate", "setPubDateAsString", 1);
    digester.addCallParam("eml/dataset/pubDate", 0);

    // digester.addCallMethod("eml/additionalMetadata/metadata/gbif/dateStamp", "setDateStamp", 1);
    // digester.addCallParam("eml/additionalMetadata/metadata/gbif/dateStamp", 0);

    addContactRules(digester, "eml/dataset/creator", "addPreferredOriginatorContact");
    addContactRules(digester, "eml/dataset/metadataProvider", "addPreferredMetadataContact");
    addContactRules(digester, "eml/dataset/associatedParty", "addContact");

    addContactRules(digester, "eml/dataset/contact", "addPreferredAdministrativeContact");

    digester.addBeanPropertySetter("eml/dataset/purpose/para", "purpose");
    digester.addBeanPropertySetter("eml/dataset/additionalInfo/para", "additionalInfo");
    digester.addBeanPropertySetter("eml/dataset/intellectualRights/para", "rights");

    // KeywordCollections
    addKeywordCollectionRules(digester, "eml/dataset/keywordSet", "addKeywordCollection");

    // BibliographicCitations
    addCitationRules(digester, "eml/additionalMetadata/metadata/gbif/bibliography/citation",
      "addBibliographicCitation");

    // Data Description (Physical Data aka External Links)
    addDataDescriptionRules(digester, "eml/additionalMetadata/metadata/gbif/physical", "addDataDescription");

    // Geographic coverage
    addGeographicCoverageRules(digester, "eml/dataset/coverage/geographicCoverage", "addGeographicCoverage");

    // Temporal coverage
    addTemporalCoverageRules(digester);

    // Taxonomic coverage
    addTaxonomicCoverageRules(digester);

    // Project
    addProjectRules(digester, "eml/dataset/project", "setProject");

    addCollectionRules(digester, "eml/additionalMetadata/metadata/gbif/collection", "addCollection");

    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/specimenPreservationMethod",
      "specimenPreservationMethod");

  }

  private void addTaxonomicCoverageRules(Digester digester) {
    final String prefix = "eml/dataset/coverage/taxonomicCoverage";

    digester.addObjectCreate(prefix, TaxonomicCoverages.class);
    digester.addBeanPropertySetter(prefix + "/generalTaxonomicCoverage", "description");

    digester.addObjectCreate(prefix + "/taxonomicClassification", TaxonomicCoverage.class);
    digester.addBeanPropertySetter(prefix + "/taxonomicClassification/taxonRankValue", "scientificName");
    digester.addBeanPropertySetter(prefix + "/taxonomicClassification/commonName", "commonName");

    digester.addFactoryCreate(prefix + "/taxonomicClassification/taxonRankName", new InterpretedRankEnumFactory());
    digester.addBeanPropertySetter(prefix + "/taxonomicClassification/taxonRankName", "verbatim");

    digester.addSetNext(prefix + "/taxonomicClassification/taxonRankName", "setRank");

    digester.addSetNext(prefix + "/taxonomicClassification", "addCoverages");

    digester.addSetNext(prefix, "addTaxonomicCoverages");
  }

  public class InterpretedRankEnumFactory extends AbstractObjectCreationFactory {
    @Override
    public Object createObject(Attributes attributes) throws Exception {
      return new InterpretedEnum<String, Rank>();
    }
  }
  /**
   * This is a reusable set of rules to build Citation and add the Citation as per the method called on parent object
   * which is the previous stack object.
   *
   * @param digester     to add the rules to
   * @param path         The XPath for extracting the Citation information
   * @param parentMethod Of the previous stack object to call and add the Citation to
   */
  private void addCitationRules(Digester digester, String path, String parentMethod) {
    digester.addObjectCreate(path, Citation.class);
    digester.addBeanPropertySetter(path, "text");
    digester.addCallMethod(path, "setIdentifier", 1);
    digester.addCallParam(path, 0, "identifier");
    digester.addSetNext(path, parentMethod);
  }

  /**
   * This is a reusable set of rules to build a Collection and add the Collection as per the method
   * called on parent object which is the previous stack object.
   *
   * @param digester     to add the rules to
   * @param prefix       The XPath prefix to prepend for extracting the Collection information
   * @param parentMethod Of the previous stack object to call and add the Collection to
   */
  private void addCollectionRules(Digester digester, String prefix, String parentMethod) {
    digester.addObjectCreate(prefix, Collection.class);
    digester.addBeanPropertySetter(prefix + "/parentCollectionIdentifier", "parentCollectionIdentifier");
    digester.addBeanPropertySetter(prefix + "/collectionIdentifier", "collectionIdentifier");
    digester.addBeanPropertySetter(prefix + "/collectionName", "collectionName");
    addCuratorialUnit(digester, "eml/additionalMetadata/metadata/gbif/jgtiCuratorialUnit", "addCuratorial");
    digester.addSetNext(prefix, parentMethod);
  }

  /**
   * This is a reusable set of rules to build Contacts and add the Contact as per the method called on parent object
   * which is the previous stack object.
   *
   * @param digester     to add the rules to
   * @param prefix       The XPath prefix to prepend for extracting the Contact information
   * @param parentMethod Of the previous stack object to call and add the Contact to
   */
  private void addContactRules(Digester digester, String prefix, String parentMethod) {
    digester.addObjectCreate(prefix, Contact.class);
    digester.addBeanPropertySetter(prefix + "/individualName/givenName", "firstName");
    digester.addBeanPropertySetter(prefix + "/individualName/surName", "lastName");
    digester.addBeanPropertySetter(prefix + "/organizationName", "organization");
    digester.addBeanPropertySetter(prefix + "/positionName", "position");
    digester.addBeanPropertySetter(prefix + "/phone", "phone");
    digester.addBeanPropertySetter(prefix + "/electronicMailAddress", "email");
    digester.addBeanPropertySetter(prefix + "/role", "type");
    digester.addBeanPropertySetter(prefix + "/address/city", "city");
    digester.addBeanPropertySetter(prefix + "/address/administrativeArea", "province");
    digester.addBeanPropertySetter(prefix + "/address/postalCode", "postalCode");
    digester.addBeanPropertySetter(prefix + "/address/country", "country");
    digester.addBeanPropertySetter(prefix + "/address/deliveryPoint", "address");
    digester.addSetNext(prefix, parentMethod);
  }

  /**
   * This is a reusable set of rules to build a CuratorialUnit and add the CuratorialUnit as per the method
   * called on parent object which is the previous stack object.
   *
   * @param digester     to add the rules to
   * @param prefix       The XPath prefix to prepend for extracting the CuratorialUnit information
   * @param parentMethod Of the previous stack object to call and add the CuratorialUnit to
   */
  private void addCuratorialUnit(Digester digester, String prefix, String parentMethod) {
    digester.addObjectCreate(prefix, CuratorialUnitComposite.class);
    digester.addCallMethod(prefix + "/jgtiUnits", "addDeviation", 1);
    digester.addCallParam(prefix + "/jgtiUnits", 0, "uncertaintyMeasure");
    digester.addBeanPropertySetter(prefix + "/jgtiUnitType", "typeVerbatim");
    digester.addBeanPropertySetter(prefix + "/jgtiUnits", "count");
    digester.addBeanPropertySetter(prefix + "/jgtiUnitRange/beginRange", "lower");
    digester.addBeanPropertySetter(prefix + "/jgtiUnitRange/endRange", "upper");
    digester.addSetNext(prefix + "/jgtiUnitType", parentMethod);
  }

  /**
   * This is a reusable set of rules to build a DataDescription and add the DataDescription as per the method
   * called on parent object which is the previous stack object.
   *
   * @param digester     to add the rules to
   * @param prefix       The XPath prefix to prepend for extracting the DataDescription information
   * @param parentMethod Of the previous stack object to call and add the DataDescription to
   */
  private void addDataDescriptionRules(Digester digester, String prefix, String parentMethod) {
    digester.addObjectCreate(prefix, DataDescription.class);
    digester.addBeanPropertySetter(prefix + "/objectName", "name");
    digester.addBeanPropertySetter(prefix + "/characterEncoding", "charset");
    digester.addBeanPropertySetter(prefix + "/dataFormat/externallyDefinedFormat/formatName", "format");
    digester.addBeanPropertySetter(prefix + "/dataFormat/externallyDefinedFormat/formatVersion", "formatVersion");
    digester.addBeanPropertySetter(prefix + "/distribution/online/url", "url");
    digester.addSetNext(prefix, parentMethod);
  }

  /**
   * Adds rules to get the geographic coverage.
   *
   * @param digester     to add the rules to
   * @param prefix       The XPath prefix to prepend for extracting the geographic information
   * @param parentMethod Of the previous stack object to call and add the geographic information to
   */
  private void addGeographicCoverageRules(Digester digester, String prefix, String parentMethod) {
    digester.addObjectCreate(prefix, GeospatialCoverage.class);
    digester.addBeanPropertySetter(prefix + "/geographicDescription", "description");
    digester.addObjectCreate(prefix + "/boundingCoordinates", BoundingBox.class);
    digester.addBeanPropertySetter(prefix + "/boundingCoordinates/westBoundingCoordinate", "minLongitude");
    digester.addBeanPropertySetter(prefix + "/boundingCoordinates/eastBoundingCoordinate", "maxLongitude");
    digester.addBeanPropertySetter(prefix + "/boundingCoordinates/northBoundingCoordinate", "maxLatitude");
    digester.addBeanPropertySetter(prefix + "/boundingCoordinates/southBoundingCoordinate", "minLatitude");
    digester.addSetNext(prefix + "/boundingCoordinates", "setBoundingBox"); // add the BBox to the GeospatialCoverage
    digester.addSetNext(prefix, parentMethod); // add the GeospatialCoverage to the list in EML
  }


  /**
   * Add rules to extract a KeywordCollection and add the KeywordCollection as per the method called on parent object
   * which is the previous stack object.
   *
   * @param digester     to add the rules to
   * @param prefix       The XPath prefix to prepend for extracting the KeywordCollection information
   * @param parentMethod Of the previous stack object to call and add the KeywordCollection to
   */
  private void addKeywordCollectionRules(Digester digester, String prefix, String parentMethod) {
    digester.addObjectCreate(prefix, KeywordCollection.class);
    digester.addCallMethod(prefix + "/keyword", "addKeyword", 1);
    digester.addCallParam(prefix + "/keyword", 0);
    digester.addBeanPropertySetter(prefix + "/keywordThesaurus", "thesaurus");
    digester.addSetNext(prefix, parentMethod);
  }


  /**
   * This is a reusable set of rules to build a Project and add the Project as per the method called on parent object
   * which is the previous stack object.
   *
   * @param digester     to add the rules to
   * @param prefix       The XPath prefix to prepend for extracting the Project information
   * @param parentMethod Of the previous stack object to call and add the Project to
   */
  private void addProjectRules(Digester digester, String prefix, String parentMethod) {
    digester.addObjectCreate(prefix, Project.class);
    digester.addBeanPropertySetter(prefix + "/title", "title");
    addContactRules(digester, prefix + "/personnel", "addContact");
    digester.addBeanPropertySetter(prefix + "/funding/para", "funding");
    digester.addBeanPropertySetter(prefix + "/studyAreaDescription/descriptor/descriptorValue", "studyAreaDescription");
    digester.addBeanPropertySetter(prefix + "/designDescription/description/para", "designDescription");
    digester.addSetNext(prefix, parentMethod);
  }

  /**
   * This is a reusable set of rules to build a SamplingDescription and add the SamplingDescription as per the method
   * called on parent object which is the previous stack object.
   *
   * @param digester     to add the rules to
   * @param prefix       The XPath prefix to prepend for extracting the SamplingDescription information
   * @param parentMethod Of the previous stack object to call and add the SamplingDescription to
   */
  private void addSamplingDescriptionRules(Digester digester, String prefix, String parentMethod) {
    digester.addObjectCreate(prefix, SamplingDescription.class);
    digester.addBeanPropertySetter(prefix + "/sampling/studyExtent/description/para", "studyExtent");
    digester.addBeanPropertySetter(prefix + "/sampling/studyExtent/description/para", "studyExtent");
    digester.addBeanPropertySetter(prefix + "/sampling/samplingDescription/para", "sampling");
    digester.addBeanPropertySetter(prefix + "/qualityControl/description/para", "qualityControl");

    digester.addObjectCreate(prefix + "/methodStep", ParagraphContainer.class);
    digester.addCallMethod(prefix + "/methodStep/description/para", "appendParagraph", 0);
    digester.addRule(prefix + "/methodStep/description", new SetRootRule("addMethodStep", ParagraphContainer.class));

    SetNextRule nextRule = new SetNextRule(parentMethod);
    nextRule.setFireOnBegin(true);
    digester.addRule(prefix, nextRule);
  }

  /**
   * Adds rules to extract the temporal coverages, which can be single dates, date ranges, living time periods
   * or formation periods.
   *
   * @param digester to add the rules to
   */
  private void addTemporalCoverageRules(Digester digester) {

    digester.addObjectCreate("eml/dataset/coverage/temporalCoverage/singleDateTime", SingleDate.class);
    digester.addBeanPropertySetter("eml/dataset/coverage/temporalCoverage/singleDateTime/calendarDate", "date");
    digester.addSetNext("eml/dataset/coverage/temporalCoverage/singleDateTime", "addTemporalCoverage");

    digester.addObjectCreate("eml/dataset/coverage/temporalCoverage/rangeOfDates", DateRange.class);
    digester
      .addBeanPropertySetter("eml/dataset/coverage/temporalCoverage/rangeOfDates/beginDate/calendarDate", "start");
    digester.addBeanPropertySetter("eml/dataset/coverage/temporalCoverage/rangeOfDates/endDate/calendarDate", "end");
    digester.addSetNext("eml/dataset/coverage/temporalCoverage/rangeOfDates", "addTemporalCoverage");

    digester.addObjectCreate("eml/additionalMetadata/metadata/gbif/livingTimePeriod", VerbatimTimePeriod.class);
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/livingTimePeriod", "period");
    digester.addCallMethod("eml/additionalMetadata/metadata/gbif/livingTimePeriod", "setType", 1,
      new Class[] {VerbatimTimePeriodType.class});
    digester.addObjectParam("eml/additionalMetadata/metadata/gbif/livingTimePeriod", 0,
      VerbatimTimePeriodType.LIVING_TIME_PERIOD);
    digester.addSetNext("eml/additionalMetadata/metadata/gbif/livingTimePeriod", "addTemporalCoverage");

    digester.addObjectCreate("eml/additionalMetadata/metadata/gbif/formationPeriod", VerbatimTimePeriod.class);
    digester.addBeanPropertySetter("eml/additionalMetadata/metadata/gbif/formationPeriod", "period");
    digester.addCallMethod("eml/additionalMetadata/metadata/gbif/formationPeriod", "setType", 1,
      new Class[] {VerbatimTimePeriodType.class});
    digester.addObjectParam("eml/additionalMetadata/metadata/gbif/formationPeriod", 0,
      VerbatimTimePeriodType.FORMATION_PERIOD);
    digester.addSetNext("eml/additionalMetadata/metadata/gbif/formationPeriod", "addTemporalCoverage");
  }

}