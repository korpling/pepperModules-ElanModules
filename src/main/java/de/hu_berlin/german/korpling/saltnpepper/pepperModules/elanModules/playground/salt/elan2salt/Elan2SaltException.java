/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.elanModules.playground.salt.elan2salt;

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.exceptions.SaltException;

public class Elan2SaltException extends SaltException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2254913079950779931L;

	public Elan2SaltException()
	{ super(); }
	
    public Elan2SaltException(String s)
    { super(s); }
    
	public Elan2SaltException(String s, Throwable ex)
	{super(s, ex); }
}
