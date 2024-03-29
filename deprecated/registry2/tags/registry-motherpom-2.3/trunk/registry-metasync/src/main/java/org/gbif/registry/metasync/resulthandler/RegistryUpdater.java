package org.gbif.registry.metasync.resulthandler;

import org.gbif.api.model.registry.Contact;
import org.gbif.api.model.registry.Dataset;
import org.gbif.api.model.registry.Endpoint;
import org.gbif.api.model.registry.Identifier;
import org.gbif.api.model.registry.MachineTag;
import org.gbif.api.service.registry.DatasetService;
import org.gbif.api.service.registry.MetasyncHistoryService;
import org.gbif.api.vocabulary.DatasetType;
import org.gbif.api.vocabulary.EndpointType;
import org.gbif.registry.metasync.api.SyncResult;
import org.gbif.registry.metasync.util.Constants;

import java.util.Map;
import java.util.UUID;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes synchronisation results and saves those back to the registry.
 */
public class RegistryUpdater {

  private static final Logger LOG = LoggerFactory.getLogger(RegistryUpdater.class);
  private final DatasetService datasetService;
  private final MetasyncHistoryService historyService;

  public RegistryUpdater(DatasetService datasetService, MetasyncHistoryService historyService) {
    this.datasetService = datasetService;
    this.historyService = historyService;
  }

  @VisibleForTesting
  public DatasetService getDatasetService() {
    return datasetService;
  }

  public void saveSyncResultsToRegistry(Iterable<SyncResult> syncResults) {
    for (SyncResult syncResult : syncResults) {
      saveSyncResults(syncResult);
    }
  }

  /**
   * Iterate through the updated datasets. For each dataset, update the dataset itself, and then update its contacts,
   * identifiers, machine tags, and endpoints. Tags remain unchanged by the metadata synchronizer, and therefore aren't
   * updated. The following assumptions apply:
   * </br>
   * Contacts: are derived 100% from the metadata response.
   * </br>
   * Machine tags: with metasync.gbif.org namespace are derived 100% from metadata synchronizer
   * </br>
   * Endpoints: only 1 endpoint exists per dataset.
   * </br>
   * Identifiers:
   * 
   * @param result SyncResult
   */
  void saveUpdatedDatasets(SyncResult result) {
    for (Map.Entry<Dataset, Dataset> entry : result.existingDatasets.entrySet()) {
      Dataset existingDataset = entry.getKey();
      UUID datasetKey = existingDataset.getKey();
      if (skipDatasetUpdate(existingDataset)) {
        LOG.info("Dataset [{}] updated at source untouched in Registry because it's locked or contains a DwC-A "
          + "endpoint, which takes precedence", datasetKey);
      } else {
        LOG.info("Updating dataset [{}]", datasetKey);

        // the updated dataset created from the metadata synchronization
        Dataset updated = entry.getValue();

        // safest, preserve existing dataset, copying over only those properties that could have changed from the sync
        existingDataset.setHomepage(updated.getHomepage());
        existingDataset.setLanguage(updated.getLanguage());
        existingDataset.setCitation(updated.getCitation());
        existingDataset.setTitle(updated.getTitle());
        existingDataset.setDescription(updated.getDescription());
        existingDataset.setLogoUrl(updated.getLogoUrl());
        existingDataset.setRights(updated.getRights());
        // perform update
        datasetService.update(existingDataset);

        // Contacts are derived 100% from the metadata
        // delete existing contacts, and replace with new/updated contacts
        for (Contact contact : existingDataset.getContacts()) {
          datasetService.deleteContact(datasetKey, contact.getKey());
        }
        for (Contact contact : updated.getContacts()) {
          datasetService.addContact(datasetKey, contact);
        }

        // Machine tags with namepace "metasync.gbif.org" are derived 100% from the metadata sync
        // delete existing machine tags in this namespace, and replace with new/updated machine tags
        for (MachineTag machineTag : existingDataset.getMachineTags()) {
          // TODO: replace below with datasetService.deleteMachineTags(uuid, namespace); once implemented
          if (machineTag.getNamespace().equalsIgnoreCase(Constants.METADATA_NAMESPACE)) {
            datasetService.deleteMachineTag(datasetKey, machineTag.getKey());
          }
        }
        for (MachineTag machineTag : updated.getMachineTags()) {
          datasetService.addMachineTag(datasetKey, machineTag);
        }

        // Only 1 endpoint exists per Dataset
        // delete existing endpoint, and replace with new/updated endpoint
        for (Endpoint endpoint : existingDataset.getEndpoints()) {
          datasetService.deleteEndpoint(datasetKey, endpoint.getKey());
        }
        for (Endpoint endpoint : updated.getEndpoints()) {
          datasetService.addEndpoint(datasetKey, endpoint);
        }

        // Only add identifiers that don't yet exist
        // Identifiers like GBIF Portal ID aren't created during metadata sync, so never delete these
        if (!updated.getIdentifiers().isEmpty()) {
          for (Identifier identifier : updated.getIdentifiers()) {
            // does this identifier exist already?
            boolean found = false;
            for (Identifier existingIdentifier : existingDataset.getIdentifiers()) {
              if (identifier.lenientEquals(existingIdentifier)) {
                found = true;
                break;
              }
            }
            if (!found) {
              datasetService.addIdentifier(datasetKey, identifier);
            }
          }
        }
      }
    }
  }

  /**
   * Processes the result of a synchronisation by:
   * <ul>
   * <li>Creating new Datasets and Endpoints</li>
   * <li>Deleting existing Datasets</li>
   * <li>Updating existing Installations, Datasets and Endpoints</li>
   * </ul>
   */
  private void saveSyncResults(SyncResult result) {
    if (result.exception == null) {
      saveAddedDatasets(result);
      saveDeletedDatasets(result);
      saveUpdatedDatasets(result);
    } else {
      LOG.warn("Installation [{}] failed sync because of [{}]",
        result.installation.getKey(),
        result.exception.getMessage());
    }
    historyService.createMetasync(result.buildHistory());

  }

  /**
   * Check if the dataset update should be skipped. Currently, there are 2 reasons for skipping:
   * 1) The dataset has been locked for automatic updates
   * 2) The dataset has been migrated to DwC-A, in other words, it has an endpoint of type Darwin Core.
   * 
   * @param existingDataset existing dataset (dataset as it currently exists in registry)
   * @return true if the dataset update should be skipped, false otherwise
   */
  private boolean skipDatasetUpdate(Dataset existingDataset) {
    // is the dataset locked for auto update?
    if (existingDataset.isLockedForAutoUpdate()) {
      return true;
    }
    // is the dataset migrated to DwC-A?
    for (Endpoint endpoint : existingDataset.getEndpoints()) {
      if (endpoint.getType() == EndpointType.DWC_ARCHIVE) {
        return true;
      }
    }
    // otherwise, proceed with update
    return false;
  }

  private void saveDeletedDatasets(SyncResult result) {
    for (Dataset dataset : result.deletedDatasets) {
      if (dataset.isLockedForAutoUpdate()) {
        LOG.info("Dataset [{}] deleted at source but left in Registry because it's locked", dataset.getKey());
      } else {
        LOG.info("Deleting dataset [{}]", dataset.getKey());
        datasetService.delete(dataset.getKey());
      }
    }
  }

  private void saveAddedDatasets(SyncResult result) {
    for (Dataset dataset : result.addedDatasets) {
      dataset.setOwningOrganizationKey(result.installation.getOrganizationKey());
      dataset.setInstallationKey(result.installation.getKey());
      dataset.setType(DatasetType.OCCURRENCE);

      UUID uuid = datasetService.create(dataset);
      LOG.info("Created new Dataset with id [{}]", uuid);
      for (Contact contact : dataset.getContacts()) {
        datasetService.addContact(uuid, contact);
      }
      for (MachineTag machineTag : dataset.getMachineTags()) {
        datasetService.addMachineTag(uuid, machineTag);
      }
      for (Endpoint endpoint : dataset.getEndpoints()) {
        datasetService.addEndpoint(uuid, endpoint);
      }
      for (Identifier identifier : dataset.getIdentifiers()) {
        datasetService.addIdentifier(uuid, identifier);
      }
    }
  }

}
