package org.gbif.registry.ws.resources.rest;

import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.registry.service.NetworkEntityService;
import org.gbif.registry.ws.guice.Trim;
import org.gbif.ws.annotation.NullForNotFound;

import java.util.UUID;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import org.apache.bval.guice.Validate;
import org.mybatis.guice.transactional.Transactional;

public interface NetworkEntityRest<T> extends NetworkEntityService<T> {

  @POST
  @Validate
  @Transactional
  @Override
  public UUID create(@NotNull @Valid @Trim T entity);

  @GET
  @Path("{key}")
  @NullForNotFound
  @Override
  public T get(@PathParam("key") UUID key);

  /**
   * Method exists only to allow validation that the path key equals the entity key.
   */
  @PUT
  @Path("{key}")
  @Validate
  public void update(@PathParam("key") UUID key, @NotNull @Valid @Trim T entity);

  @Validate
  @Transactional
  @Override
  public void update(@Valid @Trim T entity);

  @DELETE
  @Path("{key}")
  @Transactional
  @Override
  public void delete(@PathParam("key") UUID key);

  @GET
  @Override
  public PagingResponse<T> list(@Nullable @Context Pageable page);
}
