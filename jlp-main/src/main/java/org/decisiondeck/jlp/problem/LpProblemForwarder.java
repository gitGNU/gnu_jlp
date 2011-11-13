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
package org.decisiondeck.jlp.problem;

import java.util.Set;

import org.decisiondeck.jlp.LpConstraint;
import org.decisiondeck.jlp.LpDirection;
import org.decisiondeck.jlp.LpLinear;
import org.decisiondeck.jlp.LpObjective;
import org.decisiondeck.jlp.LpOperator;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class LpProblemForwarder<T> implements LpProblem<T> {

    private final LpProblem<T> m_delegate;

    /**
     * @param delegate
     *            not <code>null</code>.
     */
    public LpProblemForwarder(LpProblem<T> delegate) {
	Preconditions.checkNotNull(delegate);
	m_delegate = delegate;
    }

    @Override
    public boolean add(LpConstraint<T> constraint) {
	return m_delegate.add(constraint);
    }

    @Override
    public boolean add(String name, LpLinear<T> lhs, LpOperator operator, double rhs) {
	return m_delegate.add(name, lhs, operator, rhs);
    }

    @Override
    public boolean addVariable(T variable) {
	return m_delegate.addVariable(variable);
    }

    @Override
    public void clear() {
	m_delegate.clear();
    }

    protected LpProblem<T> delegate() {
	return m_delegate;
    }

    @Override
    public boolean equals(Object obj) {
	return m_delegate.equals(obj);
    }

    @Override
    public Set<LpConstraint<T>> getConstraints() {
	return m_delegate.getConstraints();
    }

    @Override
    public LpDimension getDimension() {
	return m_delegate.getDimension();
    }

    @Override
    public String getName() {
	return m_delegate.getName();
    }

    @Override
    public LpObjective<T> getObjective() {
	return m_delegate.getObjective();
    }

    @Override
    public Set<T> getVariables() {
	return m_delegate.getVariables();
    }

    @Override
    public Number getVarLowerBound(T variable) {
	return m_delegate.getVarLowerBound(variable);
    }

    @Override
    public String getVarNameSet(T variable) {
	return m_delegate.getVarNameSet(variable);
    }

    @Override
    public String getVarNameComputed(T variable) {
	return m_delegate.getVarNameComputed(variable);
    }

    @Override
    public LpVariableType getVarType(T variable) {
	return m_delegate.getVarType(variable);
    }

    @Override
    public Number getVarUpperBound(T variable) {
	return m_delegate.getVarUpperBound(variable);
    }

    @Override
    public int hashCode() {
	return m_delegate.hashCode();
    }

    @Override
    public boolean setName(String name) {
	return m_delegate.setName(name);
    }

    @Override
    public boolean setObjective(LpLinear<T> objective, LpDirection direction) {
	return m_delegate.setObjective(objective, direction);
    }

    @Override
    public boolean setObjectiveDirection(LpDirection dir) {
	return m_delegate.setObjectiveDirection(dir);
    }

    @Override
    public boolean setVarBounds(T variable, Number lowerBound, Number upperBound) {
	return m_delegate.setVarBounds(variable, lowerBound, upperBound);
    }

    @Override
    public boolean setVarName(T variable, String name) {
	return m_delegate.setVarName(variable, name);
    }

    @Override
    public boolean setVarType(T variable, LpVariableType type) {
	return m_delegate.setVarType(variable, type);
    }

    @Override
    public String toString() {
	return m_delegate.toString();
    }

    @Override
    public void setVarNamer(Function<T, String> namer) {
	m_delegate.setVarNamer(namer);
    }

    @Override
    public Function<T, String> getVarNamer() {
	return m_delegate.getVarNamer();
    }

}
