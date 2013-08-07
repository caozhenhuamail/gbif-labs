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
package org.gbif.registry.events;

import org.gbif.common.messaging.guice.PostalServiceModule;

import java.util.Properties;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;

/**
 * One can either extend and bind listeners in this class, or bind them in other modules.
 * Note that you MUST start the listener. Often the easiest way of doing this is simply to bind it as an eager singleton
 * in guice.
 */
public class EventModule extends AbstractModule {

  public static final String MESSAGING_ENABLED_PROPERTY = "registry.postalservice.enabled";
  private final EventBus eventBus = new EventBus();
  private final Properties properties;

  public EventModule(Properties properties) {
    this.properties = properties;
  }

  @Override
  protected void configure() {
    bind(EventBus.class).toInstance(eventBus);
    bindEventListeners();
  }

  /**
   * Bind Event listeners here as eager Singletons.
   */
  private void bindEventListeners() {
    if (properties.getProperty(MESSAGING_ENABLED_PROPERTY, "false").equals("true")) {
      install(new PostalServiceModule("registry", properties));
      bind(MessageSendingEventListener.class).asEagerSingleton();
    }
  }

}
