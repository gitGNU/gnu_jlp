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

public class LpProblemImmutable<T> extends LpProblemReadView<T> implements LpProblem<T> {

    /**
     * Creates a new problem that contains the same data than the given problem and is immutable.
     * 
     * @param problem
     *            not <code>null</code>.
     */
    public LpProblemImmutable(LpProblem<T> problem) {
	super((problem instanceof LpProblemImmutable<?>) ? problem : new LpProblemImpl<T>(problem));
    }
}
