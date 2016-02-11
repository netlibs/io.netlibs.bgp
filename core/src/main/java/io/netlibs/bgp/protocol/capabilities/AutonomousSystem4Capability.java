/**
 *  Copyright 2012 Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package io.netlibs.bgp.protocol.capabilities;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class AutonomousSystem4Capability extends Capability
{

  private int autonomousSystem;

  public AutonomousSystem4Capability()
  {
  }

  public AutonomousSystem4Capability(final int autonomousSystem)
  {
    this.autonomousSystem = autonomousSystem;
  }

  /**
   * @return the autonomousSystem
   */
  public int getAutonomousSystem()
  {
    return this.autonomousSystem;
  }

  /**
   * @param autonomousSystem
   *          the autonomousSystem to set
   */
  public void setAutonomousSystem(final int autonomousSystem)
  {
    this.autonomousSystem = autonomousSystem;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.net.Capability#orderNumber()
   */
  @Override
  protected int orderNumber()
  {
    return ORDER_NUMBER_AS4_CAPABILITY;
  }

  @Override
  protected boolean equalsSubclass(final Capability other)
  {
    return (this.getAutonomousSystem() == ((AutonomousSystem4Capability) other).getAutonomousSystem());
  }

  @Override
  protected int hashCodeSubclass()
  {
    return (new HashCodeBuilder()).append(this.getAutonomousSystem()).toHashCode();
  }

  @Override
  protected int compareToSubclass(final Capability other)
  {
    return (new CompareToBuilder())
        .append(this.getAutonomousSystem(), ((AutonomousSystem4Capability) other).getAutonomousSystem())
        .toComparison();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return ToStringBuilder.reflectionToString(this);
  }
}
