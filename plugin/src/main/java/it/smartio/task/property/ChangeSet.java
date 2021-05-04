/*
 * Copyright (c) 2001-2021 Territorium Online Srl / TOL GmbH. All Rights Reserved.
 *
 * This file contains Original Code and/or Modifications of Original Code as defined in and that are
 * subject to the Territorium Online License Version 1.0. You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at http://www.tol.info/license/
 * and read it before using this file.
 *
 * The Original Code and all software distributed under the License are distributed on an 'AS IS'
 * basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND TERRITORIUM ONLINE HEREBY
 * DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT. Please see the License for
 * the specific language governing rights and limitations under the License.
 */

package it.smartio.task.property;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The {@link ChangeSet} class.
 */
class ChangeSet {

  private final Set<ChangeSet.Change> changeset = new HashSet<>();

  public final boolean isEmpty() {
    return this.changeset.isEmpty();
  }

  public final void add(String property, String valueOld, String valueNew) {
    this.changeset.add(new Change(property, valueOld, valueNew));
  }

  public final void forEach(Consumer<ChangeSet.Change> action) {
    this.changeset.forEach(action);
  }

  class Change {

    public final String PROPERTY;
    public final String OLD_VALUE;
    public final String NEW_VALUE;

    /**
     * Constructs an instance of {@link ChangeSet}.
     *
     * @param property
     * @param valueOld
     * @param valueNew
     */
    private Change(String property, String valueOld, String valueNew) {
      this.PROPERTY = property;
      this.OLD_VALUE = valueOld;
      this.NEW_VALUE = valueNew;
    }

    /**
     * Returns a hash code value for the object.
     */
    @Override
    public int hashCode() {
      return Objects.hash(this.PROPERTY);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if ((obj == null) || !(obj instanceof Change)) {
        return false;
      }
      Change other = (Change) obj;
      return Objects.equals(this.PROPERTY, other.PROPERTY);
    }
  }
}
