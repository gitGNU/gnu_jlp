/**
 * Copyright © 2010-2011 École Centrale Paris
 *
 * 	This file is part of JLP.
 *
 * 	JLP is free software: you can redistribute it and/or modify it under the
 * 	terms of the GNU Lesser General Public License version 3 as published by
 * 	the Free Software Foundation.
 *
 * 	JLP is distributed in the hope that it will be useful, but WITHOUT ANY
 * 	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * 	FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * 	more details.
 *
 * 	You should have received a copy of the GNU Lesser General Public License
 * 	along with JLP. If not, see <http://www.gnu.org/licenses/>.
 */
package org.decisiondeck.jlp.parameters;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IlpParametersDefaultValuesTest {
    @Test
    public void testInit() throws Exception {
	assertTrue(LpParametersDefaultValues.getDefaultDoubleValues().size() == LpDoubleParameter.values().length);
	assertTrue(LpParametersDefaultValues.getDefaultIntValues().size() == LpIntParameter.values().length);
	assertTrue(LpParametersDefaultValues.getDefaultStringValues().size() == LpStringParameter.values().length);
	assertTrue(LpParametersDefaultValues.getDefaultObjectValues().size() == LpObjectParameter.values().length);
    }
}
