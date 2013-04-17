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
package org.gbif.registry.todo;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.Min;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.bval.guice.Validate;
import org.apache.bval.guice.ValidationModule;


public class ValidationTest3 {

  static class O {

    @Min(value = 0)
    private int i;

    public int getI() {
      return i;
    }

    public void setI(int i) {
      this.i = i;
    }
  }

  public static void main(String[] args) {
    Module m = new AbstractModule() {

      @Override
      protected void configure() {
        bind(Test.class);
      }
    };
    Injector i = Guice.createInjector(m, new ValidationModule());
    O o = new O();
    o.i = -10;
    O o1 = new O();
    o1.i = 10;
    try {
      i.getInstance(Test.class).print(o, o1);
    } catch (ConstraintViolationException e) {
      e.printStackTrace();
      for (ConstraintViolation<?> cv : e.getConstraintViolations()) {
        System.out.println(cv.getPropertyPath());
        System.out.println(cv.getMessage());
        System.out.println(cv.getMessageTemplate());
      }
    }
  }


  static class Test {

    @Validate
    public void print(@Valid O o, O o1) {
      System.out.println(o.i);
      System.out.println(o1.i);
    }

  }
}
