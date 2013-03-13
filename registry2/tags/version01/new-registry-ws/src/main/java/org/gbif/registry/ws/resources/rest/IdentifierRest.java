package org.gbif.registry.ws.resources.rest;

import org.gbif.api.registry.model.Identifier;
import org.gbif.api.registry.service.IdentifierService;
import org.gbif.registry.ws.guice.Trim;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.bval.guice.Validate;
import org.mybatis.guice.transactional.Transactional;

public interface IdentifierRest extends IdentifierService {

  @POST
  @Path("{key}/identifier")
  @Validate
  @Transactional
  @Override
  int addIdentifier(@PathParam("key") UUID targetEntityKey, @NotNull @Valid @Trim Identifier identifier);

  @DELETE
  @Path("{key}/identifier/{identifierKey}")
  @Override
  void deleteIdentifier(@PathParam("key") UUID targetEntityKey, @PathParam("identifierKey") int identifierKey);

  @GET
  @Path("{key}/identifier")
  @Override
  List<Identifier> listIdentifiers(@PathParam("key") UUID targetEntityKey);
}