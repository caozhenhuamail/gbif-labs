package org.gbif.registry.persistence.mapper;

import org.gbif.api.registry.model.Comment;

import java.util.List;
import java.util.UUID;

import org.apache.ibatis.annotations.Param;

public interface CommentableMapper {

  int addComment(@Param("targetEntityKey") UUID entityKey, @Param("commentKey") int commentKey);

  int deleteComment(@Param("targetEntityKey") UUID entityKey, @Param("commentKey") int commentKey);

  List<Comment> listComments(@Param("targetEntityKey") UUID identifierKey);

}
