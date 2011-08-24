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
package org.decisiondeck.jlp.cplex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.decisiondeck.jlp.LpResultStatus;
import org.decisiondeck.jlp.LpSolver;
import org.decisiondeck.jlp.instanciation.LpSolverFactory;
import org.decisiondeck.jlp.instanciation.LpSolverType;
import org.decisiondeck.jlp.problem.LpProblem;
import org.decisiondeck.jlp.solution.LpProblemExamples;
import org.decisiondeck.jlp.solution.LpSolution;
import org.decisiondeck.jlp.solution.LpSolutionImmutable;
import org.junit.Test;

// for missing javadoc.
@SuppressWarnings("all")
public class TestCplex {
    @Test
    // for missing javadoc.
    @SuppressWarnings("all")
    public void testIntOneFourThree() throws Exception {
	LpSolverFactory factory = new LpSolverFactory();
	factory.setImpl(LpSolverType.CPLEX);

	final LpProblem<String> problem = LpProblemExamples.getIntOneFourThree();

	LpSolver<String> solver = factory.newSolver(String.class);
	solver.setProblem(problem);

	final LpResultStatus status = solver.solve();
	assertEquals(LpResultStatus.OPTIMAL, status);

	LpSolution<String> solution = solver.getSolution();
	assertEquals(LpProblemExamples.getIntOneFourThreeSolution(), solution);

	final LpProblem<String> problemLowX = LpProblemExamples.getIntOneFourThreeLowX();
	solver.setProblem(problemLowX);

	final LpResultStatus statusLowX = solver.solve();
	assertEquals(LpResultStatus.OPTIMAL, statusLowX);

	final LpSolution<String> solutionLowX = solver.getSolution();
	assertEquals(LpProblemExamples.getIntOneFourThreeLowXSolution(), solutionLowX);
    }

    @Test(expected = IllegalStateException.class)
    // for missing javadoc.
    @SuppressWarnings("all")
    public void testSolveDirNotSet() throws Exception {
	LpSolverFactory factory = new LpSolverFactory();
	factory.setImpl(LpSolverType.CPLEX);

	final LpProblem<String> problem = LpProblemExamples.getIntOneFourThree();
	problem.setObjectiveDirection(null);

	LpSolver<String> solver = factory.newSolver(String.class);
	solver.setProblem(problem);
	solver.solve();
    }

    @Test(expected = IllegalArgumentException.class)
    // for missing javadoc.
    @SuppressWarnings("all")
    public void testUnusedVar() throws Exception {
	LpSolverFactory factory = new LpSolverFactory();
	factory.setImpl(LpSolverType.CPLEX);

	final LpProblem<String> problem = LpProblemExamples.getIntOneFourThree();
	problem.addVariable("z");

	LpSolver<String> solver = factory.newSolver(String.class);
	solver.setProblem(problem);

	final LpResultStatus status = solver.solve();
	assertEquals(LpResultStatus.OPTIMAL, status);

	LpSolution<String> solutionAugmentedProblem = solver.getSolution();
	/** Should be different because they do not relate to the same problem. */
	assertFalse(LpProblemExamples.getIntOneFourThreeSolution().equals(solutionAugmentedProblem));

	@SuppressWarnings("unused")
	final LpSolutionImmutable<String> lpSolutionImmutable = new LpSolutionImmutable<String>(
		LpProblemExamples.getIntOneFourThree(), solutionAugmentedProblem);
    }

}