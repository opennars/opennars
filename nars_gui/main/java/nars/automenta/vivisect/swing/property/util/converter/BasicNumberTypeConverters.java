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
package automenta.vivisect.swing.property.util.converter;

import java.text.NumberFormat;

public class BasicNumberTypeConverters implements Converter {

  private static NumberFormat defaultFormat;

  private NumberFormat format;

  public BasicNumberTypeConverters() {
    this(getDefaultFormat());
  }

  public BasicNumberTypeConverters(NumberFormat format) {
    this.format = format;
  }

  public static NumberFormat getDefaultFormat() {
    synchronized (BasicNumberTypeConverters.class) {
      if (defaultFormat == null) {
        defaultFormat = NumberFormat.getNumberInstance();
        defaultFormat.setMinimumIntegerDigits(1);
        defaultFormat.setMaximumIntegerDigits(64);
        defaultFormat.setMinimumFractionDigits(0);
        defaultFormat.setMaximumFractionDigits(64);
        defaultFormat.setGroupingUsed(false);
      }
    }
    return defaultFormat;
  }

  public void register(ConverterRegistry registry) {

    registry.addConverter(double.class, String.class, this);
    registry.addConverter(float.class, String.class, this);
    registry.addConverter(int.class, String.class, this);
    registry.addConverter(long.class, String.class, this);
    registry.addConverter(short.class, String.class, this);
    registry.addConverter(byte.class, String.class, this);
    
    registry.addConverter(String.class, double.class, this);
    registry.addConverter(String.class, float.class, this);
    registry.addConverter(String.class, int.class, this);
    registry.addConverter(String.class, long.class, this);
    registry.addConverter(String.class, short.class, this);
    registry.addConverter(String.class, byte.class, this);
  }

  public Object convert(Class targetType, Object value) {
	  
	  if (targetType.equals(String.class))
		  return ""+value;
	  switch (targetType.getSimpleName()) {
	case "int":
		return Integer.parseInt(""+value);
	case "long":
		return Long.parseLong(""+value);
	case "short":
		return Short.parseShort(""+value);
	case "float":
		return Float.parseFloat(""+value);
	case "double":
		return Double.parseDouble(""+value);
	case "byte":
		return Byte.parseByte(""+value);
		
	default:
		break;
	}
    throw new IllegalArgumentException("no conversion supported for "+targetType.getSimpleName());
  }

}