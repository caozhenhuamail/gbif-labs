package org.gbif.api.registry.service;

import org.gbif.api.registry.model.Identifier;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

public interface IdentifierService {

  int addIdentifier(@NotNull UUID targetEntityKey, Identifier identifier);

  void deleteIdentifier(@NotNull UUID targetEntityKey, @NotNull int identifierKey);

  List<Identifier> listIdentifiers(@NotNull UUID targetEntityKey);
}