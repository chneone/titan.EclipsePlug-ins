/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimension;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimensions;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Port_Type extends Type {
	private static final String TEMPLATENOTALLOWED = "Template cannot be defined for port type `{0}''";

	private final PortTypeBody body;

	public Port_Type(final PortTypeBody body) {
		this.body = body;

		if (body != null) {
			body.setFullNameParent(this);
			body.setMyType(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_PORT;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (body != null) {
			body.setMyScope(scope);
		}
	}

	public PortTypeBody getPortBody() {
		return body;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);
		final IType temp = otherType.getTypeRefdLast(timestamp);
		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp)) {
			return true;
		}

		return this == otherType;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isIdentical(final CompilationTimeStamp timestamp, final IType type) {
		check(timestamp);
		type.check(timestamp);
		final IType temp = type.getTypeRefdLast(timestamp);
		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp)) {
			return true;
		}

		return this == temp;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetypeTtcn3() {
		if (isErroneous) {
			return Type_type.TYPE_UNDEFINED;
		}

		return getTypetype();
	}

	@Override
	/** {@inheritDoc} */
	public String getTypename() {
		return getFullName();
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "port.gif";
	}

	@Override
	/** {@inheritDoc} */
	public boolean isComponentInternal(final CompilationTimeStamp timestamp) {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		initAttributes(timestamp);

		if (body != null) {
			body.check(timestamp);
			final FormalParameterList mapParams = body.getMapParameters();
			if (mapParams != null) {
				mapParams.setMyDefinition((Definition)getDefiningAssignment());
				mapParams.setGenName(getGenNameOwn());
			}
			final FormalParameterList unmapParams = body.getUnmapParameters();
			if (unmapParams != null) {
				unmapParams.setMyDefinition((Definition)getDefiningAssignment());
				unmapParams.setGenName(getGenNameOwn());
			}
			if (withAttributesPath != null) {
				body.checkAttributes(timestamp, withAttributesPath);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkComponentInternal(final CompilationTimeStamp timestamp, final Set<IType> typeSet, final String operation) {
		location.reportSemanticError(MessageFormat.format("Port type `{0}'' cannot be {1}", getTypename(), operation));
	}

	@Override
	/** {@inheritDoc} */
	public void checkEmbedded(final CompilationTimeStamp timestamp, final Location errorLocation,
			final boolean defaultAllowed, final String errorMessage) {
		errorLocation.reportSemanticError(MessageFormat.format("Port type `{0}'' cannot be {1}", getTypename(), errorMessage));
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template,
			final boolean isModified, final boolean implicitOmit, final Assignment lhs) {
		registerUsage(template);
		template.getLocation().reportSemanticError(MessageFormat.format(TEMPLATENOTALLOWED, getFullName()));

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return this;
		}

		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(ArraySubReference.INVALIDSUBREFERENCE, getTypename()));
			return null;
		case fieldSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(),
							getTypename()));
			return null;
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference).getId().getDisplayName(),
							getTypename()));
			return null;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return null;
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("port");
	}

	@Override
	/** {@inheritDoc} */
	public void addProposal(final ProposalCollector propCollector, final int i) {
		if (body != null) {
			body.addProposal(propCollector, i);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (body != null) {
			body.updateSyntax(reparser, false);
			reparser.updateLocation(body.getLocation());
		}

		if (subType != null) {
			subType.updateSyntax(reparser, false);
		}

		if (withAttributesPath != null) {
			withAttributesPath.updateSyntax(reparser, false);
			reparser.updateLocation(withAttributesPath.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (body != null) {
			body.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (body!=null && !body.accept(v)) {
			return false;
		}
		return true;
	}

	/**
	 * This function generates and returns the name of the class representing the port type.
	 * In some cases this is generated by us, in other cases the user has to provide it.
	 *
	 * @param aData used to access build settings.
	 * @param source where the source code is to be generated.
	 * */
	public String getClassName(final JavaGenData aData, final StringBuilder source){
		return body.getClassName(aData, source);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source) {
		return getClassName(aData, source);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source) {
		ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous setting `" + getFullName() + "''");
		return "FATAL_ERROR encountered while processing `" + getFullName() + "''\n";
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTypeDescriptor(final JavaGenData aData, final StringBuilder source) {
		ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous setting `" + getFullName() + "''");
		return "FATAL_ERROR encountered while processing `" + getFullName() + "''\n";
	}

	@Override
	/** {@inheritDoc} */
	public boolean generatesOwnClass(JavaGenData aData, StringBuilder source) {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		if (lastTimeGenerated != null && !lastTimeGenerated.isLess(aData.getBuildTimstamp())) {
			return;
		}

		lastTimeGenerated = aData.getBuildTimstamp();

		body.generateCode(aData, source);
	}

	/**
	 * Generate the classes to represent a port array.
	 *
	 * @param aData only used to update imports if needed
	 * @param source where the source code should be generated
	 * @param dimensions the dimensions of the array to use.
	 */
	public String generateCodePort(final JavaGenData aData, final StringBuilder source, final ArrayDimensions dimensions) {
		final String typeName = aData.getTemporaryVariableName();
		String className = typeName;
		String elementName;

		for (int i = 0; i < dimensions.size(); i++) {
			final ArrayDimension dimension = dimensions.get(i);
			if (i == dimensions.size() - 1) {
				elementName = getGenNameValue(aData, source);
			} else {
				elementName = aData.getTemporaryVariableName();
			}

			source.append(MessageFormat.format("\tpublic static class {0} extends TitanPort_Array<{1}> '{'\n", className, elementName));
			source.append(MessageFormat.format("\t\tpublic {0}() '{'\n", className));
			source.append(MessageFormat.format("\t\t\tsuper({0}.class, {1} , {2});\n", elementName, dimension.getSize(), dimension.getOffset()));
			source.append("\t\t}\n");
			source.append(MessageFormat.format("\t\tpublic {0}({0} otherValue) '{'\n", className));
			source.append("\t\t\tsuper(otherValue);\n");
			source.append("\t\t}\n");

			PortGenerator.generatePortArrayBodyMembers(aData, source, body.generateDefinitionForCodeGeneration(aData, source), dimension.getSize(), dimension.getOffset());

			source.append("\t}\n\n");

			className = elementName;
		}

		aData.addBuiltinTypeImport("TitanPort_Array");

		return typeName;
	}
}
