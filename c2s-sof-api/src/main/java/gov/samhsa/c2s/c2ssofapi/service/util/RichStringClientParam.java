package gov.samhsa.c2s.c2ssofapi.service.util;

import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.StringClientParam;

import java.util.Arrays;
import java.util.List;

public class RichStringClientParam extends StringClientParam{

	private final String myParamName;

	public RichStringClientParam(String theParamName) {
        super(theParamName);
        myParamName = theParamName;
	}

	/**
	 * The string contains the given value (servers will often, but are not required to) implement this as a contains match,
	 * meaning that a value of "mi" would match "smi" and "smith".
	 */
	public IStringMatch contains() {
		return new StringContains();
	}

	private class StringContains implements IStringMatch {
		@Override
		public ICriterion<StringClientParam> value(String theValue) {
			return new StringCriterion<StringClientParam>(getParamName() + Constants.PARAMQUALIFIER_STRING_CONTAINS, theValue);
		}

		@Override
		public ICriterion<StringClientParam> value(StringDt theValue) {
			return new StringCriterion<StringClientParam>(getParamName() + Constants.PARAMQUALIFIER_STRING_CONTAINS, theValue.getValue());
		}

		@Override
		public ICriterion<StringClientParam> values(List<String> theValue) {
			return new StringCriterion<StringClientParam>(getParamName() + Constants.PARAMQUALIFIER_STRING_CONTAINS, theValue);
		}

		@Override
		public ICriterion<?> values(String... theValues) {
			return new StringCriterion<StringClientParam>(getParamName() + Constants.PARAMQUALIFIER_STRING_CONTAINS, Arrays.asList(theValues));
		}
	}

}
