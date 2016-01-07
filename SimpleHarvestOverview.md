# Overview #

simple-harvest provides a simple way to harvest data from GBIF Publishers whatever protocol or software they use.


# Context #
GBIF France needs to develop its own national portal. The goal of this portal will be to allow the users to have a more detailled view of the data that has been published to GBIF.
One of the main features of the portal will be to publish all concepts mapped during the data connection process and that are not showed at an international level.
To answer this need, a harvesting tool has been developed: simple-harvest

# Description #
simple-harvest is a tool that has been firstly developed with the help of Tim Robertson from the Secretariat.
It’s actually a part of the HIT, simplified at extreme to be easily customisable for own purpose.
This project will not only be use by the GBIF France portal but also be a lab project which will help the Secretariat to evaluate his new ‘decoupling components’ policy (http://gbif.blogspot.com/2011/05/decoupling-components.html).
Thus, the crawling/harvesting process will become independant of others aspects of the HIT.

## Process ##
The tool takes 6 arguments:
  1. Dataset Number: 1
  1. URL: http://ww3.bgbm.org/biocase/pywrapper.cgi?dsa=CameroonHerbarium?
  1. Database: jdbc:mysql://localhost/france?useUnicode=yes&characterEncoding=UTF-8
  1. Database login: root
  1. Database password: ""
  1. Temp folder: /tmp/


In a very simplified view, the general process is:
  1. Connection to a dataset URL
  1. Sending of a request ("send me all your data")
  1. Retrieving of the software response
  1. Parsing process (depends of the standard and software that are used)
  1. Data backup in a database