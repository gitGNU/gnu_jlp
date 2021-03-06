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
package org.decisiondeck.jlp.minisat;

import java.util.HashMap;
import java.util.Map.Entry;

import net.sf.javailp.minisat.MiniSat;

import org.decisiondeck.jlp.AbstractLpSolver;
import org.decisiondeck.jlp.LpConstraint;
import org.decisiondeck.jlp.LpDirection;
import org.decisiondeck.jlp.LpFileFormat;
import org.decisiondeck.jlp.LpLinear;
import org.decisiondeck.jlp.LpOperator;
import org.decisiondeck.jlp.LpResultStatus;
import org.decisiondeck.jlp.LpSolverException;
import org.decisiondeck.jlp.LpTerm;
import org.decisiondeck.jlp.parameters.LpDoubleParameter;
import org.decisiondeck.jlp.problem.LpVariableType;
import org.decisiondeck.jlp.solution.LpSolutionImpl;
import org.decisiondeck.jlp.utils.LpSolverUtils;
import org.decisiondeck.jlp.utils.TimingHelper;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.Maps;

/**
 * <p>
 * The <a href="http://minisat.se/MiniSat+.html">MiniSat+</a> solver. The bounds of all variables must be defined and be
 * between zero and one, or the variable type must be {@link LpVariableType#BOOL}.
 * </p>
 * <p>
 * Note that the implementation is most probably incomplete. This has not been thoroughly tested. Help is welcome.
 * </p>
 * 
 * @param <T>
 *            the class used for the variables.
 * 
 * @author Olivier Cailloux
 * 
 */
public class SolverMiniSat<T> extends AbstractLpSolver<T> {

    private int m_coefficients[];
    private int m_literals[];
    private MiniSat m_minisat;
    private BiMap<T, Integer> m_variablesIds;

    /**
     * Creates a new solver instance.
     */
    public SolverMiniSat() {
	m_variablesIds = null;
	m_minisat = null;
	m_literals = null;
	m_coefficients = null;
    }

    @Override
    public void close() {
	m_variablesIds = null;
	m_minisat = null;
    }

    private int getAsInteger(double number, LpDirection optType) throws LpSolverException {
	final int intValue = LpSolverUtils.getAsInteger(number);
	switch (optType) {
	case MIN:
	    return intValue;
	case MAX:
	    return -intValue;
	}
	throw new IllegalStateException("Unknown opt type.");
    }

    /**
     * Retrieves the minisat string equivalent of the given operator.
     * 
     * @param operator
     *            not <code>null</code>.
     * @return not <code>null</code>.
     */
    public String getAsMinisatString(LpOperator operator) {
	Preconditions.checkNotNull(operator);
	switch (operator) {
	case LE:
	    return "<=";
	case GE:
	    return ">=";
	case EQ:
	    return "=";
	}
	throw new IllegalStateException("Unknown operator.");
    }

    @Override
    public LpFileFormat getPreferredFormat() throws LpSolverException {
	throw new LpSolverException("Not implemented yet.");
    }

    @Override
    public Object getUnderlyingSolver() throws LpSolverException {
	lazyInitSolver();
	return m_minisat;
    }

    private int getVariableId(final Object variable) throws LpSolverException {
	final Integer id = m_variablesIds.get(variable);
	if (id == null) {
	    throw new LpSolverException("Variable not declared: " + variable + ".");
	}
	return id.intValue();
    }

    /**
     * Retrieves the mapping between the variable objects used in the problem and the variable ids used by minisat. This
     * is guaranteed to be initialized after having called {@link #getUnderlyingSolver()} or {@link #solve()}.
     * 
     * @return <code>null</code> if not initialized yet.
     */
    public BiMap<T, Integer> getVariablesIds() {
	return m_variablesIds;
    }

    private void lazyInitSolver() throws LpSolverException {
	if (m_minisat != null) {
	    return;
	}

	LpSolverUtils.assertIntZeroOne(getProblem());

	m_variablesIds = LpSolverUtils.getVariablesIds(getProblem(), 0);

	setParameters();

	for (LpConstraint<T> constraint : getProblem().getConstraints()) {
	    LpLinear<T> linear = constraint.getLhs();
	    LpOperator operator = constraint.getOperator();

	    final String comp = getAsMinisatString(operator);

	    int rhs = LpSolverUtils.getAsInteger(constraint.getRhs());

	    populateCoefficients(linear);

	    m_minisat.addConstraint(m_coefficients, m_literals, comp, rhs);
	}

	if (!getProblem().getObjective().isEmpty()) {
	    populateCoefficients(getProblem().getObjective().getFunction(), getProblem().getObjective().getDirection());
	    m_minisat.setObjective(m_coefficients, m_literals);
	}

	{
	    for (T variable : getProblem().getVariables()) {
		final int id = getVariableId(variable);

		Number lowerBound = LpSolverUtils.getVarLowerBoundBounded(getProblem(), variable);
		Number upperBound = LpSolverUtils.getVarUpperBoundBounded(getProblem(), variable);

		int coeffs[] = new int[1];
		int lits[] = new int[1];
		coeffs[0] = 1;
		lits[0] = id + 1;

		if (lowerBound.doubleValue() > 0d) {
		    m_minisat.addConstraint(coeffs, lits, ">=", 1);
		}
		if (upperBound.doubleValue() < 1d) {
		    m_minisat.addConstraint(coeffs, lits, "<=", 0);
		}
	    }
	}
    }

    private void populateCoefficients(LpLinear<T> terms) throws LpSolverException {
	m_coefficients = new int[terms.size()];
	m_literals = new int[terms.size()];
	int i = 0;
	for (LpTerm<T> term : terms) {
	    final int idInt = getVariableId(term.getVariable());
	    final int coefficient = LpSolverUtils.getAsInteger(term.getCoefficient());
	    m_literals[i] = idInt;// + 1;
	    m_coefficients[i] = coefficient;
	    ++i;
	}
    }

    private void populateCoefficients(LpLinear<T> terms, LpDirection optType) throws LpSolverException {
	m_coefficients = new int[terms.size()];
	m_literals = new int[terms.size()];
	int i = 0;
	for (LpTerm<T> term : terms) {
	    final int idInt = getVariableId(term.getVariable());
	    final int coefficient = getAsInteger(term.getCoefficient(), optType);
	    m_literals[i] = idInt;// + 1;
	    m_coefficients[i] = coefficient;
	    ++i;
	}
    }

    private void setParameters() throws LpSolverException {
	final HashMap<Enum<?>, Object> mandatory = Maps.newHashMap();
	mandatory.put(LpDoubleParameter.MAX_WALL_SECONDS, null);
	mandatory.put(LpDoubleParameter.MAX_CPU_SECONDS, null);
	LpSolverUtils.assertConform(getParametersView(), mandatory);
	// minisat.setVerbose(value);
    }

    @Override
    protected LpResultStatus solveUnderlying() throws LpSolverException {
	lazyInitSolver();

	final TimingHelper timingHelper = new TimingHelper();
	timingHelper.start();
	m_minisat.solve();
	timingHelper.stop();

	final LpResultStatus result;
	if (m_minisat.okay()) {
	    result = LpResultStatus.FEASIBLE;
	} else {
	    result = LpResultStatus.INFEASIBLE;
	}

	if (result.isFeasible()) {
	    final LpSolutionImpl<T> solution = new LpSolutionImpl<T>(getProblem());

	    for (Entry<Integer, T> entry : m_variablesIds.inverse().entrySet()) {
		int id = entry.getKey().intValue();
		T variable = entry.getValue();

		final boolean b = m_minisat.valueOf(id);
		final Number value = b ? Integer.valueOf(1) : Integer.valueOf(0);

		solution.putValue(variable, value);
	    }
	    setSolution(solution);
	}

	m_lastDuration = timingHelper.getDuration();

	close();

	return result;
    }

    @Override
    public void writeProblem(LpFileFormat format, String file, boolean addExtension) throws LpSolverException {
	throw new LpSolverException("Not implemented yet.");
    }

}
