/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.titan.runtime.core.Base_Type.TTCN_Typedescriptor;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Any;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_AnyOrNone;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_ComplementList_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_List_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Omit;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Pattern;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_StringRange;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Unbound;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.expression_operand_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.type_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_behavior_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TitanCharString.CharCoding;

/**
 * TTCN-3 universal charstring template
 *
 * @author Arpad Lovassy
 * @author Farkas Izabella Ingrid
 * @author Andrea Palfi
 */
public class TitanUniversalCharString_template extends Restricted_Length_Template {
	static class Unichar_Decmatch {
		IDecode_Match dec_match;
		CharCoding coding;
	}

	protected TitanUniversalCharString single_value;

	private TitanCharString pattern_string;

	// value_list part
	private ArrayList<TitanUniversalCharString_template> value_list;

	// value range part
	private boolean min_is_set, max_is_set;
	private boolean min_is_exclusive, max_is_exclusive;
	private TitanUniversalChar min_value, max_value;

	/** originally pattern_value/regexp_init */
	private boolean pattern_value_regexp_init;

	/**
	 * java/perl style pattern converted from TTCN-3 charstring pattern
	 * originally pattern_value/posix_regexp
	 */
	private Pattern pattern_value_posix_regexp;

	/** originally pattern_value/nocase */
	private boolean pattern_value_nocase;

	private Unichar_Decmatch dec_match;

	/**
	 * Initializes to unbound/uninitialized template.
	 * */
	public TitanUniversalCharString_template() {
		// do nothing
	}

	/**
	 * Initializes to a given template kind.
	 *
	 * @param otherValue
	 *                the template kind to initialize to.
	 * */
	public TitanUniversalCharString_template(final template_sel otherValue) {
		super(otherValue);
		check_single_selection(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUniversalCharString_template(final String otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanUniversalCharString(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUniversalCharString_template(final TitanCharString otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.must_bound("Creating a template from an unbound charstring value.");

		single_value = new TitanUniversalCharString(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUniversalCharString_template(final TitanCharString_Element otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.must_bound("Creating a template from an unbound charstring value.");

		single_value = new TitanUniversalCharString(String.valueOf(otherValue.get_char()));
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUniversalCharString_template(final TitanUniversalCharString otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.must_bound("Creating a template from an unbound universal charstring value.");

		single_value = new TitanUniversalCharString(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUniversalCharString_template(final TitanUniversalCharString_Element otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.must_bound("Creating a template from an unbound universal charstring value.");

		single_value = new TitanUniversalCharString(otherValue);
	}

	/**
	 * Initializes to a given template.
	 *
	 * @param otherValue
	 *                the template to initialize to.
	 * */
	public TitanUniversalCharString_template(final TitanUniversalCharString_template otherValue) {
		copy_template(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 * Causes dynamic testcase error if the parameter is not present or omit.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public  TitanUniversalCharString_template(final Optional< TitanUniversalCharString> otherValue) {
		switch (otherValue.get_selection()) {
		case OPTIONAL_PRESENT:
			set_selection(template_sel.SPECIFIC_VALUE);
			single_value = new  TitanUniversalCharString(otherValue.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		case OPTIONAL_UNBOUND:
			throw new TtcnError("Creating a universal charstring template from an unbound optional field.");
		}
	}

	/**
	 * Initializes to a given template.
	 *
	 * @param otherValue
	 *                the template to initialize to.
	 * */
	public TitanUniversalCharString_template(final TitanCharString_template otherValue) {
		copy_template(otherValue);
	}

	public TitanUniversalCharString_template(final template_sel selValue, final TitanCharString otherValue) {
		this(selValue, otherValue, false);
	}

	public TitanUniversalCharString_template(final template_sel selValue, final TitanCharString otherValue, final boolean nocase) {
		super(template_sel.STRING_PATTERN);
		if (selValue != template_sel.STRING_PATTERN) {
			throw new TtcnError("Internal error: Initializing a universal charstring pattern template with invalid selection.");
		}

		pattern_string = new TitanCharString(otherValue);
		pattern_value_regexp_init = false;
		pattern_value_nocase = nocase;
	}

	@Override
	public void clean_up() {
		switch (template_selection) {
		case SPECIFIC_VALUE:
			single_value = null;
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list.clear();
			value_list = null;
			break;
		case VALUE_RANGE:
			min_value = null;
			max_value = null;
			break;
		case STRING_PATTERN:
			pattern_value_regexp_init = false;
			pattern_value_posix_regexp = null;
			pattern_string = null;
			break;
		case DECODE_MATCH:
			dec_match = null;
			break;
		default:
			break;
		}
		template_selection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	// originally operator=
	@Override
	public TitanUniversalCharString_template operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanUniversalCharString) {
			return operator_assign((TitanUniversalCharString) otherValue);
		} else if (otherValue instanceof TitanCharString) {
			return operator_assign((TitanCharString) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to universal charstring", otherValue));
	}

	@Override
	public TitanUniversalCharString_template operator_assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanUniversalCharString_template) {
			return operator_assign((TitanUniversalCharString_template) otherValue);
		} else if (otherValue instanceof TitanCharString_template) {
			return operator_assign((TitanCharString_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to universal charstring template", otherValue));
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanUniversalCharString) {
			log_match((TitanUniversalCharString) match_value, legacy);
			return;
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to universalcharstring", match_value));
	}

	@Override
	public TitanUniversalCharString_template operator_assign(final template_sel otherValue) {
		check_single_selection(otherValue);
		clean_up();
		set_selection(otherValue);

		return this;
	}

	// originally operator=
	public TitanUniversalCharString_template operator_assign(final String otherValue) {
		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanUniversalCharString(otherValue);

		return this;
	}

	// originally operator=
	public TitanUniversalCharString_template operator_assign(final TitanUniversalCharString otherValue) {
		otherValue.must_bound("Assignment of an unbound universal charstring value to a template.");

		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanUniversalCharString(otherValue);

		return this;
	}

	public TitanUniversalCharString_template operator_assign(final TitanUniversalCharString_Element otherValue) {
		otherValue.must_bound("Assignment of an unbound universal charstring element to a template.");

		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanUniversalCharString(otherValue);

		return this;
	}

	public TitanUniversalCharString_template operator_assign(final TitanCharString otherValue) {
		otherValue.must_bound("Assignment of an unbound charstring value to a template.");

		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanUniversalCharString(otherValue);

		return this;
	}

	public TitanUniversalCharString_template operator_assign(final TitanCharString_Element otherValue) {
		otherValue.must_bound("Assignment of an unbound charstring element to a universal charstring template.");

		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanUniversalCharString(String.valueOf(otherValue.get_char()));

		return this;
	}

	public TitanUniversalCharString_template operator_assign(final TitanCharString_template otherValue) {
		clean_up();
		copy_template(otherValue);

		return this;
	}

	// originally operator=
	public TitanUniversalCharString_template operator_assign(final TitanUniversalCharString_template otherValue) {
		if (otherValue != this) {
			clean_up();
			copy_template(otherValue);
		}

		return this;
	}


	private void copy_template(final TitanCharString_template otherValue) {
		switch (otherValue.template_selection) {
		case SPECIFIC_VALUE:
			single_value = new TitanUniversalCharString(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanUniversalCharString_template>(otherValue.value_list.size());
			for (int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanUniversalCharString_template temp = new TitanUniversalCharString_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		case VALUE_RANGE:
			if (!otherValue.min_is_set) {
				throw new TtcnError("The lower bound is not set when copying a charstring value range template to a universal charstring template.");
			}
			if (!otherValue.max_is_set) {
				throw new TtcnError("The upper bound is not set when copying a charstring value range template to a universal charstring template.");
			}
			min_is_set = true;
			max_is_set = true;
			min_is_exclusive = otherValue.min_is_exclusive;
			max_is_exclusive = otherValue.max_is_exclusive;
			min_value = new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.min_value.get_at(0).get_char());
			max_value =  new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.max_value.get_at(0).get_char());
			break;
		case STRING_PATTERN:
			pattern_string = new TitanCharString(otherValue.single_value);
			pattern_value_regexp_init = false;
			pattern_value_nocase = otherValue.pattern_value_nocase;
			break;
		case DECODE_MATCH:
			dec_match = otherValue.dec_match;
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported charstring template to a universal charstring template.");
		}

		set_selection(otherValue);
	}

	private void copy_template(final TitanUniversalCharString_template otherValue) {
		switch (otherValue.template_selection) {
		case SPECIFIC_VALUE:
			single_value = new TitanUniversalCharString(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanUniversalCharString_template>(otherValue.value_list.size());
			for (int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanUniversalCharString_template temp = new TitanUniversalCharString_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		case VALUE_RANGE:
			min_is_set = otherValue.min_is_set;
			min_is_exclusive = otherValue.min_is_exclusive;
			if (min_is_set) {
				min_value = new TitanUniversalChar(otherValue.min_value);
			}
			max_is_set = otherValue.max_is_set;
			max_is_exclusive = otherValue.max_is_exclusive;
			if (max_is_set) {
				max_value = new TitanUniversalChar(otherValue.max_value);
			}
			break;
		case STRING_PATTERN:
			pattern_string = new TitanCharString(otherValue.pattern_string);
			pattern_value_regexp_init = false;
			pattern_value_nocase = otherValue.pattern_value_nocase;
			break;
		case DECODE_MATCH:
			dec_match = otherValue.dec_match;
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported universal charstring template.");
		}

		set_selection(otherValue);
	}

	/**
	 * Gives access to the given element. Indexing begins from zero.
	 * Over-indexing by 1 extends the universal charstring.
	 *
	 * Causes dynamic testcase error if the template is not a specific value.
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this universal charstring
	 * */
	public TitanUniversalCharString_Element get_at(final int index) {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a universal charstring element of a non-specific universal charstring template.");
		}

		return single_value.get_at(index);
	}

	/**
	 * Gives access to the given element. Indexing begins from zero.
	 * Over-indexing by 1 extends the universal charstring.
	 *
	 * Causes dynamic testcase error if the template is not a specific value.
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this universal charstring
	 * */
	public TitanUniversalCharString_Element get_at(final TitanInteger index) {
		index.must_bound("Indexing a universal charstring template with an unbound integer value.");

		return get_at(index.get_int());
	}

	/**
	 * Gives read-only access to the given element.
	 *
	 * Index underflow and overflow causes dynamic test case error.
	 * Also if the template is not a specific value template
	 *
	 * const operator[] const in the core.
	 *
	 * @param index
	 *            the index of the element to return.
	 * @return the element at the specified position in this universal charstring
	 * */
	public TitanUniversalCharString_Element constGet_at(final int index) {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a universal charstring element of a non-specific universal charstring template.");
		}

		return single_value.constGet_at(index);
	}

	/**
	 * Gives read-only access to the given element.
	 *
	 * Index underflow and overflow causes dynamic test case error.
	 * Also if the template is not a specific value template
	 *
	 * const operator[] const in the core.
	 *
	 * @param index
	 *            the index of the element to return.
	 * @return the element at the specified position in this universal charstring
	 * */
	public TitanUniversalCharString_Element constGet_at(final TitanInteger index) {
		index.must_bound("Indexing a universal charstring template with an unbound integer value.");

		return constGet_at(index.get_int());
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanUniversalCharString) {
			return match((TitanUniversalCharString) otherValue, legacy);
		} else if (otherValue instanceof TitanCharString) {
			return match(new TitanUniversalCharString((TitanCharString) otherValue), legacy);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to universal charstring", otherValue));
	}

	/**
	 * Matches the provided value against this template.
	 *
	 * @param otherValue the value to be matched.
	 * */
	public boolean match(final TitanUniversalCharString otherValue) {
		return match(otherValue, false);
	}

	/**
	 * Matches the provided value against this template. In legacy mode
	 * omitted value fields are not matched against the template field.
	 *
	 * @param otherValue
	 *                the value to be matched.
	 * @param legacy
	 *                use legacy mode.
	 * */
	public boolean match(final TitanUniversalCharString otherValue, final boolean legacy) {
		if (!otherValue.is_bound()) {
			return false;
		}

		final List<TitanUniversalChar> otherStr = otherValue.get_value();
		final int otherLen = otherStr.size();
		if (!match_length(otherLen)) {
			return false;
		}

		switch (template_selection) {
		case SPECIFIC_VALUE:
			return single_value.operator_equals(otherValue);
		case OMIT_VALUE:
			return false;
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for (int i = 0; i < value_list.size(); i++) {
				if (value_list.get(i).match(otherValue, legacy)) {
					return template_selection == template_sel.VALUE_LIST;
				}
			}
			return template_selection == template_sel.COMPLEMENTED_LIST;
		case VALUE_RANGE:
			if (!min_is_set) {
				throw new TtcnError("The lower bound is not set when " +
						"matching with a universal charstring value range template.");
			}
			if (!max_is_set) {
				throw new TtcnError("The upper bound is not set when " +
						"matching with a universal charstring value range template.");
			}
			for (int i = 0; i < otherLen; i++) {
				final TitanUniversalChar uc = otherStr.get(i);
				if (uc.is_less_than(min_value) || max_value.is_less_than(uc)) {
					return false;
				} else if ((min_is_exclusive && uc.operator_equals(min_value)) || (max_is_exclusive && uc.operator_equals(max_value))) {
					return false;
				}
			}
			return true;

		case STRING_PATTERN:
			if (!pattern_value_regexp_init) {
				pattern_value_posix_regexp = TTCN_Pattern.convert_pattern(pattern_string.get_value().toString(), pattern_value_nocase);
			}
			if (pattern_value_posix_regexp != null) {
				return TTCN_Pattern.match(otherValue.to_utf(), pattern_value_posix_regexp, pattern_value_nocase);
			}
			throw new TtcnError(MessageFormat.format("Cannot convert pattern \"{0}\" to POSIX-equivalent.", pattern_string.get_value().toString()));
		case DECODE_MATCH: {
			TTCN_EncDec.set_error_behavior(error_type.ET_ALL, error_behavior_type.EB_WARNING);
			TTCN_EncDec.clear_error();
			final TTCN_Buffer buffer = new TTCN_Buffer();
			switch (dec_match.coding) {
			case UTF_8:
				otherValue.encode_utf8(buffer, false);
				break;
			case UTF16:
			case UTF16LE:
			case UTF16BE:
				otherValue.encode_utf16(buffer, dec_match.coding);
				break;
			case UTF32:
			case UTF32LE:
			case UTF32BE:
				otherValue.encode_utf32(buffer, dec_match.coding);
				break;
			default:
				throw new TtcnError("Internal error: Invalid string serialization for universal charstring template with decoded content matching.");
			}
			final boolean ret_val = dec_match.dec_match.match(buffer);
			TTCN_EncDec.set_error_behavior(error_type.ET_ALL, error_behavior_type.EB_DEFAULT);
			TTCN_EncDec.clear_error();
			return ret_val;
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported universal charstring template.");
		}
	}

	/**
	 * Matches the provided value against this template. In legacy mode
	 * omitted value fields are not matched against the template field.
	 *
	 * @param otherValue
	 *                the value to be matched.
	 * @param legacy
	 *                use legacy mode.
	 * */
	public boolean match(final TitanUniversalCharString_Element otherValue, final boolean legacy) {
		return match(new TitanUniversalCharString(otherValue), legacy);
	}

	/**
	 * Returns the number of elements, that is, the largest used index plus
	 * one and zero for the empty value.
	 *
	 * lengthof in the core
	 *
	 * @return the number of elements.
	 * */
	public TitanInteger lengthof() {
		if (is_ifPresent) {
			throw new TtcnError("Performing lengthof() operation on a universal charstring template which has an ifpresent attribute.");
		}

		int min_length;
		boolean has_any_or_none;
		switch (template_selection) {
		case SPECIFIC_VALUE:
			min_length = single_value.lengthof().get_int();
			has_any_or_none = false;
			break;
		case OMIT_VALUE:
			throw new TtcnError("Performing lengthof() operation on a universal charstring template containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
		case VALUE_RANGE:
			min_length = 0;
			has_any_or_none = true; // max. length is infinity
			break;
		case VALUE_LIST: {
			// error if any element does not have length or the
			// lengths differ
			if (value_list.isEmpty()) {
				throw new TtcnError("Internal error: Performing lengthof() operation on a universal charstring template containing an empty list.");
			}
			final int item_length = value_list.get(0).lengthof().get_int();
			for (int i = 1; i < value_list.size(); ++i) {
				if (value_list.get(i).lengthof().get_int() != item_length) {
					throw new TtcnError("Performing lengthof() operation on a universal charstring template containing a value list with different lengths.");
				}
			}
			min_length = item_length;
			has_any_or_none = false;
			break;
		}
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing lengthof() operation on a universal charstring template containing complemented list.");
		case STRING_PATTERN:
			throw new TtcnError("Performing lengthof() operation on a universal charstring template containing a pattern is not allowed.");
		default:
			throw new TtcnError("Performing lengthof() operation on an uninitialized/unsupported universal charstring template.");
		}

		return new TitanInteger(check_section_is_single(min_length, has_any_or_none, "length", "a", "universal charstring template"));
	}

	@Override
	public TitanUniversalCharString valueof() {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific universal charstring template.");
		}

		return single_value;
	}

	@Override
	public void set_type(final template_sel otherValue, final int lenght) {
		clean_up();
		switch (otherValue) {
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			set_selection(otherValue);
			value_list = new ArrayList<TitanUniversalCharString_template>(lenght);
			for (int i = 0; i < lenght; ++i) {
				value_list.add(new TitanUniversalCharString_template());
			}
			break;
		case VALUE_RANGE:
			set_selection(template_sel.VALUE_RANGE);
			min_is_set = false;
			max_is_set = false;
			min_is_exclusive = false;
			max_is_exclusive = false;
			break;
		case DECODE_MATCH:
			set_selection(template_sel.DECODE_MATCH);
			break;
		default:
			throw new TtcnError("Setting an invalid type for a universal charstring template.");
		}
	}

	@Override
	public int n_list_elem() {
		if (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list universal charstring template.");
		}

		return value_list.size();
	}

	@Override
	public TitanUniversalCharString_template list_item(final int listIndex) {
		if (template_selection != template_sel.VALUE_LIST &&
				template_selection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list universal charstring template.");
		}
		if (listIndex < 0) {
			throw new TtcnError("Accessing an universal charstring value list template using a negative index (" + listIndex + ").");
		}
		if (listIndex >= value_list.size()) {
			throw new TtcnError("Index overflow in a universal charstring value list template.");
		}

		return value_list.get(listIndex);
	}

	public void set_min(final TitanUniversalCharString minValue) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the lower bound for a non-range universal charstring template.");
		}

		minValue.must_bound("Setting an unbound value as lower bound in a universal charstring value range template.");
		final int length = minValue.lengthof().get_int();
		if (length != 1) {
			throw new TtcnError("The length of the lower bound in a universal charstring value range template must be 1 instead of " + length);
		}

		min_is_set = true;
		min_is_exclusive = false;
		min_value = minValue.get_at(0).get_char();

		if (max_is_set && max_value.is_less_than(min_value)) {
			throw new TtcnError("The lower bound in a universal charstring value range template is greater than the upper bound.");
		}
	}

	public void set_max(final TitanUniversalCharString maxValue) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the upper bound for a non-range universal charstring template.");
		}

		maxValue.must_bound("Setting an unbound value as upper bound in a universal charstring value range template.");
		final int length = maxValue.lengthof().get_int();
		if (length != 1) {
			throw new TtcnError("The length of the upper bound in a universal charstring value range template must be 1 instead of " + length);
		}

		max_is_set = true;
		max_is_exclusive = false;
		max_value = maxValue.get_at(0).get_char();

		if (min_is_set && max_value.is_less_than(min_value)) {
			throw new TtcnError("The upper bound in a universal charstring value range template is smaller than the lower bound.");
		}
	}

	public void set_min(final String minValue) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the lower bound for a non-range universal charstring template.");
		}

		final int length = minValue.length();
		if (length != 1) {
			throw new TtcnError("The length of the lower bound in a universal charstring value range template must be 1 instead of " + length);
		}

		min_is_set = true;
		min_is_exclusive = false;
		min_value = new TitanUniversalChar((char) 0, (char) 0, (char) 0, minValue.charAt(0));

		if (max_is_set && max_value.is_less_than(min_value)) {
			throw new TtcnError("The lower bound in a universal charstring value range template is greater than the upper bound.");
		}
	}

	public void set_max(final String maxValue) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the upper bound for a non-range universal charstring template.");
		}

		final int length = maxValue.length();
		if (length != 1) {
			throw new TtcnError("The length of the upper bound in a universal charstring value range template must be 1 instead of " + length);
		}

		max_is_set = true;
		max_is_exclusive = false;
		max_value = new TitanUniversalChar((char) 0, (char) 0, (char) 0, maxValue.charAt(0));

		if (min_is_set && max_value.is_less_than(min_value)) {
			throw new TtcnError("The upper bound in a universal charstring value range template is smaller than the lower bound.");
		}
	}


	public void set_min(final TitanCharString minValue) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the lower bound for a non-range universal charstring template.");
		}

		minValue.must_bound("Setting an unbound value as lower bound in a universal charstring value range template.");
		final int length = minValue.lengthof().get_int();
		if (length != 1) {
			throw new TtcnError("The length of the lower bound in a universal charstring value range template must be 1 instead of " + length);
		}

		min_is_set = true;
		min_is_exclusive = false;
		min_value = new TitanUniversalChar((char) 0, (char) 0, (char) 0, minValue.get_at(0).get_char());

		if (max_is_set && max_value.is_less_than(min_value)) {
			throw new TtcnError("The lower bound in a universal charstring value range template is greater than the upper bound.");
		}
	}

	public void set_max(final TitanCharString maxValue) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the upper bound for a non-range universal charstring template.");
		}

		maxValue.must_bound("Setting an unbound value as upper bound in a universal charstring value range template.");
		final int length = maxValue.lengthof().get_int();
		if (length != 1) {
			throw new TtcnError("The length of the upper bound in a universal charstring value range template must be 1 instead of " + length);
		}

		max_is_set = true;
		max_is_exclusive = false;
		max_value = new TitanUniversalChar((char) 0, (char) 0, (char) 0, maxValue.get_at(0).get_char());

		if (min_is_set && max_value.is_less_than(min_value)) {
			throw new TtcnError("The upper bound in a universal charstring value range template is smaller than the lower bound.");
		}
	}

	public void set_min_exclusive(final boolean minExclusive) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the lower bound exclusiveness for a non-range universal charstring template.");
		}

		min_is_exclusive = minExclusive;
	}

	public void set_max_exclusive(final boolean maxExclusive) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the upper bound exclusiveness for a non-range universal charstring template.");
		}

		max_is_exclusive = maxExclusive;
	}

	public void set_decmatch(final IDecode_Match dec_match) {
		set_decmatch(dec_match, (String)null);
	}

	public void set_decmatch(final IDecode_Match dec_match, final String codingString) {
		if (template_selection != template_sel.DECODE_MATCH) {
			throw new TtcnError("Setting the decoded content matching mechanism of a non-decmatch universal charstring template.");
		}

		final CharCoding new_coding = TitanUniversalCharString.get_character_coding(codingString, "decoded content match");
		this.dec_match = new Unichar_Decmatch();
		this.dec_match.dec_match = dec_match;
		this.dec_match.coding = new_coding;
	}

	public void set_decmatch(final IDecode_Match dec_match, final TitanCharString codingString) {
		set_decmatch(dec_match, codingString.get_value().toString());
	}

	public Object get_decmatch_dec_res() {
		if (template_selection != template_sel.DECODE_MATCH) {
			throw new TtcnError("Retrieving the decoding result of a non-decmatch universal charstring template.");
		}

		return dec_match.dec_match.get_dec_res();
	}

	public CharCoding get_decmatch_str_enc() {
		if (template_selection != template_sel.DECODE_MATCH) {
			throw new TtcnError("Retrieving the encoding format of a non-decmatch universal charstring template.");
		}

		return dec_match.coding;
	}

	public TTCN_Typedescriptor get_decmatch_type_descr() {
		if (template_selection != template_sel.DECODE_MATCH) {
			throw new TtcnError("Retrieving the decoded type's descriptor in a non-decmatch universal charstring template.");
		}

		return dec_match.dec_match.get_type_descr();
	}

	@Override
	public void log() {
		switch (template_selection) {
		case STRING_PATTERN:
			TitanCharString_template.log_pattern(pattern_string.lengthof().get_int(), pattern_string.get_value().toString(), pattern_value_nocase);
			break;
		case SPECIFIC_VALUE: {
			single_value.log();
			break;
		}
		case COMPLEMENTED_LIST:
			TTCN_Logger.log_event_str("complement");
		case VALUE_LIST:
			TTCN_Logger.log_char('(');
			for (int i = 0; i < value_list.size(); i++) {
				if (i > 0) {
					TTCN_Logger.log_event_str(", ");
				}
				value_list.get(i).log();
			}
			TTCN_Logger.log_char(')');
			break;
		case VALUE_RANGE:
			TTCN_Logger.log_char('(');
			if (min_is_exclusive) {
				TTCN_Logger.log_char('!');
			}
			if (min_is_set) {
				if (TitanUniversalCharString.is_printable(min_value)) {
					TTCN_Logger.log_char('"');
					TTCN_Logger.log_char_escaped(min_value.getUc_cell());
					TTCN_Logger.log_char('"');

				} else {
					TTCN_Logger.log_event(MessageFormat.format("char({0}, {1}, {2}, {3})", (int)min_value.getUc_group(), (int)min_value.getUc_plane(), (int)min_value.getUc_row(), (int)min_value.getUc_cell()));
				}
			} else {
				TTCN_Logger.log_event_str("<unknown lower bound>");
			}
			TTCN_Logger.log_event_str(" .. ");
			if (max_is_exclusive) {
				TTCN_Logger.log_char('!');
			}
			if (max_is_set) {
				if (TitanUniversalCharString.is_printable(max_value)) {
					TTCN_Logger.log_char('"');
					TTCN_Logger.log_char_escaped(max_value.getUc_cell());
					TTCN_Logger.log_char('"');

				} else {
					TTCN_Logger.log_event(MessageFormat.format("char({0}, {1}, {2}, {3})", (int)max_value.getUc_group(), (int)max_value.getUc_plane(), (int)max_value.getUc_row(), (int)max_value.getUc_cell()));
				}
			} else {
				TTCN_Logger.log_event_str("<unknown upper bound>");
			}

			TTCN_Logger.log_char(')');
			break;
		case DECODE_MATCH:
			TTCN_Logger.log_event_str("decmatch(");
			switch (dec_match.coding) {
			case UTF_8:
				TTCN_Logger.log_event_str("UTF-8");
				break;
			case UTF16:
				TTCN_Logger.log_event_str("UTF-16");
				break;
			case UTF16LE:
				TTCN_Logger.log_event_str("UTF-16LE");
				break;
			case UTF16BE:
				TTCN_Logger.log_event_str("UTF-16BE");
				break;
			case UTF32:
				TTCN_Logger.log_event_str("UTF-32");
				break;
			case UTF32LE:
				TTCN_Logger.log_event_str("UTF-32LE");
				break;
			case UTF32BE:
				TTCN_Logger.log_event_str("UTF-32BE");
				break;
			default:
				TTCN_Logger.log_event_str("<unknown coding>");
				break;
			}
			TTCN_Logger.log_event_str(")");
			dec_match.dec_match.log();
			break;
		default:
			log_generic();
			break;
		}
		log_restricted();
		log_ifpresent();
	}

	/**
	 * Logs the matching of the provided value to this template, to help
	 * identify the reason for mismatch. In legacy mode omitted value fields
	 * are not matched against the template field.
	 *
	 * @param match_value
	 *                the value to be matched.
	 * @param legacy
	 *                use legacy mode.
	 * */
	public void log_match(final TitanUniversalCharString match_value, final boolean legacy) {
		if (TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity()
				&& TTCN_Logger.get_logmatch_buffer_len() != 0) {
			TTCN_Logger.print_logmatch_buffer();
			TTCN_Logger.log_event_str(" := ");
		}
		match_value.log();
		TTCN_Logger.log_event_str(" with ");
		log();
		if (match(match_value)) {
			TTCN_Logger.log_event_str(" matched");
		} else {
			TTCN_Logger.log_event_str(" unmatched");
		}
	}

	@Override
	/** {@inheritDoc} */
	public void set_param(Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_TEMPLATE.getValue()|basic_check_bits_t.BC_LIST.getValue(), "universal charstring template");

		// Originally RT2
		if (param.get_type() == Module_Parameter.type_t.MP_Reference) {
			param = param.get_referenced_param().get();
		}

		switch (param.get_type()) {
		case MP_Omit:
			this.operator_assign(template_sel.OMIT_VALUE);
			break;
		case MP_Any:
			this.operator_assign(template_sel.ANY_VALUE);
			break;
		case MP_AnyOrNone:
			this.operator_assign(template_sel.ANY_OR_OMIT);
			break;
		case MP_List_Template:
		case MP_ComplementList_Template: {
			final TitanUniversalCharString_template temp = new TitanUniversalCharString_template();
			temp.set_type(param.get_type() == type_t.MP_List_Template ? template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST, param.get_size());
			for (int i = 0; i < param.get_size(); i++) {
				temp.list_item(i).set_param(param.get_elem(i));
			}
			this.operator_assign(temp);
			break;
		}
		case MP_Charstring: {
			this.operator_assign(param.get_charstring());
			break;
		}
		case MP_Universal_Charstring:
			this.operator_assign((TitanUniversalCharString)param.get_string_data());
			break;
		case MP_StringRange: {
			final TitanUniversalChar lower_uchar = param.get_lower_uchar();
			final TitanUniversalChar upper_uchar = param.get_upper_uchar();
			clean_up();
			set_selection(template_sel.VALUE_RANGE);
			min_is_set = true;
			max_is_set = true;
			min_value = lower_uchar;
			max_value = upper_uchar;
			set_min_exclusive(param.get_is_min_exclusive());
			set_max_exclusive(param.get_is_max_exclusive());
			break;
		}
		case MP_Pattern:
			clean_up();
			pattern_string = new TitanCharString(param.get_pattern());
			pattern_value_regexp_init = false;
			pattern_value_nocase = param.get_nocase();
			set_selection(template_sel.STRING_PATTERN);
			break;
		case MP_Expression:
			if (param.get_expr_type() == expression_operand_t.EXPR_CONCATENATE) {
				final TitanUniversalCharString operand1 = new TitanUniversalCharString();
				final TitanUniversalCharString operand2 = new TitanUniversalCharString();
				final boolean nocase = false;
				final boolean is_pattern = operand1.set_param_internal(param.get_operand1(), true, nocase);
				operand2.set_param(param.get_operand2());
				final TitanUniversalCharString result = operand1.operator_concatenate(operand2);
				if (is_pattern) {
					clean_up();
					if (result.charstring) {
						pattern_string = new TitanCharString(result.cstr);
					} else {
						pattern_string = new TitanCharString(result.get_stringRepr_for_pattern());
					}
					pattern_value_regexp_init = false;
					pattern_value_nocase = param.get_nocase();
					set_selection(template_sel.STRING_PATTERN);
				} else {
					this.operator_assign(result);
				}
			} else {
				param.expr_type_error("a charstring");
			}
			break;
		default:
			param.type_error("universal charstring template");
			break;
		}
		is_ifPresent = param.get_ifpresent();
		if (param.get_length_restriction() != null) {
			set_length_range(param);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Module_Parameter get_param(final Module_Param_Name param_name) {
		Module_Parameter mp = null;
		switch (template_selection) {
		case UNINITIALIZED_TEMPLATE:
			mp = new Module_Param_Unbound();
			break;
		case OMIT_VALUE:
			mp = new Module_Param_Omit();
			break;
		case ANY_VALUE:
			mp = new Module_Param_Any();
			break;
		case ANY_OR_OMIT:
			mp = new Module_Param_AnyOrNone();
			break;
		case SPECIFIC_VALUE:
			mp = single_value.get_param(param_name);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			if (template_selection == template_sel.VALUE_LIST) {
				mp = new Module_Param_List_Template();
			} else {
				mp = new Module_Param_ComplementList_Template();
			}
			for (int i = 0; i < value_list.size(); ++i) {
				mp.add_elem(value_list.get(i).get_param(param_name));
			}
			break;
		}
		case VALUE_RANGE:
			mp = new Module_Param_StringRange(min_value, max_value, min_is_exclusive, max_is_exclusive);
			break;
		case STRING_PATTERN:
			mp = new Module_Param_Pattern(pattern_string.get_value().toString(), pattern_value_nocase);
			break;
		case DECODE_MATCH:
			throw new TtcnError("Referencing a decoded content matching template is not supported.");
		default:
			throw new TtcnError("Referencing an uninitialized/unsupported universal charstring template.");
		}
		if (is_ifPresent) {
			mp.set_ifpresent();
		}
		mp.set_length_restriction(get_length_range());
		return mp;
	}

	@Override
	/** {@inheritDoc} */
	public boolean match_omit(final boolean legacy) {
		if (is_ifPresent) {
			return true;
		}

		switch (template_selection) {
		case OMIT_VALUE:
		case ANY_OR_OMIT:
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			if (legacy) {
				// legacy behavior: 'omit' can appear in the value/complement list
				for (int i = 0; i < value_list.size(); i++) {
					if (value_list.get(i).match_omit()) {
						return template_selection == template_sel.VALUE_LIST;
					}
				}
				return template_selection == template_sel.COMPLEMENTED_LIST;
			}
			return false;
		default:
			return false;
		}
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		encode_text_restricted(text_buf);

		switch (template_selection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			single_value.encode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			text_buf.push_int(value_list.size());
			for (int i = 0; i < value_list.size(); i++) {
				value_list.get(i).encode_text(text_buf);
			}
			break;
		case VALUE_RANGE:
			if (!min_is_set) {
				throw new TtcnError("Text encoder: The lower bound is not set in a universal charstring value range template.");
			}
			if (!max_is_set) {
				throw new TtcnError("Text encoder: The upper bound is not set in a universal charstring value range template.");
			}

			final byte[] buf = new byte[8];
			buf[0] = (byte)min_value.getUc_group();
			buf[1] = (byte)min_value.getUc_plane();
			buf[2] = (byte)min_value.getUc_row();
			buf[3] = (byte)min_value.getUc_cell();
			buf[4] = (byte)max_value.getUc_group();
			buf[5] = (byte)max_value.getUc_plane();
			buf[6] = (byte)max_value.getUc_row();
			buf[7] = (byte)max_value.getUc_cell();
			text_buf.push_raw(buf);
			break;
		case STRING_PATTERN:
			text_buf.push_int(pattern_value_nocase ? 1 : 0);
			pattern_string.encode_text(text_buf);
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized/unsupported universal charstring template.");
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		clean_up();
		decode_text_restricted(text_buf);

		switch (template_selection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			single_value = new TitanUniversalCharString();
			single_value.decode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int size = text_buf.pull_int().get_int();
			value_list = new ArrayList<TitanUniversalCharString_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanUniversalCharString_template temp = new TitanUniversalCharString_template();
				temp.decode_text(text_buf);
				value_list.add(temp);
			}
			break;
		}
		case VALUE_RANGE:
			final byte[] buf = new byte[8];
			text_buf.pull_raw(8, buf);
			min_value = new TitanUniversalChar((char)buf[0], (char)buf[1], (char)buf[2], (char)buf[3]);
			max_value = new TitanUniversalChar((char)buf[4], (char)buf[5], (char)buf[6], (char)buf[7]);
			min_is_set = true;
			max_is_set = true;
			min_is_exclusive = false;
			max_is_exclusive = false;
			break;
		case STRING_PATTERN:
			pattern_value_regexp_init = false;
			pattern_value_nocase = text_buf.pull_int().get_int() == 1;
			pattern_string = new TitanCharString();
			pattern_string.decode_text(text_buf);
			break;
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received for a universal charstring template.");
		}
	}

	@Override
	public void check_restriction(final template_res restriction, final String name, final boolean legacy) {
		if (template_selection == template_sel.UNINITIALIZED_TEMPLATE) {
			return;
		}

		switch ((name != null && restriction == template_res.TR_VALUE) ? template_res.TR_OMIT : restriction) {
		case TR_VALUE:
			if (!is_ifPresent && template_selection == template_sel.SPECIFIC_VALUE) {
				return;
			}
			break;
		case TR_OMIT:
			if (!is_ifPresent && (template_selection == template_sel.OMIT_VALUE || template_selection == template_sel.SPECIFIC_VALUE)) {
				return;
			}
			break;
		case TR_PRESENT:
			if (!match_omit(legacy)) {
				return;
			}
			break;
		default:
			return;
		}

		throw new TtcnError(MessageFormat.format("Restriction `{0}'' on template of type {1} violated.", get_res_name(restriction), name == null ? "universal charstring" : name));
	}

	public TitanCharString get_single_value() {
		if (pattern_string == null) {
			throw new TtcnError("Pattern string does not exist in universal charstring template");
		}
		return pattern_string;
	}

}
