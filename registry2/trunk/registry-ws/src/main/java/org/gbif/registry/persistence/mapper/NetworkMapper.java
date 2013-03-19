package org.gbif.registry.persistence.mapper;

import org.gbif.api.registry.model.Network;

public interface NetworkMapper extends NetworkEntityMapper<Network>, ContactableMapper, EndpointableMapper,
  MachineTaggableMapper, TaggableMapper, CommentableMapper {

}