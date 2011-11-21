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

import org.decisiondeck.jlp.LpConstraint;
import org.decisiondeck.jlp.LpDirection;
import org.decisiondeck.jlp.LpLinear;
import org.decisiondeck.jlp.LpOperator;

import com.google.common.base.Function;

public class LpProblemReadView<T> extends LpProblemForwarder<T> implements LpProblem<T> {

    public LpProblemReadView(LpProblem<T> delegate) {
	super(delegate);
    }

    @Override
    public boolean add(LpConstraint<T> constraint) {
	throw new UnsupportedOperationException("This object is a read-only view.");
    }

    @Override
    public boolean add(Object id, LpLinear<T> lhs, LpOperator operator, double rhs) {
	throw new UnsupportedOperationException("This object is a read-only view.");
    }

    @Override
    public void setConstraintsNamer(Function<LpConstraint<T>, String> namer) {
	throw new UnsupportedOperationException("This object is a read-only view.");
    }

    @Override
    public boolean addVariable(T variable) {
	throw new UnsupportedOperationException("This object is a read-only view.");
    }

    @Override
    public void clear() {
	throw new UnsupportedOperationException("This object is a read-only view.");
    }

    @Override
    public boolean setName(String name) {
	throw new UnsupportedOperationException("This object is a read-only view.");
    }

    @Override
    public boolean setObjective(LpLinear<T> objective, LpDirection direction) {
	throw new UnsupportedOperationException("This object is a read-only view.");
    }

    @Override
    public boolean setObjectiveDirection(LpDirection dir) {
	throw new UnsupportedOperationException("This object is a read-only view.");
    }

    @Override
    public boolean setVarBounds(T variable, Number lowerBound, Number upperBound) {
	throw new UnsupportedOperationException("This object is a read-only view.");
    }

    @Override
    public boolean setVarName(T variable, String name) {
	throw new UnsupportedOperationException("This object is a read-only view.");
    }

    @Override
    public void setVarNamer(Function<T, String> namer) {
	throw new UnsupportedOperationException("This object is a read-only view.");
    }

    @Override
    public boolean setVarType(T variable, LpVariableType type) {
	throw new UnsupportedOperationException("This object is a read-only view.");
    }

}
