/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util.data;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * A mutable integer (key) wrapper with double auxiliary value.
 * 
 */
public class MutableIntegerDouble extends MutableInteger {

	/** The mutable value. */
	private double aux;

	/**
	 * Constructs a new MutableDouble with the default value of zero.
	 */
	public MutableIntegerDouble() {
	}

	/**
	 * Constructs a new MutableDouble with the specified value.
	 * 
	 * @param value
	 *            a value.
	 */
	public MutableIntegerDouble(int value, double aux) {
		super(value);
		this.aux = aux;
	}

	public double getAux() {
		return aux;
	}

	public void setAux(double aux) {
		this.aux = aux;
	}

	/**
	 * Constructs a new MutableDouble with the specified value.
	 * 
	 * @param value
	 *            a value.
	 * @throws NullPointerException
	 *             if the object is null
	 */
	public MutableIntegerDouble(Number value) {
		super(value);
	}

}
