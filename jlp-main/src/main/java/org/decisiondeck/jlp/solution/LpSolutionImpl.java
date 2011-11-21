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
/**
 * Java ILP is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Java ILP is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Java ILP. If not, see http://www.gnu.org/licenses/.
 */
package org.decisiondeck.jlp.solution;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.decisiondeck.jlp.LpConstraint;
import org.decisiondeck.jlp.LpLinear;
import org.decisiondeck.jlp.problem.LpProblem;
import org.decisiondeck.jlp.problem.LpProblemImmutable;
import org.decisiondeck.jlp.utils.LpSolverUtils;
import org.decisiondeck.jlp.utils.LpUtils;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * The class {@code ResultImpl} is a {@code Map} based implementation of the {@link LpSolution}.
 * 
 * @author lukasiewycz
 * @author Olivier Cailloux
 * @param <T>
 *            the type of the variables to be used in the new solver instance.
 * 
 */
public class LpSolutionImpl<T> implements LpSolution<T> {

    /**
     * No <code>null</code> key or value.
     */
    private final Map<LpConstraint<T>, Number> m_dualValues = new HashMap<LpConstraint<T>, Number>();
    private Number m_objectiveValue = null;
    /**
     * No <code>null</code> key or value.
     */
    private final Map<T, Number> m_primalValues = new HashMap<T, Number>();
    /**
     * Not <code>null</code>, immutable.
     */
    private final LpProblem<T> m_problem;

    /**
     * A new solution satisfying the given problem. The new solution is shielded from changes to the given problem.
     * 
     * @param problem
     *            not <code>null</code>.
     */
    public LpSolutionImpl(LpProblem<T> problem) {
	Preconditions.checkNotNull(problem);
	m_problem = new LpProblemImmutable<T>(problem);
    }

    /**
     * <p>
     * A new solution related to the given problem, with solution values copied from an other solution. The set of
     * (dual) variables in the given problem must be a superset of the set of (dual) variables in the given solution.
     * This constructor may be useful e.g. if a solution <em>s</em> related to a problem <em>p</em> is known to be also
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
    public LpSolutionImpl(LpProblem<T> problem, LpSolutionAlone<T> solution) {
	Preconditions.checkNotNull(problem);
	Preconditions.checkNotNull(solution);

	for (T variable : solution.getVariables()) {
	    final Number value = solution.getValue(variable);
	    if (value != null) {
		Preconditions.checkArgument(problem.getVariables().contains(variable));
		m_primalValues.put(variable, value);
	    }
	}
	for (LpConstraint<T> constraint : solution.getConstraints()) {
	    final Number value = solution.getDualValue(constraint);
	    if (value != null) {
		Preconditions.checkArgument(problem.getConstraints().contains(constraint));
		m_dualValues.put(constraint, value);
	    }
	}

	m_objectiveValue = solution.getObjectiveValue();
	m_problem = new LpProblemImmutable<T>(problem);
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
	return LpSolverUtils.getSolutionEquivalence().hash(this);
    }

    /**
     * @param constraint
     *            not <code>null</code>. Must exist in the bound problem.
     * @param value
     *            may be <code>null</code>.
     * @return <code>true</code> iff the call changed this object state, or equivalently, <code>false</code> iff the
     *         given value is <code>null</code> and the given variable did not have its dual value set, or the given
     *         value is not <code>null</code> and equals the dual value previously associated with the given variable.
     */
    public boolean putDualValue(LpConstraint<T> constraint, Number value) {
	Preconditions.checkNotNull(constraint);
	Preconditions.checkArgument(m_problem.getConstraints().contains(constraint));
	m_dualValues.put(constraint, value);
	final Number previous;
	if (value == null) {
	    previous = m_dualValues.remove(constraint);
	} else {
	    previous = m_dualValues.put(constraint, value);
	}
	return Objects.equal(previous, value);
    }

    /**
     * @param variable
     *            not <code>null</code>. Must exist in the bound problem.
     * @param value
     *            may be <code>null</code>.
     * @return <code>true</code> iff the call changed this object state, or equivalently, <code>false</code> iff the
     *         given value is <code>null</code> and the given variable did not have its primal value set, or the given
     *         value is not <code>null</code> and equals the primal value previously associated with the given variable.
     */
    public boolean putValue(T variable, Number value) {
	Preconditions.checkNotNull(variable);
	Preconditions.checkArgument(m_problem.getVariables().contains(variable));
	final Number previous;
	if (value == null) {
	    previous = m_primalValues.remove(variable);
	} else {
	    previous = m_primalValues.put(variable, value);
	}
	return Objects.equal(previous, value);
    }

    /**
     * The bound problem must have a defined objective.
     * 
     * @param objectiveValue
     *            may be <code>null</code>.
     */
    public void setObjectiveValue(Number objectiveValue) {
	Preconditions.checkState(m_problem.getObjective().isComplete(),
		"Objective value only with complete objective please.");
	m_objectiveValue = objectiveValue;
    }

    @Override
    public String toString() {
	return LpSolverUtils.getAsString(this);
    }

}