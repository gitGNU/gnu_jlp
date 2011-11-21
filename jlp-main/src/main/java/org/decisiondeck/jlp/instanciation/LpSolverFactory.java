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
package org.decisiondeck.jlp.instanciation;

import org.decisiondeck.jlp.LpSolver;

public class LpSolverFactory {

    private static final String CLASS_NAME_CPLEX = "org.decisiondeck.jlp.cplex.SolverCPLEX";
    private static final String CLASS_NAME_LP_SOLVE = "org.decisiondeck.jlp.lpsolve.SolverLpSolve";
    /**
     * <code>null</code> for no solver implementation specified.
     */
    private LpSolverType m_solverImpl;

    /**
     * @param impl
     *            <code>null</code> for not set.
     */
    public LpSolverFactory(LpSolverType impl) {
	m_solverImpl = impl;
    }

    public LpSolverFactory() {
	m_solverImpl = null;
    }

    /**
     * <p>
     * A solver implementation must have been specified.
     * </p>
     * <p>
     * Using this method is recommended over {@link #newSolverThrowing(Class)} because of its higher-level exception
     * mechanism as it simply wraps exceptions that can be thrown in a simpler exception.
     * </p>
     * 
     * @param type
     *            a technical parameter used only for knowing the generic type T.
     * @param <T>
     *            the type of the variables to be used in the new solver instance.
     * 
     * @return a new solver instance backed by the implementation previously chosen.
     * 
     * @throws LpSolverFactoryException
     *             if anything goes wrong when constructing the solver.
     */
    public <T> LpSolver<T> newSolver(Class<T> type) throws LpSolverFactoryException {
	try {
	    return newSolverThrowing(type);
	} catch (Exception exc) {
	    throw new LpSolverFactoryException(exc);
	}
    }

    public <T> LpSolver<T> newSolver(LpSolverType impl, Class<T> type) throws LpSolverFactoryException {
	setImpl(impl);
	return newSolver(type);
    }

    /**
     * <p>
     * A solver implementation must have been specified.
     * </p>
     * 
     * <p>
     * Note that this method propagates any exception thrown by the nullary constructor, including a checked exception.
     * Use of this method effectively bypasses the compile-time exception checking that would otherwise be performed by
     * the compiler. However, the solver implementor is forbidden to do that, thus it should not happen.
     * </p>
     * 
     * @param type
     *            a technical parameter used only for knowing the generic type T.
     * @param <T>
     *            the type of the variables to be used in the new solver instance.
     * 
     * @return a new solver instance backed by the implementation previously chosen.
     * 
     * @throws ClassNotFoundException
     *             if the implementing class is not found. Try an other implementation or check your classpath.
     * @throws InstantiationException
     *             if the implementing class does not look like a correct implementing class (e.g. it does not implement
     *             the required interface) or can't be instantiated.
     * @throws IllegalAccessException
     *             if the class or its nullary constructor is not accessible.
     * @throws SecurityException
     *             If a security manager, <i>s</i>, is present and any of the following conditions is met:
     *             <ul>
     *             <li>invocation of {@link SecurityManager#checkMemberAccess s.checkMemberAccess(this, Member.PUBLIC)}
     *             denies creation of new instances of this class
     *             <li>the caller's class loader is not the same as or an ancestor of the class loader for the current
     *             class and invocation of {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} denies
     *             access to the package of this class
     *             </ul>
     */
    public <T> LpSolver<T> newSolverThrowing(@SuppressWarnings("unused") Class<T> type) throws ClassNotFoundException,
	    InstantiationException, IllegalAccessException, SecurityException {
	/**
	 * Ideally we should prevent possible checked exceptions thrown by the constructor to propagate. We could check
	 * using reflection that the constructor does not do that (it should not!), but this is not implemented yet.
	 */
	if (m_solverImpl == null) {
	    throw new IllegalStateException("Solver implementation has not been specified.");
	}

	final String className;
	switch (m_solverImpl) {
	case CPLEX:
	    className = CLASS_NAME_CPLEX;
	    break;
	case LP_SOLVE:
	    className = CLASS_NAME_LP_SOLVE;
	    break;
	default:
	    throw new IllegalStateException("Unknown impl: " + m_solverImpl + ".");
	}
	Class<?> c = Class.forName(className);
	final Object inst = c.newInstance();
	if (!(inst instanceof LpSolver)) {
	    throw new InstantiationException("Class " + className + " found but is not an instance of "
		    + LpSolver.class.getCanonicalName() + ".");
	}
	@SuppressWarnings("unchecked")
	final LpSolver<T> inst2 = (LpSolver<T>) inst;
	return inst2;
    }

    public void setImpl(LpSolverType impl) {
	m_solverImpl = impl;
    }

}
