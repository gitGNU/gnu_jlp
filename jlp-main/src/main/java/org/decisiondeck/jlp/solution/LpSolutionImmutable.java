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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.decisiondeck.jlp.LpConstraint;
import org.decisiondeck.jlp.LpLinear;
import org.decisiondeck.jlp.problem.LpProblem;
import org.decisiondeck.jlp.problem.LpProblemImmutable;
import org.decisiondeck.jlp.utils.LpSolverUtils;
import org.decisiondeck.jlp.utils.LpUtils;

import com.google.common.base.Equivalence;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class LpSolutionImmutable<T> implements LpSolution<T> {

    /**
     * No <code>null</code> key or value.
     */
    private final Map<LpConstraint<T>, Number> m_dualValues;
    private final Number m_objectiveValue;
    private final ImmutableMap<T, Number> m_primalValues;
    private final LpProblem<T> m_problem;

    /**
     * <p>
     * An immutable solution related to the given problem, with solution values copied from an other solution. The set
     * of variables in the given problem must be a superset of the set of variables in the given solution. This
     * constructor may be useful e.g. if a solution <em>s</em> related to a problem <em>p</em> is known to be also
     * appliable to a different, but related, problem, e.g. a problem <em>p'</em> with relaxed constraints compared to
     * <em>p</em>.
     * </p>
     * <p>
     * The new solution is shielded from changes to the given problem and the given solution.
     * </p>
     * 
     * @param problem
     *            not <code>null</code>, must contain the variables for which the given solution has a value.
     * @param solution
     *            not <code>null</code>.
     */
    public LpSolutionImmutable(LpProblem<T> problem, LpSolutionAlone<T> solution) {
	this(problem, solution, true);
    }

    private LpSolutionImmutable(LpProblem<T> problem, LpSolutionAlone<T> solution, boolean protectProblem) {
	Preconditions.checkNotNull(solution);
	final Builder<T, Number> primalValues = ImmutableMap.builder();
	final Builder<LpConstraint<T>, Number> dualValues = ImmutableMap.builder();

	for (T variable : solution.getVariables()) {
	    final Number value = solution.getValue(variable);
	    if (value != null) {
		Preconditions.checkArgument(problem.getVariables().contains(variable),
			"Solution contains a variable that is not in the problem: " + variable + ".");
		primalValues.put(variable, value);
	    }
	}
	for (LpConstraint<T> constraint : solution.getConstraints()) {
	    final Number value = solution.getDualValue(constraint);
	    if (value != null) {
		Preconditions.checkArgument(problem.getConstraints().contains(constraint));
		dualValues.put(constraint, value);
	    }
	}
	m_objectiveValue = solution.getObjectiveValue();
	m_primalValues = primalValues.build();
	m_dualValues = dualValues.build();
	if (protectProblem) {
	    m_problem = new LpProblemImmutable<T>(problem);
	} else {
	    m_problem = problem;
	}
    }

    /**
     * Copy constructor by value.
     * 
     * @param solution
     *            not <code>null</code>.
     */
    public LpSolutionImmutable(LpSolution<T> solution) {
	this(solution.getProblem(), solution, false);
    }

    @Override
    public boolean boolsAreBools() {
	return LpSolverUtils.boolsAreBools(this);
    }

    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof LpSolution<?>)) {
	    return false;
	}

	LpSolution<?> s2 = (LpSolution<?>) obj;
	return LpSolverUtils.equivalent(this, s2);
    }

    @Override
    public boolean getBooleanValue(T variable) {
	Number number = m_primalValues.get(variable);
	if (number == null) {
	    throw new IllegalArgumentException("Variable has no value: " + variable + ".");
	}
	double v = number.doubleValue();
	if (Math.abs(v - 0) < 1e-6) {
	    return false;
	}
	if (Math.abs(v - 1) < 1e-6) {
	    return true;
	}
	throw new IllegalStateException("Variable has a non boolean value: " + variable + ".");
    }

    @Override
    public Number getComputedObjectiveValue() {
	final LpLinear<T> objectiveFunction = m_problem.getObjective().getFunction();
	if (objectiveFunction == null) {
	    return null;
	}
	return LpUtils.evaluate(objectiveFunction, m_primalValues);
    }

    @Override
    public Set<LpConstraint<T>> getConstraints() {
	return Collections.unmodifiableSet(m_dualValues.keySet());
    }

    @Override
    public Number getDualValue(LpConstraint<T> constraint) {
	Preconditions.checkNotNull(constraint);
	return m_dualValues.get(constraint);
    }

    @Override
    public Number getObjectiveValue() {
	return m_objectiveValue;
    }

    @Override
    public LpProblem<T> getProblem() {
	return m_problem;
    }

    @Override
    public Number getValue(T variable) {
	Preconditions.checkNotNull(variable);
	return m_primalValues.get(variable);
    }

    @Override
    public Set<T> getVariables() {
	return Collections.unmodifiableSet(m_primalValues.keySet());
    }

    @Override
    public int hashCode() {
	final Equivalence<LpSolution<T>> solutionEquivalence = LpSolverUtils.getSolutionEquivalence();
	return solutionEquivalence.hash(this);
    }

    @Override
    public String toString() {
	return LpSolverUtils.getAsString(this);
    }

}
