CREATE TABLE occurrence (
  id mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  dataset_id tinyint(3) unsigned NOT NULL,
  occurrence_id varchar(10),
  institution_code varchar(100) NOT NULL,
  collection_id varchar(100),
  collection_code varchar(100) NOT NULL,
  catalogue_number varchar(100) NOT NULL,
  sex varchar(50),
  kingdom varchar(100),
  phylum varchar(100),
  klass varchar(100),
  taxorder varchar(100),
  family varchar(100),
  genus varchar(100),
  subgenus varchar(100),
  specific_epithet varchar(100),
  infraspecific_epithet varchar(100),
  scientific_name varchar(255) NOT NULL,
  scientific_name_authorship varchar(255),
  taxon_rank varchar(100),
  date_identified varchar(100),
  identified_by varchar(100),
  type_status varchar(100),
  continent varchar(100),
  water_body varchar(100),
  country varchar(100),
  state_province varchar(100),
  locality varchar(1000),
  decimal_latitude varchar(100),
  decimal_longitude varchar(100),
  coordinate_precision varchar(100),
  minimum_elevation_in_meters varchar(100),
  maximum_elevation_in_meters varchar(100),
  minimum_depth_in_meters varchar(100),
  maximum_depth_in_meters varchar(100),
  status varchar(10),
  PRIMARY KEY (id)
) ENGINE=MyISAM AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8;
