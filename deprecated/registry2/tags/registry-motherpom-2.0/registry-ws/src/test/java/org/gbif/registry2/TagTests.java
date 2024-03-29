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
package org.gbif.registry2;

import org.gbif.api.model.registry2.NetworkEntity;
import org.gbif.api.model.registry2.Tag;
import org.gbif.api.service.registry2.TagService;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TagTests {

  public static <T extends NetworkEntity> void testTagErroneousDelete(TagService service, T entity) {
    int tagKey = service.addTag(entity.getKey(), "tag1");
    service.deleteTag(UUID.randomUUID(), tagKey); // wrong parent UUID
    // nothing happens - expected?
  }

  public static <T extends NetworkEntity> void testAddDelete(TagService service, T entity) {
    List<Tag> tags = service.listTags(entity.getKey(), null);
    assertNotNull("Tag list should be empty, not null when no tags exist", tags);
    assertTrue("Tags should be empty when none added", tags.isEmpty());
    service.addTag(entity.getKey(), "tag1");
    service.addTag(entity.getKey(), "tag2");
    tags = service.listTags(entity.getKey(), null);
    assertNotNull(tags);
    assertEquals("2 tags have been added", 2, tags.size());
    service.deleteTag(entity.getKey(), tags.get(0).getKey());
    tags = service.listTags(entity.getKey(), null);
    assertNotNull(tags);
    assertEquals("1 tag should remain after the deletion", 1, tags.size());
  }

}
