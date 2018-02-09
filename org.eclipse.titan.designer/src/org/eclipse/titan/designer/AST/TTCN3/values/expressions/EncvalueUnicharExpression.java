/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.values.CharstringExtractor;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Expression type for
 * encvalue_unichar(in template (value) any_type inpar,
 *                  in charstring string_serialization := "UTF-8",
 *                  in universal charstring encoding_info := "")
 *           return universal charstring
 * @author Arpad Lovassy
 * @author Kristof Szabados
 */
public final class EncvalueUnicharExpression extends Expression_Value {
	private static final String OPERAND1_ERROR1 = "Cannot determine the type of the 1st operand of the `encvalue_unichar' operation";
	private static final String OPERAND1_ERROR2 = "The 1st operand of the `encvalue_unichar' operation cannot be encoded";

	private static final String OPERAND2_ERROR1 = "The 2nd operand of the `encvalue_unichar' operation should be a charstring value";

	private static final String OPERAND3_ERROR1 = "The 3rd operand of the `encvalue_unichar' operation should be a universal charstring value";

	private final TemplateInstance templateInstance1;
	private final Value value2;
	private final Value value3;
	//FIXME missing support for forth parameter

	public EncvalueUnicharExpression(final TemplateInstance templateInstance1, final Value value2, final Value value3) {
		this.templateInstance1 = templateInstance1;
		this.value2 = value2;
		this.value3 = value3;

		if (templateInstance1 != null) {
			templateInstance1.setFullNameParent(this);
		}
		if (value2 != null) {
			value2.setFullNameParent(this);
		}
		if (value3 != null) {
			value3.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Operation_type getOperationType() {
		return Operation_type.ENCVALUE_UNICHAR_OPERATION;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReference(final CompilationTimeStamp timestamp, final Assignment lhs) {
		if (templateInstance1 != null && templateInstance1.getTemplateBody().checkExpressionSelfReferenceTemplate(timestamp, lhs)) {
			return true;
		}
		if (value2 != null && value2.checkExpressionSelfReferenceValue(timestamp, lhs)) {
			return true;
		}
		if (value3 != null && value3.checkExpressionSelfReferenceValue(timestamp, lhs)) {
			return true;
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("encvalue_unichar(");
		builder.append(templateInstance1.createStringRepresentation());
		builder.append(", ");
		builder.append(value2 == null ? "null" : value2.createStringRepresentation());
		builder.append(", ");
		builder.append(value3 == null ? "null" : value3.createStringRepresentation());
		builder.append(')');
		return builder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		if (templateInstance1 != null) {
			templateInstance1.setMyScope(scope);
		}
		if (value2 != null) {
			value2.setMyScope(scope);
		}
		if (value3 != null) {
			value3.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		super.setCodeSection(codeSection);

		if (templateInstance1 != null) {
			templateInstance1.setCodeSection(codeSection);
		}
		if (value2 != null) {
			value2.setCodeSection(codeSection);
		}
		if (value3 != null) {
			value3.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (templateInstance1 == child) {
			return builder.append(OPERAND1);
		} else if (value2 == child) {
			return builder.append(OPERAND2);
		} else if (value3 == child) {
			return builder.append(OPERAND3);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_UCHARSTRING;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		return true;
	}

	/**
	 * Checks the parameters of the expression and if they are valid in
	 * their position in the expression or not.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 */
	private void checkExpressionOperands( final CompilationTimeStamp timestamp,
			final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		//check template1
		checkExpressionOperand1(timestamp, expectedValue, referenceChain);
		//check value2
		checkExpressionOperand2(timestamp, expectedValue, referenceChain);
		//check value3
		checkExpressionOperand3(timestamp, expectedValue, referenceChain);
	}

	/**
	 * Checks the 1st operand
	 * in template (value) any_type
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 */
	private void checkExpressionOperand1( final CompilationTimeStamp timestamp,
			final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain ) {
		if (templateInstance1 == null) {
			setIsErroneous(true);
			return;
		}

		final Expected_Value_type internalExpectation = Expected_Value_type.EXPECTED_DYNAMIC_VALUE.equals(expectedValue) ? Expected_Value_type.EXPECTED_TEMPLATE
				: expectedValue;
		IType type = templateInstance1.getExpressionGovernor(timestamp, internalExpectation);
		ITTCN3Template template = templateInstance1.getTemplateBody();
		if (type == null) {
			template = template.setLoweridToReference(timestamp);
			type = template.getExpressionGovernor(timestamp, internalExpectation);
		}

		if (type == null) {
			if (!template.getIsErroneous(timestamp)) {
				templateInstance1.getLocation().reportSemanticError(OPERAND1_ERROR1);
			}
			setIsErroneous(true);
			return;
		}

		IsValueExpression.checkExpressionTemplateInstance(timestamp, this, templateInstance1, type, referenceChain, expectedValue);

		if (getIsErroneous(timestamp)) {
			return;
		}

		template.checkSpecificValue(timestamp, false);

		type = type.getTypeRefdLast(timestamp);
		switch (type.getTypetype()) {
		case TYPE_UNDEFINED:
		case TYPE_NULL:
		case TYPE_REFERENCED:
		case TYPE_VERDICT:
		case TYPE_PORT:
		case TYPE_COMPONENT:
		case TYPE_DEFAULT:
		case TYPE_SIGNATURE:
		case TYPE_FUNCTION:
		case TYPE_ALTSTEP:
		case TYPE_TESTCASE:
			if (!isErroneous) {
				location.reportSemanticError(OPERAND1_ERROR2);
				setIsErroneous(true);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Checks the 2nd operand
	 * in charstring (optional)
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 */
	private void checkExpressionOperand2( final CompilationTimeStamp timestamp,
			final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain ) {
		if (value2 == null) {
			return;
		}

		value2.setLoweridToReference(timestamp);
		final Type_type tempType = value2.getExpressionReturntype(timestamp, expectedValue);

		switch (tempType) {
		case TYPE_CHARSTRING:
			final IValue last = value2.getValueRefdLast(timestamp, expectedValue, referenceChain);
			if (!last.isUnfoldable(timestamp)) {
				final String originalString = ((Charstring_Value) last).getValue();
				final CharstringExtractor cs = new CharstringExtractor( originalString );
				if ( cs.isErrorneous() ) {
					value2.getLocation().reportSemanticError( cs.getErrorMessage() );
					setIsErroneous(true);
				}
			}

			break;
		case TYPE_UNDEFINED:
			setIsErroneous(true);
			break;
		default:
			if (!isErroneous) {
				location.reportSemanticError(OPERAND2_ERROR1);
				setIsErroneous(true);
			}
			break;
		}
	}

	/**
	 * Checks the 3rd operand
	 * in universal charstring (optional)
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 */
	private void checkExpressionOperand3( final CompilationTimeStamp timestamp,
			final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain ) {
		if (value3 == null) {
			return;
		}

		value3.setLoweridToReference(timestamp);
		final Type_type tempType = value3.getExpressionReturntype(timestamp, expectedValue);

		switch (tempType) {
		case TYPE_CHARSTRING:
		case TYPE_UCHARSTRING:
			break;
		case TYPE_UNDEFINED:
			setIsErroneous(true);
			break;
		default:
			if (!isErroneous) {
				location.reportSemanticError(OPERAND3_ERROR1);
				setIsErroneous(true);
			}
			break;
		}
	}

	@Override
	/** {@inheritDoc} */
	public IValue evaluateValue(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return lastValue;
		}

		isErroneous = false;
		lastTimeChecked = timestamp;
		lastValue = this;

		if (templateInstance1 != null) {
			checkExpressionOperands(timestamp, expectedValue, referenceChain);
		}

		return lastValue;
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			if (templateInstance1 != null) {
				referenceChain.markState();
				templateInstance1.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
			if (value2 != null) {
				referenceChain.markState();
				value2.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
			if (value3 != null) {
				referenceChain.markState();
				value3.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (templateInstance1 != null) {
			templateInstance1.updateSyntax(reparser, false);
			reparser.updateLocation(templateInstance1.getLocation());
		}

		if (value2 != null) {
			value2.updateSyntax(reparser, false);
			reparser.updateLocation(value2.getLocation());
		}

		if (value3 != null) {
			value3.updateSyntax(reparser, false);
			reparser.updateLocation(value3.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (templateInstance1 != null) {
			templateInstance1.findReferences(referenceFinder, foundIdentifiers);
		}
		if (value2 != null) {
			value2.findReferences(referenceFinder, foundIdentifiers);
		}
		if (value3 != null) {
			value3.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (templateInstance1 != null && !templateInstance1.accept(v)) {
			return false;
		}
		if (value2 != null && !value2.accept(v)) {
			return false;
		}
		if (value3 != null && !value3.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule) {
		if (templateInstance1 != null) {
			templateInstance1.reArrangeInitCode(aData, source, usageModule);
		}
		if (value2 != null) {
			value2.reArrangeInitCode(aData, source, usageModule);
		}
		if (value3 != null) {
			value3.reArrangeInitCode(aData, source, usageModule);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpressionExpression(final JavaGenData aData, final ExpressionStruct expression) {
		aData.addBuiltinTypeImport("TitanOctetString");
		aData.addCommonLibraryImport("AdditionalFunctions");

		final boolean isValue = templateInstance1.getTemplateBody().isValue(CompilationTimeStamp.getBaseTimestamp());

		final ExpressionStruct expression2 = new ExpressionStruct();
		if (isValue) {
			templateInstance1.getTemplateBody().getValue().generateCodeExpressionMandatory(aData, expression2, true);
		} else {
			templateInstance1.generateCode(aData, expression2, Restriction_type.TR_NONE);
		}

		String v2_code;
		if (value2 == null) {
			v2_code = "\"UTF-8\"";
		} else {
			final ExpressionStruct tempExpression = new ExpressionStruct();
			value2.generateCodeExpressionMandatory(aData, tempExpression, true);
			final String tempID = aData.getTemporaryVariableName();
			expression.preamble.append(MessageFormat.format("TitanCharString {0} = {1};\n", tempID, tempExpression.expression));
			expression.preamble.append(MessageFormat.format("if ({0}.operatorNotEquals(\"UTF-8\") && {0}.operatorNotEquals(\"UTF-16\") && {0}.operatorNotEquals(\"UTF-16LE\") && {0}.operatorNotEquals(\"UTF-16BE\") && {0}.operatorNotEquals(\"UTF-32\") && {0}.operatorNotEquals(\"UTF-32LE\") && {0}.operatorNotEquals(\"UTF-32BE\")) '{'\n", tempID));
			expression.preamble.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"decvalue_unichar: Invalid encoding parameter: '{'0'}'\", {0}));\n", tempID));
			expression.preamble.append("}\n");

			v2_code = tempID;
		}

		final Scope scope = templateInstance1.getTemplateBody().getMyScope();
		final IType governor = templateInstance1.getTemplateBody().getMyGovernor();
		if (expression2.preamble.length() > 0) {
			expression.postamble.append(expression2.preamble);
		}

		final ExpressionStruct expression3 = new ExpressionStruct();
		//FIXME handle 4th parameter
		expression3.expression.append(MessageFormat.format("{0}_default_coding", governor.getGenNameDefaultCoding(aData, expression.expression, scope)));

		final String tempID = aData.getTemporaryVariableName();
		expression.preamble.append(MessageFormat.format("TitanOctetString {0} = new TitanOctetString();\n", tempID));
		expression.preamble.append(MessageFormat.format("{0}_encoder({1}{2}, {3}, {4});\n", governor.getGenNameCoder(aData, expression.expression, scope), expression2.expression, isValue?"":".valueOf()", tempID, expression3.expression));
		expression.expression.append(MessageFormat.format("AdditionalFunctions.oct2unichar({0}, {1})", tempID, v2_code));
		if (expression2.postamble.length() > 0) {
			expression.postamble.append(expression2.postamble);
		}
		if (expression3.postamble.length() > 0) {
			expression.postamble.append(expression3.postamble);
		}
	}
}
