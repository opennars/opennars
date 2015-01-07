/**
 * L2FProd Common v9.2 License.
 *
 * Copyright 2005 - 2009 L2FProd.com
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
package automenta.vivisect.swing.property.beans;

import java.beans.BeanDescriptor;
import java.util.MissingResourceException;




/**
 * Default bean descriptor.
 */
public class DefaultBeanDescriptor extends BeanDescriptor {

	private String displayName;

	public DefaultBeanDescriptor(BaseBeanInfo info) {

		super(info.getType());

		try {
			setDisplayName(info.getResources().getString("beanName"));
		} catch (MissingResourceException e) {
			// fall thru, this resource is not mandatory
		}

		try {
			setShortDescription(info.getResources().getString("beanDescription"));
		} catch (MissingResourceException e) {
			// fall thru, this resource is not mandatory
		}
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void setDisplayName(String name) {
		displayName = name;
	}

}
