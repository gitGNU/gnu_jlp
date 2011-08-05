/**
 * Copyright © 2010-2011 Olivier Cailloux
 *
 *     This file is part of JLP.
 *
 *     JLP is free software: you can redistribute it and/or modify it under the
 *     terms of the GNU Lesser General Public License version 3 as published by
 *     the Free Software Foundation.
 *
 *     JLP is distributed in the hope that it will be useful, but WITHOUT ANY
 * 	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * 	FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * 	more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with JLP. If not, see <http://www.gnu.org/licenses/>.
 */
package org.decisiondeck.jlp.problem;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.decisiondeck.jlp.LpConstraint;
import org.decisiondeck.jlp.LpDirection;
import org.decisiondeck.jlp.LpLinear;
import org.decisiondeck.jlp.LpLinearImmutable;
import org.decisiondeck.jlp.LpObjective;
import org.decisiondeck.jlp.LpOperator;
import org.decisiondeck.jlp.LpTerm;
import org.decisiondeck.jlp.utils.LpSolverUtils;

import com.google.common.base.Equivalence;
import com.google.common.base.Equivalences;
import com.google.common.base.Preconditions;
import com.google.common.collect.EnumMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

/**
 * A simple mutable implementation of {@link LpProblem}.
 * 
 * @param <T>
 *            the class used for the variables.
 * 
 * @author Olivier Cailloux
 * 
 */
public class LpProblemImpl<T> implements LpProblem<T> {

    /**
     * Never <code>null</code>.
     */
    private String m_name;

    @Override
    public String getName() {
	return m_name;
    }

    @Override
    public int hashCode() {
	final Equivalence<LpProblem<T>> problemEquivalence = LpSolverUtils.getProblemEquivalence();
	return problemEquivalence.hash(this);
    }

    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof LpProblem<?>)) {
	    return false;
	}
	LpProblem<?> p2 = (LpProblem<?>) obj;
	return LpSolverUtils.equivalent(this, p2);
    }

    @Override
    public boolean setName(String name) {
	final String newName;
	if (name == null) {
	    newName = "";
	} else {
	    newName = name;
	}
	final boolean equivalent = Equivalences.equals().equivalent(m_name, newName);
	if (equivalent) {
	    return false;
	}
	m_name = name;
	return true;
    }

    private LpLinear<T> m_objectiveFunction;
    private LpDirection m_optType;
    private final Set<LpConstraint<T>> m_constraints = Sets.newLinkedHashSet();

    /**
     * No <code>null</code> key or value. Each variable in this problem has a type, thus this map contains all the
     * variables in the problem.
     */
    private final Map<T, LpVariableType> m_varType = Maps.newLinkedHashMap();

    private final Multiset<LpVariableType> m_varCount = EnumMultiset.create(LpVariableType.class);
    private final Map<T, Number> m_varLowerBound = Maps.newHashMap();
    private final Map<T, Number> m_varUpperBound = Maps.newHashMap();
    /**
     * Contains no <code>null</code> keys, no <code>null</code> values, no empty string values.
     */
    private final Map<T, String> m_varNames = Maps.newHashMap();

    public LpProblemImpl() {
	m_name = "";
	m_objectiveFunction = null;
	m_optType = null;
    }

    /**
     * A copy constructor, by value. No reference is shared between the new problem and the given one.
     * 
     * @param problem
     *            not <code>null</code>.
     */
    public LpProblemImpl(LpProblem<T> problem) {
	Preconditions.checkNotNull(problem);
	LpSolverUtils.copyProblemTo(problem, this);
    }

    @Override
    public LpObjective<T> getObjective() {
	return new LpObjective<T>(m_objectiveFunction, m_optType);
    }

    @Override
    public boolean setObjective(LpLinear<T> objectiveFunction, LpDirection direction) {
	final boolean equivFct = Equivalences.equals().equivalent(m_objectiveFunction, objectiveFunction);
	if (!equivFct) {
	    if (objectiveFunction == null) {
		m_objectiveFunction = null;
	    } else {
		assertVariablesExist(objectiveFunction);
		m_objectiveFunction = new LpLinearImmutable<T>(objectiveFunction);
	    }
	}
	final boolean equalDirs = m_optType == direction;
	m_optType = direction;
	return !equivFct || !equalDirs;
    }

    private void assertVariablesExist(LpLinear<T> linear) {
	for (LpTerm<T> term : linear) {
	    final T variable = term.getVariable();
	    Preconditions.checkArgument(m_varType.containsKey(variable));
	}
    }

    @Override
    public boolean setObjectiveDirection(LpDirection optType) {
	final boolean equalDir = m_optType == optType;
	m_optType = optType;
	return !equalDir;
    }

    @Override
    public Set<LpConstraint<T>> getConstraints() {
	return Collections.unmodifiableSet(m_constraints);
    }

    @Override
    public Set<T> getVariables() {
	return Collections.unmodifiableSet(m_varType.keySet());
    }

    /**
     * NB no defensive copy of the given constraint is done. Adds a constraint, or does nothing if the given constraint
     * is already in the problem. The variables used in the objective must have been added to this problem already.
     * 
     * @param constraint
     *            the constraint to be added. Not <code>null</code>.
     * @return <code>true</code> iff the call modified the state of this object. Equivalently, returns
     *         <code>false</code> iff the given constraint already was in the problem.
     */
    private boolean addInternal(LpConstraint<T> constraint) {
	Preconditions.checkNotNull(constraint);
	for (LpTerm<T> term : constraint.getLhs()) {
	    final T variable = term.getVariable();
	    if (!m_varType.containsKey(variable)) {
		// setVarTypeInternal(variable, IlpVariableType.REAL);
		throw new IllegalArgumentException("Unknown variable in constraint: " + variable + ".");
	    }
	}
	return m_constraints.add(constraint);
    }

    @Override
    public boolean add(LpConstraint<T> constraint) {
	return addInternal(constraint);
    }

    @Override
    public boolean add(String name, LpLinear<T> lhs, LpOperator operator, double rhs) {
	Preconditions.checkNotNull(lhs, "" + operator + rhs);
	Preconditions.checkNotNull(operator, "" + lhs + rhs);
	Preconditions.checkArgument(!Double.isNaN(rhs) && !Double.isInfinite(rhs));
	LpConstraint<T> constraint = new LpConstraint<T>(name, lhs, operator, rhs);
	return addInternal(constraint);
    }

    @Override
    public LpVariableType getVarType(T variable) {
	Preconditions.checkArgument(m_varType.containsKey(variable));
	return m_varType.get(variable);
    }

    @Override
    public boolean setVarType(T variable, LpVariableType type) {
	Preconditions.checkNotNull(variable, type);
	Preconditions.checkNotNull(type, variable);
	final LpVariableType previous = setVarTypeInternal(variable, type);
	return previous == null || previous != type;
    }

    private LpVariableType setVarTypeInternal(T variable, LpVariableType type) {
	Preconditions.checkNotNull(type);
	final LpVariableType previous = m_varType.put(variable, type);
	if (previous != null && previous != type) {
	    final boolean removed = m_varCount.remove(previous);
	    Preconditions.checkState(removed);
	}
	if (previous == null || previous != type) {
	    m_varCount.add(type);
	}
	return previous;
    }

    @Override
    public boolean setVarName(T variable, String name) {
	Preconditions.checkNotNull(variable, "" + name);
	final boolean added = addVariable(variable);

	final String previous;
	final boolean changed;
	if (name == null || name.isEmpty()) {
	    previous = m_varNames.remove(variable);
	    changed = previous != null;
	} else {
	    previous = m_varNames.put(variable, name);
	    changed = !name.equals(previous);
	}
	return added || changed;
    }

    @Override
    public Number getVarLowerBound(T variable) {
	Preconditions.checkArgument(m_varType.containsKey(variable));
	return m_varLowerBound.get(variable);
    }

    @Override
    public Number getVarUpperBound(T variable) {
	Preconditions.checkArgument(m_varType.containsKey(variable));
	return m_varUpperBound.get(variable);
    }

    @Override
    public boolean setVarBounds(T variable, Number lowerBound, Number upperBound) {
	Preconditions.checkNotNull(variable, "" + lowerBound + "; " + upperBound);
	final Number newLower = lowerBound == null ? m_varLowerBound.get(variable) : lowerBound;
	final Number newUpper = upperBound == null ? m_varUpperBound.get(variable) : upperBound;
	if (newLower != null && newUpper != null) {
	    Preconditions.checkArgument(newLower.doubleValue() <= newUpper.doubleValue(), "Lower bound: " + newLower
		    + " must be less or equal to upper bound: " + newUpper + ".");
	}

	final boolean added = addVariable(variable);

	final Number previousLower;
	if (lowerBound == null) {
	    previousLower = m_varLowerBound.remove(variable);
	} else {
	    previousLower = m_varLowerBound.put(variable, lowerBound);
	}
	final boolean changedLower = LpSolverUtils.getEquivalenceByDoubleValue().equivalent(previousLower, lowerBound);

	final Number previousUpper;
	if (upperBound == null) {
	    previousUpper = m_varUpperBound.remove(variable);
	} else {
	    previousUpper = m_varUpperBound.put(variable, upperBound);
	}
	final boolean changedUpper = LpSolverUtils.getEquivalenceByDoubleValue().equivalent(previousUpper, upperBound);

	return added || changedLower || changedUpper;
    }

    @Override
    public String toString() {
	return LpSolverUtils.getAsString(this);
    }

    @Override
    public String getVarName(T variable) {
	Preconditions.checkArgument(m_varType.containsKey(variable));
	final String name = m_varNames.get(variable);
	return name == null ? "" : name;
    }

    @Override
    public boolean addVariable(T variable) {
	if (m_varType.containsKey(variable)) {
	    return false;
	}
	setVarTypeInternal(variable, LpVariableType.REAL);
	return true;
    }

    @Override
    public LpDimension getDimension() {
	return new LpDimension(m_varCount.count(LpVariableType.BOOL), m_varCount.count(LpVariableType.INT),
		m_varCount.count(LpVariableType.REAL), getConstraints().size());
    }

    @Override
    public void clear() {
	m_constraints.clear();
	m_name = "";
	m_objectiveFunction = null;
	m_optType = null;
	m_varCount.clear();
	m_varNames.clear();
	m_varType.clear();
	m_varLowerBound.clear();
	m_varUpperBound.clear();
    }

}
