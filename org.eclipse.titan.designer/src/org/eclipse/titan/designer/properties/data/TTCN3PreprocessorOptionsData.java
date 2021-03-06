/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.data;

import java.util.HashSet;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles the TTCN-3 preprocessor related property settings of projects. Also
 * loading, saving of these properties from and into external formats.
 * 
 * @author Kristof Szabados
 * */
public final class TTCN3PreprocessorOptionsData {
	public static final String TTCN3_PREPROCESSOR_PROPERTY = "TTCN3preprocessor";
	public static final String TTCN3_PREPROCESSOR_TAG = "TTCN3preprocessor";
	public static final String DEFAULT_PREPROCESSOR = "cpp";

	public static String getPreprocessorName(final IProject project) {
		try {
			String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TTCN3PreprocessorOptionsData.TTCN3_PREPROCESSOR_PROPERTY));
			if (temp == null || temp.length() == 0) {
				temp = "cpp";
			}
			return temp;
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While reading the preprecessor name of `" + project.getName() + "'", e);
			return "cpp";
		}
	}

	private TTCN3PreprocessorOptionsData() {
		// Do nothing
	}

	/**
	 * Remove the TITAN provided attributes from a project.
	 * 
	 * @param project
	 *                the project to remove the attributes from.
	 * */
	public static void removeTITANAttributes(final IProject project) {
		try {
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, TTCN3_PREPROCESSOR_PROPERTY), null);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While removing attributes from project `" + project.getName() + "'", e);
		}
	}

	/**
	 * Loads and sets the Makefile related settings contained in the XML
	 * tree for this project.
	 * 
	 * @see ProjectFileHandler#loadProjectSettings()
	 * @see ProjectBuildPropertyData#loadMakefileSettings(Node, IProject,
	 *      HashSet)
	 * 
	 * @param root
	 *                the root of the subtree containing Makefile related
	 *                attributes
	 * @param project
	 *                the project to set the found attributes on.
	 * */
	public static void loadMakefileSettings(final Node root, final IProject project) {
		final NodeList resourceList = root.getChildNodes();

		String newValue = DEFAULT_PREPROCESSOR;

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final Node node = resourceList.item(i);
			final String name = node.getNodeName();
			if (TTCN3_PREPROCESSOR_TAG.equals(name)) {
				newValue = node.getTextContent();
			}
		}

		try {
			final QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, TTCN3_PREPROCESSOR_PROPERTY);
			final String oldValue = project.getPersistentProperty(qualifiedName);
			if (newValue != null && !newValue.equals(oldValue)) {
				project.setPersistentProperty(qualifiedName, newValue);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While loading makefile settings of `" + project.getName() + "'", e);
		}
	}

	/**
	 * Creates an XML tree from the Makefile related settings of the
	 * project.
	 * 
	 * @see ProjectFileHandler#saveProjectSettings()
	 * @see ProjectBuildPropertyData#saveMakefileSettings(Document,
	 *      IProject)
	 * @see InternalMakefileCreationData#saveMakefileSettings(Element,
	 *      Document, IProject)
	 * 
	 * @param makefileSettings
	 *                the node to use as root node
	 * @param document
	 *                the document used for creating the tree nodes
	 * @param project
	 *                the project to work on
	 * */
	public static void saveMakefileSettings(final Element makefileSettings, final Document document, final IProject project) {
		Element node;
		String temp = null;
		try {
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, TTCN3_PREPROCESSOR_PROPERTY));
			if (temp != null && !DEFAULT_PREPROCESSOR.equals(temp)) {
				node = document.createElement(TTCN3_PREPROCESSOR_TAG);
				node.appendChild(document.createTextNode(temp));
				makefileSettings.appendChild(node);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While saving makefile settings of `" + project.getName() + "'", e);
		}
	}

	/**
	 * Copies the project information related to Makefiles from the source
	 * node to the target makefileSettings node.
	 * 
	 * @see ProjectFileHandler#copyProjectInfo(Node, Node, IProject,
	 *      TreeMap, TreeMap, boolean)
	 * 
	 * @param source
	 *                the node used as the source of the information.
	 * @param makefileSettings
	 *                the node used as the target of the operation.
	 * @param document
	 *                the document to contain the result, used to create the
	 *                XML nodes.
	 * @param saveDefaultValues
	 *                whether the default values should be forced to be
	 *                added to the output.
	 * */
	public static void copyMakefileSettings(final Node source, final Node makefileSettings, final Document document,
			final boolean saveDefaultValues) {
		final NodeList resourceList = source.getChildNodes();

		String newValue = DEFAULT_PREPROCESSOR;

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final Node node = resourceList.item(i);
			final String name = node.getNodeName();
			if (TTCN3_PREPROCESSOR_TAG.equals(name)) {
				newValue = node.getTextContent();
			}
		}

		if (saveDefaultValues || (newValue != null && !DEFAULT_PREPROCESSOR.equals(newValue))) {
			final Node node = document.createElement(TTCN3_PREPROCESSOR_TAG);
			node.appendChild(document.createTextNode(newValue));
			makefileSettings.appendChild(node);
		}
	}
}
