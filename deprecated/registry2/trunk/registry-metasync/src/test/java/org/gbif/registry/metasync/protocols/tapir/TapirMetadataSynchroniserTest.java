package org.gbif.registry.metasync.protocols.tapir;

import org.gbif.api.model.registry.Dataset;
import org.gbif.api.model.registry.Endpoint;
import org.gbif.api.model.registry.Installation;
import org.gbif.api.model.registry.MachineTag;
import org.gbif.api.vocabulary.InstallationType;
import org.gbif.registry.metasync.api.SyncResult;
import org.gbif.registry.metasync.protocols.HttpGetMatcher;
import org.gbif.registry.metasync.util.Constants;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TapirMetadataSynchroniserTest {

  @Mock
  private HttpClient client;
  private TapirMetadataSynchroniser synchroniser;
  private Installation installation;

  @Before
  public void setup() {
    synchroniser = new TapirMetadataSynchroniser(client);

    installation = new Installation();
    installation.setType(InstallationType.TAPIR_INSTALLATION);
    Endpoint endpoint = new Endpoint();
    endpoint.setUrl(URI.create("http://localhost/nmr"));
    installation.addEndpoint(endpoint);
  }

  @Test
  public void testCanHandle() {
    installation.setType(InstallationType.BIOCASE_INSTALLATION);
    assertThat(synchroniser.canHandle(installation)).isFalse();

    installation.setType(InstallationType.TAPIR_INSTALLATION);
    assertThat(synchroniser.canHandle(installation)).isTrue();
  }

  /**
   * A simple test to see if multiple datasets are parsed successfully.
   */
  @Test
  public void testAddedDatasets() throws Exception {
    when(client.execute(argThat(HttpGetMatcher.matchUrl("http://localhost/nmr?op=capabilities")))).thenReturn(
      prepareResponse(200, "tapir/capabilities1.xml"));
    when(client.execute(argThat(HttpGetMatcher.matchUrl("http://localhost/nmr")))).thenReturn(
      prepareResponse(200, "tapir/metadata1.xml"));
    when(
      client.execute(argThat(HttpGetMatcher
        .matchUrl("http://localhost/nmr?op=s&t=http%3A%2F%2Frs.gbif.org%2Ftemplates%2Ftapir%2Fdwc%2F1.4%2Fsci_name_range.xml&count=true&start=0&limit=1&lower=AAA&upper=zzz"))))
      .thenReturn(
        prepareResponse(200, "tapir/search1.xml"));
    SyncResult syncResult = synchroniser.syncInstallation(installation, new ArrayList<Dataset>());
    assertThat(syncResult.deletedDatasets).isEmpty();
    assertThat(syncResult.existingDatasets).isEmpty();
    assertThat(syncResult.addedDatasets).hasSize(1);
    assertThat(syncResult.addedDatasets.get(0).getContacts()).hasSize(2);
    assertThat(syncResult.addedDatasets.get(0).getMachineTags()).hasSize(2);

    // Assert the declared record count machine tag was found, and that its value was 167348
    MachineTag count = null;
    for (MachineTag tag : syncResult.addedDatasets.get(0).getMachineTags()) {
      if (tag.getName().equalsIgnoreCase(Constants.DECLARED_COUNT)) {
        count = tag;
      }
    }
    assertThat(count).isNotNull();
    assertThat(Integer.valueOf(count.getValue())).isEqualTo(167348);
  }

  @Test
  public void testDeletedDataset() throws Exception {
    Dataset dataset = new Dataset();
    dataset.setTitle("Foobar");

    when(client.execute(argThat(HttpGetMatcher.matchUrl("http://localhost/nmr?op=capabilities")))).thenReturn(
      prepareResponse(200, "tapir/capabilities1.xml"));
    when(client.execute(argThat(HttpGetMatcher.matchUrl("http://localhost/nmr")))).thenReturn(
      prepareResponse(200, "tapir/metadata1.xml"));
    when(
      client.execute(argThat(HttpGetMatcher
        .matchUrl("http://localhost/nmr?op=s&t=http%3A%2F%2Frs.gbif.org%2Ftemplates%2Ftapir%2Fdwc%2F1.4%2Fsci_name_range.xml&count=true&start=0&limit=1&lower=AAA&upper=zzz"))))
      .thenReturn(
        prepareResponse(200, "tapir/search1.xml"));
    SyncResult syncResult = synchroniser.syncInstallation(installation, Lists.newArrayList(dataset));
    assertThat(syncResult.deletedDatasets).hasSize(1);
    assertThat(syncResult.existingDatasets).isEmpty();
    assertThat(syncResult.addedDatasets).hasSize(1);

    assertThat(syncResult.deletedDatasets.get(0).getTitle()).isEqualTo("Foobar");
  }

  @Test
  public void testUpdatedDataset() throws Exception {
    Dataset dataset = new Dataset();
    dataset.setTitle("Foobar");
    Endpoint endpoint = new Endpoint();
    endpoint.setUrl(URI.create("http://localhost/nmr"));
    dataset.addEndpoint(endpoint);

    when(client.execute(argThat(HttpGetMatcher.matchUrl("http://localhost/nmr?op=capabilities")))).thenReturn(
      prepareResponse(200, "tapir/capabilities1.xml"));
    when(client.execute(argThat(HttpGetMatcher.matchUrl("http://localhost/nmr")))).thenReturn(
      prepareResponse(200, "tapir/metadata1.xml"));
    when(
      client.execute(argThat(HttpGetMatcher
        .matchUrl("http://localhost/nmr?op=s&t=http%3A%2F%2Frs.gbif.org%2Ftemplates%2Ftapir%2Fdwc%2F1.4%2Fsci_name_range.xml&count=true&start=0&limit=1&lower=AAA&upper=zzz"))))
      .thenReturn(
        prepareResponse(200, "tapir/search1.xml"));

    SyncResult syncResult = synchroniser.syncInstallation(installation, Lists.newArrayList(dataset));
    assertThat(syncResult.deletedDatasets).describedAs("Deleted datasets").isEmpty();
    assertThat(syncResult.existingDatasets).hasSize(1);
    assertThat(syncResult.addedDatasets).isEmpty();

    assertThat(syncResult.existingDatasets
      .get(dataset)
      .getTitle()).isEqualTo("Natural History Museum Rotterdam");
  }

  public HttpResponse prepareResponse(int responseStatus, String fileName) throws IOException {
    HttpResponse response =
      new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), responseStatus, ""));
    response.setStatusCode(responseStatus);
    byte[] bytes = Resources.toByteArray(Resources.getResource(fileName));
    response.setEntity(new ByteArrayEntity(bytes));
    return response;
  }
}
