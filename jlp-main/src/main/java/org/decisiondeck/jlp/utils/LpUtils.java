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
package org.decisiondeck.jlp.utils;

import java.util.Map;
import java.util.Set;

import org.decisiondeck.jlp.LpConstraint;
import org.decisiondeck.jlp.LpLinear;
import org.decisiondeck.jlp.LpOperator;
import org.decisiondeck.jlp.LpTerm;
import org.decisiondeck.jlp.problem.LpProblem;
import org.decisiondeck.jlp.solution.LpSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class LpUtils {
    private static final Logger s_logger = LoggerFactory.getLogger(LpUtils.class);

    static public void assertSatisfied(double lhs, LpOperator op, double rhs) {
	if (!satisfied(lhs, op, rhs)) {
	    throw new IllegalStateException("Unsatisfied: " + lhs + op + rhs + ".");
	}
    }

    static public void assertSatisfied(int lhs, LpOperator op, int rhs) {
	if (!satisfied(lhs, op, rhs)) {
	    throw new IllegalStateException("Unsatisfied: " + lhs + op + rhs + ".");
	}
    }

    /**
     * Computes the result of the given linear expression with the given values assigned to the variables in the
     * expression. The number used for the variable values are converted to double for the computation.
     * 
     * @param <T>
     *            the class used for the variables.
     * @param linear
     *            not <code>null</code>.
     * @param values
     *            not <code>null</code>, no <code>null</code> keys or values.
     * @return <code>null</code> iff at least one of the variables used in the linear expression has no associated
     *         value, zero if the given linear is empty.
     */
    static public <T> Double evaluate(LpLinear<T> linear, Map<T, Number> values) {
	Preconditions.checkNotNull(linear);
	Preconditions.checkNotNull(values);

	double expr = 0d;
	for (LpTerm<T> term : linear) {
	    final T variable = term.getVariable();
	    if (!values.containsKey(variable)) {
		return null;
	    }
	    final double value = values.get(variable).doubleValue();
	    expr += term.getCoefficient() * value;
	}
	return Double.valueOf(expr);
    }

    static public <T> String getAsString(LpLinear<T> linear) {
	final Iterable<String> termsToStrings = Iterables.transform(linear, new Function<LpTerm<T>, String>() {
	    @Override
	    public String apply(LpTerm<T> term) {
		return term.getCoefficient() + "*" + term.getVariable();
	    }
	});

	return Joiner.on(" + ").join(termsToStrings);
    }

    static public <T> void logProblemContents(LpProblem<T> problem) {
	s_logger.info("Problem {}, {}.", problem.getName(), problem.getDimension());
	final Set<T> variables = problem.getVariables();
	for (T variable : variables) {
	    s_logger.info("Variable {} in problem: name {}, type " + problem.getVarType(variable) + ", bounds "
		    + problem.getVarLowerBound(variable) + " to " + problem.getVarUpperBound(variable) + ".", variable,
		    problem.getVarNameComputed(variable));
	}
	final Set<LpConstraint<T>> constraints = problem.getConstraints();
	for (LpConstraint<T> constraint : constraints) {
	    s_logger.info("Constraint {}.", constraint);
	}
	s_logger.info("Objective: {}.", problem.getObjective());
    }

    static public <T> void logSolutionValues(LpSolution<T> solution) {
	final Set<T> variables = solution.getVariables();
	for (T variable : variables) {
	    final Number value = solution.getValue(variable);
	    s_logger.info("Variable {} has value {}.", variable, value);
	}
	s_logger.info("Objective value: {}.", solution.getObjectiveValue());
    }

    static public boolean satisfied(double lhs, LpOperator op, double rhs) {
	switch (op) {
	case EQ:
	    return lhs == rhs;
	case GE:
	    return lhs >= rhs;
	case LE:
	    return lhs <= rhs;
	}
	throw new IllegalStateException("Unknown operator.");
    }

    static public boolean satisfied(int lhs, LpOperator op, int rhs) {
	switch (op) {
	case EQ:
	    return lhs == rhs;
	case GE:
	    return lhs >= rhs;
	case LE:
	    return lhs <= rhs;
	}
	throw new IllegalStateException("Unknown operator.");
    }
}
