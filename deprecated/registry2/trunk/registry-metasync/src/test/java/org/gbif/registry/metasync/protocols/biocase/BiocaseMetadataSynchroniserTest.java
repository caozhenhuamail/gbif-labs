/*
 * Copyright 2013 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.registry.metasync.protocols.biocase;

import org.gbif.api.model.registry.Dataset;
import org.gbif.api.model.registry.Endpoint;
import org.gbif.api.model.registry.Installation;
import org.gbif.api.vocabulary.EndpointType;
import org.gbif.api.vocabulary.InstallationType;
import org.gbif.registry.metasync.api.SyncResult;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BiocaseMetadataSynchroniserTest {

  @Mock
  private HttpClient client;
  private BiocaseMetadataSynchroniser synchroniser;
  private Installation installation;

  @Before
  public void setup() {
    synchroniser = new BiocaseMetadataSynchroniser(client);
    installation = new Installation();
    installation.setType(InstallationType.BIOCASE_INSTALLATION);
    Endpoint endpoint = new Endpoint();
    endpoint.setUrl(URI.create("http://localhost"));
    installation.addEndpoint(endpoint);
  }

  @Test
  public void testCanHandle() {
    installation.setType(InstallationType.DIGIR_INSTALLATION);
    assertThat(synchroniser.canHandle(installation)).isFalse();

    installation.setType(InstallationType.BIOCASE_INSTALLATION);
    assertThat(synchroniser.canHandle(installation)).isTrue();
  }

  /**
   * This tests a BioCASe endpoint that supports the old style inventory and ABCD 2.06
   */
  @Test
  public void testAddedDataset1() throws Exception {
    when(client.execute(any(HttpGet.class))).thenReturn(prepareResponse(200, "biocase/capabilities1.xml"))
      .thenReturn(prepareResponse(200, "biocase/inventory1.xml"))
      .thenReturn(prepareResponse(200, "biocase/dataset1.xml"));
    SyncResult syncResult = synchroniser.syncInstallation(installation, new ArrayList<Dataset>());
    assertThat(syncResult.exception).isNull();
    assertThat(syncResult.deletedDatasets).isEmpty();
    assertThat(syncResult.existingDatasets).isEmpty();
    assertThat(syncResult.addedDatasets).hasSize(1);

    Dataset dataset = syncResult.addedDatasets.get(0);
    assertThat(dataset.getTitle()).isEqualTo("Pontaurus");
    assertThat(dataset.getCitation().getText()).isEqualTo("All credit to Markus Doring");
    // endpoints
    assertThat(dataset.getEndpoints().size()).isEqualTo(1);
    assertThat(dataset.getEndpoints().get(0).getType()).isEqualTo(EndpointType.BIOCASE);
  }

  /**
   * This tests a BioCASe endpoint that supports the new style inventory and ABCD 2.06
   */
  @Test
  public void testAddedDataset2() throws Exception {
    when(client.execute(any(HttpGet.class))).thenReturn(prepareResponse(200, "biocase/capabilities2.xml"))
      .thenReturn(prepareResponse(200, "biocase/inventory2.xml"))
      .thenReturn(prepareResponse(200, "biocase/dataset2.xml"));
    SyncResult syncResult = synchroniser.syncInstallation(installation, new ArrayList<Dataset>());
    assertThat(syncResult.exception).isNull();
    assertThat(syncResult.deletedDatasets).isEmpty();
    assertThat(syncResult.existingDatasets).isEmpty();
    assertThat(syncResult.addedDatasets).hasSize(1);

    Dataset dataset = syncResult.addedDatasets.get(0);
    assertThat(dataset.getTitle()).isEqualTo("Collections of Phytoplankton at BGBM");
    assertThat(dataset.getCitation().getText()).isEqualTo(
      "Jahn, R. (Ed.) 2013+ (continuously updated): Collections of Phytoplankton at BGBM");
    // endpoints
    assertThat(dataset.getEndpoints().size()).isEqualTo(1);
    assertThat(dataset.getEndpoints().get(0).getType()).isEqualTo(EndpointType.BIOCASE);
  }

  /**
   * This tests a BioCASe endpoint that supports the old style inventory and ABCD 1.2
   */
  @Test
  public void testAddedDataset3() throws Exception {
    when(client.execute(any(HttpGet.class))).thenReturn(prepareResponse(200, "biocase/capabilities3.xml"))
      .thenReturn(prepareResponse(200, "biocase/inventory3.xml"))
      .thenReturn(prepareResponse(200, "biocase/dataset3.xml"));
    SyncResult syncResult = synchroniser.syncInstallation(installation, new ArrayList<Dataset>());
    assertThat(syncResult.exception).isNull();
    assertThat(syncResult.deletedDatasets).isEmpty();
    assertThat(syncResult.existingDatasets).isEmpty();
    assertThat(syncResult.addedDatasets).hasSize(4);

    Dataset dataset = syncResult.addedDatasets.get(0);
    assertThat(dataset.getTitle()).isEqualTo("Mammals housed at MHNG, Geneva");
    assertThat(dataset.getCitation().getText()).isEqualTo(
      "Ruedi M. Mammals housed at MHNG, Geneva. Muséum d'histoire naturelle de la Ville de Genève");
    // endpoints
    assertThat(dataset.getEndpoints().size()).isEqualTo(1);
    assertThat(dataset.getEndpoints().get(0).getType()).isEqualTo(EndpointType.BIOCASE);
  }

  @Test
  public void testDeletedDataset() throws Exception {
    Dataset dataset = new Dataset();
    dataset.setTitle("Foobar");

    when(client.execute(any(HttpGet.class))).thenReturn(prepareResponse(200, "biocase/capabilities1.xml"))
      .thenReturn(prepareResponse(200, "biocase/inventory1.xml"))
      .thenReturn(prepareResponse(200, "biocase/dataset1.xml"));
    SyncResult syncResult = synchroniser.syncInstallation(installation, Lists.newArrayList(dataset));
    assertThat(syncResult.deletedDatasets).hasSize(1);
    assertThat(syncResult.existingDatasets).isEmpty();
    assertThat(syncResult.addedDatasets).hasSize(1);

    assertThat(syncResult.deletedDatasets.get(0).getTitle()).isEqualTo("Foobar");
  }

  @Test
  public void testUpdatedDataset() throws Exception {
    Dataset dataset = new Dataset();
    dataset.setTitle("Pontaurus");

    when(client.execute(any(HttpGet.class))).thenReturn(prepareResponse(200, "biocase/capabilities1.xml"))
      .thenReturn(prepareResponse(200, "biocase/inventory1.xml"))
      .thenReturn(prepareResponse(200, "biocase/dataset1.xml"));
    SyncResult syncResult = synchroniser.syncInstallation(installation, Lists.newArrayList(dataset));
    assertThat(syncResult.deletedDatasets).describedAs("Deleted datasets").isEmpty();
    assertThat(syncResult.existingDatasets).hasSize(1);
    assertThat(syncResult.addedDatasets).isEmpty();

    assertThat(syncResult.existingDatasets.get(dataset).getTitle()).isEqualTo("Pontaurus");
  }

  /**
   * Prepares a {@link HttpResponse} with the given response status and the content of the file.
   */
  private HttpResponse prepareResponse(int responseStatus, String fileName) throws IOException {
    HttpResponse response =
      new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), responseStatus, ""));
    response.setStatusCode(responseStatus);
    byte[] bytes = Resources.toByteArray(Resources.getResource(fileName));
    response.setEntity(new ByteArrayEntity(bytes));
    return response;
  }

}
