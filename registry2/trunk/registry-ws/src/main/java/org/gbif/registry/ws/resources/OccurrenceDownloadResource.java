package org.gbif.registry.ws.resources;

import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.occurrence.Download;
import org.gbif.api.model.registry.PrePersist;
import org.gbif.api.service.registry.OccurrenceDownloadService;
import org.gbif.registry.persistence.mapper.DatasetOccurrenceDownloadMapper;
import org.gbif.registry.persistence.mapper.OccurrenceDownloadMapper;
import org.gbif.registry.ws.guice.Trim;
import org.gbif.ws.server.interceptor.NullToNotFound;
import org.gbif.ws.util.ExtraMediaTypes;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.bval.guice.Validate;
import org.mybatis.guice.transactional.Transactional;

/**
 * Occurrence download resource/web service.
 */
@Singleton
@Path("occurrence/download")
@Produces({MediaType.APPLICATION_JSON, ExtraMediaTypes.APPLICATION_JAVASCRIPT})
@Consumes(MediaType.APPLICATION_JSON)
public class OccurrenceDownloadResource implements OccurrenceDownloadService {

  private final OccurrenceDownloadMapper occurrenceDownloadMapper;
  private static final String ADMIN_ROLE = "ADMIN";

  @Context
  private SecurityContext securityContext;

  @Inject
  public OccurrenceDownloadResource(OccurrenceDownloadMapper occurrenceDownloadMapper) {
    this.occurrenceDownloadMapper = occurrenceDownloadMapper;
  }


  /**
   * Lists all the downloads. This operation can be executed by role ADMIN only.
   */
  @GET
  @RolesAllowed(ADMIN_ROLE)
  @Override
  public PagingResponse<Download> list(@Context Pageable page) {
    return new PagingResponse<Download>(page, (long) occurrenceDownloadMapper.count(),
      occurrenceDownloadMapper.list(page));
  }

  @GET
  @Path("{key}")
  @Nullable
  @NullToNotFound
  @Override
  public Download get(@PathParam("key") String key) {
    return occurrenceDownloadMapper.get(key);
  }

  @PUT
  @Path("{key}")
  @Transactional
  @RolesAllowed(ADMIN_ROLE)
  @Override
  public void updateStatus(@NotNull @PathParam("key") String downloadKey, @NotNull Download.Status status) {
    occurrenceDownloadMapper.updateStatus(downloadKey, status);
  }

  @POST
  @Trim
  @Transactional
  @Validate(groups = {PrePersist.class, Default.class})
  @RolesAllowed(ADMIN_ROLE)
  @Override
  public void create(@Valid @NotNull @Trim Download occurrenceDownload) {
    occurrenceDownloadMapper.create(occurrenceDownload);
  }

  /**
   * Lists the downloads of the current user.
   */
  @GET
  @Path("user")
  @NullToNotFound
  public PagingResponse<Download> listMyDownloads(@Context Pageable page) {
    return listByUser(securityContext.getUserPrincipal().getName(), page);
  }


  @GET
  @Path("user/{user}")
  @NullToNotFound
  public PagingResponse<Download> listByUser(@PathParam("user") String user, @Context Pageable page) {
    // A null securityContext means that the class is executed locally
    if (securityContext == null || securityContext.isUserInRole(ADMIN_ROLE)
      || securityContext.getUserPrincipal().getName().equals(user)) {
      return new PagingResponse<Download>(page, (long) occurrenceDownloadMapper.countByUser(user),
        occurrenceDownloadMapper.listByUser(user, page));
    }
    throw new WebApplicationException(Response.Status.UNAUTHORIZED);

  }

}
