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
package org.decisiondeck.jlp.parameters;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Equivalences;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

/**
 * A simple implementation of {@link LpParameters} with {@link HashMap} objects.
 * 
 * @author Olivier Cailloux
 * 
 */
public class LpParametersImpl implements LpParameters {

    /**
     * Do not contain default values. No <code>null</code> key.
     */
    private final Map<LpDoubleParameter, Double> m_doubleParameters = new HashMap<LpDoubleParameter, Double>();
    /**
     * Do not contain default values. No <code>null</code> key.
     */
    private final Map<LpIntParameter, Integer> m_intParameters = new HashMap<LpIntParameter, Integer>();
    /**
     * Do not contain default values. No <code>null</code> key.
     */
    private final Map<LpStringParameter, String> m_stringParameters = new HashMap<LpStringParameter, String>();

    public LpParametersImpl() {
	/** No parameters constructor. */
    }

    /**
     * Creates a new object that contains all the values that have been set in the source object. The copy is by value.
     * 
     * @param source
     *            not <code>null</code>.
     */
    public LpParametersImpl(LpParameters source) {
	m_intParameters.putAll(source.getIntParameters());
	m_stringParameters.putAll(source.getStringParameters());
	m_doubleParameters.putAll(source.getDoubleParameters());
    }

    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof LpParameters)) {
	    return false;
	}
	LpParameters p2 = (LpParameters) obj;
	return LpParametersUtils.equivalent(this, p2);
    }

    @Override
    public Map<LpDoubleParameter, Double> getDoubleParameters() {
	return Maps.newHashMap(m_doubleParameters);
    }

    @Override
    public Map<LpIntParameter, Integer> getIntParameters() {
	return Maps.newHashMap(m_intParameters);
    }

    @Override
    public Map<LpStringParameter, String> getStringParameters() {
	return Maps.newHashMap(m_stringParameters);
    }

    @Override
    public Double getValue(LpDoubleParameter parameter) {
	Preconditions.checkNotNull(parameter);
	return m_doubleParameters.containsKey(parameter) ? m_doubleParameters.get(parameter)
		: LpParametersDefaultValues.getDefaultDoubleValues().get(parameter);
    }

    @Override
    public Integer getValue(LpIntParameter parameter) {
	Preconditions.checkNotNull(parameter);
	return m_intParameters.containsKey(parameter) ? m_intParameters.get(parameter) : LpParametersDefaultValues
		.getDefaultIntValues().get(parameter);
    }

    @Override
    public String getValue(LpStringParameter parameter) {
	Preconditions.checkNotNull(parameter);
	return m_stringParameters.containsKey(parameter) ? m_stringParameters.get(parameter)
		: LpParametersDefaultValues.getDefaultStringValues().get(parameter);
    }

    @Override
    public Object getValueAsObject(Enum<?> parameter) {
	Preconditions.checkNotNull(parameter);
	final Object value;
	if (parameter instanceof LpIntParameter) {
	    LpIntParameter intParameter = (LpIntParameter) parameter;
	    value = getValue(intParameter);
	} else if (parameter instanceof LpDoubleParameter) {
	    LpDoubleParameter doubleParameter = (LpDoubleParameter) parameter;
	    value = getValue(doubleParameter);
	} else if (parameter instanceof LpStringParameter) {
	    LpStringParameter stringParameter = (LpStringParameter) parameter;
	    value = getValue(stringParameter);
	} else {
	    throw new IllegalArgumentException("Unknown parameter type.");
	}
	return value;
    }

    @Override
    public int hashCode() {
	return LpParametersUtils.hash(this);
    }

    private boolean isDefaultValue(Object parameter, Object value) {
	final Object defaultValue = LpParametersDefaultValues.getDefaultValueObject(parameter);
	return Equivalences.equals().equivalent(defaultValue, value);
    }

    @Override
    public boolean setValue(LpDoubleParameter parameter, Double value) {
	Predicate<Double> validator = LpParametersUtils.getValidator(parameter);
	Preconditions.checkArgument(validator.apply(value), "The given value: " + value
		+ " is not meaningful for the parameter " + parameter + ".");
	return setValue(m_doubleParameters, parameter, value);
    }

    @Override
    public boolean setValue(LpIntParameter parameter, Integer value) {
	Predicate<Integer> validator = LpParametersUtils.getValidator(parameter);
	Preconditions.checkArgument(validator.apply(value), "The given value: " + value
		+ " is not meaningful for the parameter " + parameter + ".");
	return setValue(m_intParameters, parameter, value);
    }

    @Override
    public boolean setValue(LpStringParameter parameter, String value) {
	Predicate<String> validator = LpParametersUtils.getValidator(parameter);
	Preconditions.checkArgument(validator.apply(value), "The given value: " + value
		+ " is not meaningful for the parameter " + parameter + ".");
	return setValue(m_stringParameters, parameter, value);
    }

    private <IlpParameter, V> boolean setValue(final Map<IlpParameter, V> parametersMap, IlpParameter parameter, V value) {
	Preconditions.checkNotNull(parameter);
	final boolean isDefault = isDefaultValue(parameter, value);
	final boolean changed;
	if (isDefault) {
	    final V previous = parametersMap.remove(parameter);
	    changed = previous != null;
	} else {
	    final V previous = parametersMap.put(parameter, value);
	    changed = !Equivalences.equals().equivalent(previous, value);
	}
	return changed;
    }

    @Override
    public boolean setValueAsObject(Enum<?> parameter, Object value) {
	Preconditions.checkNotNull(parameter);
	final boolean changed;
	if (parameter instanceof LpIntParameter) {
	    LpIntParameter intParameter = (LpIntParameter) parameter;
	    Preconditions.checkArgument(value == null || (value instanceof Integer), "Incorrect value type: " + value
		    + ".");
	    changed = setValue(m_intParameters, intParameter, (Integer) value);
	} else if (parameter instanceof LpDoubleParameter) {
	    LpDoubleParameter doubleParameter = (LpDoubleParameter) parameter;
	    Preconditions.checkArgument(value == null || (value instanceof Double), "Incorrect value type: " + value
		    + ".");
	    changed = setValue(m_doubleParameters, doubleParameter, (Double) value);
	} else if (parameter instanceof LpStringParameter) {
	    LpStringParameter stringParameter = (LpStringParameter) parameter;
	    Preconditions.checkArgument(value == null || (value instanceof String), "Incorrect value type: " + value
		    + ".");
	    changed = setValue(m_stringParameters, stringParameter, (String) value);
	} else {
	    throw new IllegalArgumentException("Unknown parameter type.");
	}
	return changed;
    }

    @Override
    public String toString() {
	return LpParametersUtils.toString(this);
    }

}
