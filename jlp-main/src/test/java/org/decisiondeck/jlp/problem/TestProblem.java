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
package org.decisiondeck.jlp.problem;

import org.decisiondeck.jlp.LpDirection;
import org.decisiondeck.jlp.LpLinear;
import org.decisiondeck.jlp.LpLinearImpl;
import org.junit.Test;

public class TestProblem {

    @Test(expected = IllegalArgumentException.class)
    public void testProblemMissingVar() throws Exception {
	LpProblem<String> problem = new LpProblemImpl<String>();

	LpLinear<String> linear = new LpLinearImpl<String>();
	linear.add(1, "x");

	problem.setObjective(linear, LpDirection.MAX);

    }

}
