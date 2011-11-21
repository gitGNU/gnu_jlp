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
package org.decisiondeck.jlp.cplex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.decisiondeck.jlp.LpConstraint;
import org.decisiondeck.jlp.LpFileFormat;
import org.decisiondeck.jlp.LpResultStatus;
import org.decisiondeck.jlp.LpSolver;
import org.decisiondeck.jlp.instanciation.LpSolverFactory;
import org.decisiondeck.jlp.instanciation.LpSolverType;
import org.decisiondeck.jlp.parameters.LpObjectParameter;
import org.decisiondeck.jlp.problem.LpProblem;
import org.decisiondeck.jlp.solution.LpProblemExamples;
import org.decisiondeck.jlp.solution.LpSolution;
import org.decisiondeck.jlp.solution.LpSolutionImmutable;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.Resources;

// for missing javadoc.
@SuppressWarnings("all")
public class TestCplex {
    @Test
    // for missing javadoc.
    public void testWriteMpsRenamed() throws Exception {
	final LpProblem<String> problem = LpProblemExamples.getIntOneFourThree();

	LpSolver<String> solver = new LpSolverFactory(LpSolverType.CPLEX).newSolver(String.class);
	solver.setProblem(problem);
	solver.getParametersView().setValue(LpObjectParameter.NAMER_VARIABLES, getRenamer());

	final Map<LpFileFormat, Function<LpConstraint<String>, String>> constraintsNamers = Maps
		.<LpFileFormat, Function<LpConstraint<String>, String>> newHashMap();
	constraintsNamers.put(LpFileFormat.MPS, getConstraintsNamer());
	solver.getParametersView().setValue(LpObjectParameter.NAMER_CONSTRAINTS_BY_FORMAT, constraintsNamers);

	final File temp = File.createTempFile("cplex-test", ".mps");
	temp.deleteOnExit();
	final String fullPath = temp.getAbsolutePath();
	final String pathNoExt = fullPath.substring(0, fullPath.length() - 4);
	solver.writeProblem(LpFileFormat.MPS, pathNoExt, true);
	final String written = Files.toString(temp, Charsets.UTF_8);

	final String expected = Resources.toString(
		getClass().getResource("OneFourThree - Renamed variables and constraints.mps"), Charsets.UTF_8);
	assertEquals(expected, written);
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

    @Test
    // for missing javadoc.
    public void testWriteLpVariablesRenamed() throws Exception {
	final LpProblem<String> problem = LpProblemExamples.getIntOneFourThree();

	LpSolver<String> solver = new LpSolverFactory(LpSolverType.CPLEX).newSolver(String.class);
	solver.setProblem(problem);
	final Map<LpFileFormat, Function<String, String>> variablesNamers = Maps
		.<LpFileFormat, Function<String, String>> newHashMap();
	variablesNamers.put(LpFileFormat.CPLEX_LP, getRenamer());
	solver.getParametersView().setValue(LpObjectParameter.NAMER_VARIABLES_BY_FORMAT, variablesNamers);

	/** Constraints namers for a different format => should have no effect. */
	final Map<LpFileFormat, Function<LpConstraint<String>, String>> constraintsNamers = Maps
		.<LpFileFormat, Function<LpConstraint<String>, String>> newHashMap();
	constraintsNamers.put(LpFileFormat.CPLEX_SAV, getConstraintsNamer());
	solver.getParametersView().setValue(LpObjectParameter.NAMER_CONSTRAINTS_BY_FORMAT, constraintsNamers);

	final File temp = File.createTempFile("cplex-test", ".lp");
	temp.deleteOnExit();
	final String fullPath = temp.getAbsolutePath();
	final String pathNoExt = fullPath.substring(0, fullPath.length() - 3);
	solver.writeProblem(LpFileFormat.CPLEX_LP, pathNoExt, true);
	final String written = Files.toString(temp, Charsets.UTF_8);

	final String expected = Resources.toString(getClass().getResource("OneFourThree - Renamed variables.lp"),
		Charsets.UTF_8);
	assertEquals(expected, written);
    }

    private Function<LpConstraint<String>, String> getConstraintsNamer() {
	return new Function<LpConstraint<String>, String>() {
	    @Override
	    public String apply(LpConstraint<String> input) {
		return "cstr_" + input.getName();
	    }
	};
    }

    @Test
    // for missing javadoc.
    public void testWriteMps() throws Exception {
	final LpProblem<String> problem = LpProblemExamples.getIntOneFourThree();

	LpSolver<String> solver = new LpSolverFactory(LpSolverType.CPLEX).newSolver(String.class);
	solver.setProblem(problem);
	final File temp = File.createTempFile("cplex-test", ".mps");
	temp.deleteOnExit();
	final String fullPath = temp.getAbsolutePath();
	final String pathNoExt = fullPath.substring(0, fullPath.length() - 4);
	solver.writeProblem(LpFileFormat.MPS, pathNoExt, true);
	final String written = Files.toString(temp, Charsets.UTF_8);

	final String expected = Resources.toString(getClass().getResource("OneFourThree.mps"), Charsets.UTF_8);
	assertEquals(expected, written);
    }

    @Test
    // for missing javadoc.
    public void testWriteLp() throws Exception {
	final LpProblem<String> problem = LpProblemExamples.getIntOneFourThree();

	LpSolver<String> solver = new LpSolverFactory(LpSolverType.CPLEX).newSolver(String.class);
	solver.setProblem(problem);
	final Function<String, String> renamer = getRenamer();
	final HashMap<LpFileFormat, Function<String, String>> namers = Maps
		.<LpFileFormat, Function<String, String>> newHashMap();
	namers.put(LpFileFormat.CPLEX_SAV, renamer);
	solver.getParametersView().setValue(LpObjectParameter.NAMER_VARIABLES_BY_FORMAT, namers);
	final File temp = File.createTempFile("cplex-test", ".lp");
	temp.deleteOnExit();
	final String fullPath = temp.getAbsolutePath();
	final String pathNoExt = fullPath.substring(0, fullPath.length() - 3);
	solver.writeProblem(LpFileFormat.CPLEX_LP, pathNoExt, true);
	final String written = Files.toString(temp, Charsets.UTF_8);

	final String expected = Resources.toString(getClass().getResource("OneFourThree.lp"), Charsets.UTF_8);
	assertEquals(expected, written);
    }

    @Test
    // for missing javadoc.
    public void testWriteLpConstraintsRenamedInSolver() throws Exception {
	final LpProblem<String> problem = LpProblemExamples.getIntOneFourThree();
	final Function<LpConstraint<String>, String> namer = getConstraintsNamer();

	LpSolver<String> solver = new LpSolverFactory(LpSolverType.CPLEX).newSolver(String.class);
	solver.setProblem(problem);
	solver.getParametersView().setValue(LpObjectParameter.NAMER_CONSTRAINTS, namer);
	final File temp = File.createTempFile("cplex-test", ".lp");
	temp.deleteOnExit();
	final String fullPath = temp.getAbsolutePath();
	final String pathNoExt = fullPath.substring(0, fullPath.length() - 3);
	solver.writeProblem(LpFileFormat.CPLEX_LP, pathNoExt, true);
	final String written = Files.toString(temp, Charsets.UTF_8);

	final String expected = Resources.toString(getClass().getResource("OneFourThree - Renamed constraints.lp"),
		Charsets.UTF_8);
	assertEquals(expected, written);
    }

    private Function<String, String> getRenamer() {
	return new Function<String, String>() {
	    @Override
	    public String apply(String input) {
		return "var_" + input;
	    }
	};
    }

    @Test
    // for missing javadoc.
    public void testWriteLpConstraintsRenamedInProblem() throws Exception {
	final LpProblem<String> problem = LpProblemExamples.getIntOneFourThree();
	final Function<LpConstraint<String>, String> namer = getConstraintsNamer();
	problem.setConstraintsNamer(namer);

	LpSolver<String> solver = new LpSolverFactory(LpSolverType.CPLEX).newSolver(String.class);
	solver.setProblem(problem);
	final File temp = File.createTempFile("cplex-test", ".lp");
	temp.deleteOnExit();
	final String fullPath = temp.getAbsolutePath();
	final String pathNoExt = fullPath.substring(0, fullPath.length() - 3);
	solver.writeProblem(LpFileFormat.CPLEX_LP, pathNoExt, true);
	final String written = Files.toString(temp, Charsets.UTF_8);

	final String expected = Resources.toString(getClass().getResource("OneFourThree - Renamed constraints.lp"),
		Charsets.UTF_8);
	assertEquals(expected, written);
    }

}