/**
 * Copyright © 2010-2011 Olivier Cailloux
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
package org.decisiondeck.jlp.solution;

import org.decisiondeck.jlp.LpDirection;
import org.decisiondeck.jlp.LpLinear;
import org.decisiondeck.jlp.LpLinearImpl;
import org.decisiondeck.jlp.LpOperator;
import org.decisiondeck.jlp.problem.LpProblem;
import org.decisiondeck.jlp.problem.LpProblemImpl;
import org.decisiondeck.jlp.problem.LpVariableType;

public class LpProblemExamples {
    /**
     * Builds a new problem with integer variables:
     * <ul>
     * <li>Maximize: 143x+60y Subject to:</li>
     * <li>
     * 120x+210y <= 15000</li>
     * <li>
     * 110x+30y <= 4000</li>
     * <li>
     * x+y <= 75</li>
     * </ul>
     * 
     * The problem is named after the coefficient of the x variable in the objective function.
     * 
     * @return a new problem.
     */
    static public LpProblem<String> getIntOneFourThree() {
	LpProblem<String> problem = new LpProblemImpl<String>();
	problem.setName("OneFourThree");
	problem.setVarType("x", LpVariableType.INT);
	problem.setVarType("y", LpVariableType.INT);

	LpLinear<String> obj = new LpLinearImpl<String>();
	obj.add(143, "x");
	obj.add(60, "y");
	problem.setObjective(obj, LpDirection.MAX);

	LpLinear<String> c1 = new LpLinearImpl<String>();
	c1.add(120, "x");
	c1.add(210, "y");
	problem.add("c1", c1, LpOperator.LE, 15000);

	LpLinear<String> c2 = new LpLinearImpl<String>();
	c2.add(110, "x");
	c2.add(30, "y");
	problem.add("c2", c2, LpOperator.LE, 4000);

	LpLinear<String> c3 = new LpLinearImpl<String>();
	c3.add(1, "x");
	c3.add(1, "y");
	problem.add("c3", c3, LpOperator.LE, 75);

	return problem;
    }

    public static LpProblem<String> getIntOneFourThreeLowX() {
	final LpProblem<String> problem = getIntOneFourThree();
	problem.setVarBounds("x", null, Double.valueOf(16d));
	return problem;
    }

    /**
     * Retrieves the optimal solution of the problem.
     * 
     * @return the solution.
     */
    static public LpSolution<String> getIntOneFourThreeLowXSolution() {
	final LpSolutionImpl<String> solution = new LpSolutionImpl<String>(getIntOneFourThreeLowX());
	solution.setObjectiveValue(Integer.valueOf(5828));
	solution.putValue("x", Integer.valueOf(16));
	solution.putValue("y", Integer.valueOf(59));
	assert (solution.getComputedObjectiveValue().doubleValue() == solution.getObjectiveValue().doubleValue());
	return solution;
    }

    /**
     * Retrieves the optimal solution of the problem.
     * 
     * @return the solution.
     */
    static public LpSolution<String> getIntOneFourThreeSolution() {
	final LpSolutionImpl<String> solution = new LpSolutionImpl<String>(getIntOneFourThree());
	solution.setObjectiveValue(Integer.valueOf(6266));
	solution.putValue("x", Integer.valueOf(22));
	solution.putValue("y", Integer.valueOf(52));
	assert (solution.getComputedObjectiveValue().doubleValue() == solution.getObjectiveValue().doubleValue());
	return solution;
    }
}
