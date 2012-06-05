/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  This library is free software; you can redistribute it and/or modify it 
  under the terms of the GNU Lesser General Public License as published by 
  the Free Software Foundation; either version 2.1 of the License, or (at 
  your option) any later version.

  This library is distributed in the hope that it will be useful, but 
  WITHOUT ANY WARRANTY; without even the implied warranty of 
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
  General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License 
  along with this library; if not, write to the Free Software Foundation, 
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

  See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
===========================================================================*/

package net.sf.okapi.lib.tmdb;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;

/**
 * Helper class to convert the string in TM db storage format to different formats.
 */
public class ContentFormat {

	private final GenericContent genCnt = new GenericContent();
	private final DocumentBuilder docBuilder;
	private boolean escapeGT = true;
	private int quoteMode = 1;

	public ContentFormat () {
		// Initialization
		DocumentBuilderFactory Fact = DocumentBuilderFactory.newInstance();
		Fact.setValidating(false);
		try {
			docBuilder = Fact.newDocumentBuilder();
		}
		catch ( ParserConfigurationException e ) {
			throw new RuntimeException("Error creating document builder.", e);
		}
	}
	
	/**
	 * Splits a text fragment into its generic coded text and a string holding the codes.
	 * @param frag the text fragment to process.
	 * @return An array of two strings:
	 * 0=the coded text, 1=the codes as a string
	 */
	public String[] fragmentToTmFields (TextFragment frag) {
		String[] res = new String[2];
		res[0] = genCnt.setContent(frag).toString();
		res[1] = Code.codesToString(frag.getCodes());
		return res;
	}
	
	/**
	 * Creates a new text fragment from a text and code TM fields. 
	 * @param ctext the text field to use
	 * @param codesAsString the code field to use
	 * @return a new text fragment.
	 */
	public TextFragment tmFieldsToFragment (String ctext,
		String codesAsString)
	{
		TextFragment tf = new TextFragment("", Code.stringToCodes(codesAsString));
		genCnt.updateFragment(ctext, tf, false);
		return tf;
	}

	public String fragmentToFullCodesText (TextFragment frag) {
		String codedText = frag.getCodedText();
		List<Code> codes = frag.getCodes();
		StringBuilder tmp = new StringBuilder();
		int index;
		int id;
		Code code;
		
		for ( int i=0; i<codedText.length(); i++ ) {
			//TODO: output attribute 'type' whenever possible
			switch ( codedText.codePointAt(i) ) {
			case TextFragment.MARKER_OPENING:
				index = TextFragment.toIndex(codedText.charAt(++i));
				code = codes.get(index);
				if ( code.hasAnnotation("protected") ) {
					tmp.append("<hi type=\"protected\">");
				}
				if ( code.hasOuterData() ) {
					tmp.append(code.getOuterData());
				}
				else {
					tmp.append(String.format("<bpt i=\"%d\">", code.getId()));
					tmp.append(Util.escapeToXML(codes.get(index).toString(), quoteMode, escapeGT, null));
					tmp.append("</bpt>");
				}
				break;
			case TextFragment.MARKER_CLOSING:
				index = TextFragment.toIndex(codedText.charAt(++i));
				code = codes.get(index);
				if ( code.hasOuterData() ) {
					tmp.append(code.getOuterData());
				}
				else {
					tmp.append(String.format("<ept i=\"%d\">", code.getId()));
					tmp.append(Util.escapeToXML(codes.get(index).toString(), quoteMode, escapeGT, null));
					tmp.append("</ept>");
				}
				if ( code.hasAnnotation("protected") ) {
					tmp.append("</hi>");
				}
				break;
			case TextFragment.MARKER_ISOLATED:
				index = TextFragment.toIndex(codedText.charAt(++i));
				code = codes.get(index);
				id = code.getId();
				// Use <ph> or <it> depending on underlying tagType
				switch ( code.getTagType() ) {
				case PLACEHOLDER:
					if ( code.hasOuterData() ) {
						tmp.append(code.getOuterData());
					}
					else {
						tmp.append(String.format("<ph x=\"%d\">", id));
						tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, null));
						tmp.append("</ph>");
					}
					break;
				case OPENING:
					if ( code.hasOuterData() ) {
						tmp.append(code.getOuterData());
					}
					else {
						tmp.append(String.format("<it x=\"%d\" pos=\"begin\">", id));
						tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, null));
						tmp.append("</it>");
					}
					break;
				case CLOSING:
					if ( code.hasOuterData() ) {
						tmp.append(code.getOuterData());
					}
					else {
						tmp.append(String.format("<it x=\"%d\" pos=\"end\">", id));
						tmp.append(Util.escapeToXML(code.toString(), quoteMode, escapeGT, null));
						tmp.append("</it>");
					}
					break;
				}
				
				break;
			case '>':
				if ( escapeGT ) tmp.append("&gt;");
				else {
					if (( i > 0 ) && ( codedText.charAt(i-1) == ']' )) 
						tmp.append("&gt;");
					else
						tmp.append('>');
				}
				break;
			case '<':
				tmp.append("&lt;");
				break;
			case '\r': // Not a line-break in the XML context, but a literal
				tmp.append("&#13;");
				break;
			case '&':
				tmp.append("&amp;");
				break;
			case '"':
				if ( quoteMode > 0 ) tmp.append("&quot;");
				else tmp.append('"');
				break;
			case '\'':
				switch ( quoteMode ) {
				case 1:
					tmp.append("&apos;");
					break;
				case 2:
					tmp.append("&#39;");
					break;
				default:
					tmp.append(codedText.charAt(i));
					break;
				}
				break;
			default:
				tmp.append(codedText.charAt(i));
				break;
			}
		}
		return tmp.toString();
	}
	
	public TextFragment fullCodesTextToFragment (String text)
		throws SAXException, IOException
	{
		// Else: parse the text with XML reader to catch syntax errors
		Document doc = docBuilder.parse(new InputSource(new StringReader("<seg>"+text+"</seg>")));
		// Then convert the content to fragment
		TextFragment tf = new TextFragment();
		int[] idInfo = new int[2];
		idInfo[1] = 0;
		Code code;
		NodeList list = doc.getDocumentElement().getChildNodes();
		for ( int i=0; i<list.getLength(); i++ ) {
			Node node = list.item(i);
			switch ( node.getNodeType() ) {
			case Node.TEXT_NODE:
				tf.append(node.getNodeValue());
				break;
			case Node.ELEMENT_NODE:
				NamedNodeMap map = node.getAttributes();
				Node attr = map.getNamedItem("type");
				Element elem = (Element)node;
				idInfo = getId(idInfo, map.getNamedItem("i"), map.getNamedItem("x"));
				
				if ( node.getNodeName().equals("bpt") ) {
					code = tf.append(TagType.OPENING, (attr==null ? "Xpt" : attr.getNodeValue()),
						elem.getTextContent(), idInfo[0]);
					code.setOuterData(writeElement("bpt", map, elem.getTextContent()));
				}
				else if ( node.getNodeName().equals("ept") ) {
					code = tf.append(TagType.CLOSING, (attr==null ? "Xpt" : attr.getNodeValue()),
						elem.getTextContent(), idInfo[0]);
					code.setOuterData(writeElement("ept", map, elem.getTextContent()));
				}
				else if ( node.getNodeName().equals("ph") ) {
					code = tf.append(TagType.PLACEHOLDER, (attr==null ? "ph" : attr.getNodeValue()),
						elem.getTextContent(), idInfo[0]);
					code.setOuterData(writeElement("ph", map, elem.getTextContent()));
				}
				else if ( node.getNodeName().equals("it") ) {
					Node pos = map.getNamedItem("pos");
					if ( pos == null ) { // Error, but just treat it as a placeholder
						code = tf.append(TagType.PLACEHOLDER, (attr==null ? "ph" : attr.getNodeValue()),
							elem.getTextContent(), idInfo[0]);
						code.setOuterData(writeElement("ph", map, elem.getTextContent()));
					}
					else if ( pos.getNodeValue().equals("begin") ) {
						code = tf.append(TagType.OPENING, (attr==null ? "it" : attr.getNodeValue()),
							elem.getTextContent(), idInfo[0]);
						code.setOuterData(writeElement("it", map, elem.getTextContent()));
					}
					else { // Assumes 'end'
						code = tf.append(TagType.CLOSING, (attr==null ? "it" : attr.getNodeValue()),
							elem.getTextContent(), idInfo[0]);
						code.setOuterData(writeElement("it", map, elem.getTextContent()));
					}
				}
				break;
			}
		}
		
		return tf;
	}

	private int[] getId (int[] idInfo,
		Node attrI,
		Node attrX)
	{
		if ( attrI != null ) idInfo[0] = Integer.valueOf(attrI.getNodeValue());
		else if ( attrX != null ) idInfo[0] = Integer.valueOf(attrX.getNodeValue());
		else {
			idInfo[0] = ++idInfo[1];
			idInfo[1] = idInfo[0];
		}
		return idInfo;
	}
		
	private String writeElement (String name,
		NamedNodeMap map,
		String content)
	{
		// Start tag
		StringBuilder tmp = new StringBuilder("<"+name);
		for ( int i=0; i<map.getLength(); i++ ) {
			Node attr = map.item(i);
			tmp.append(String.format(" %s=\"%s\"",
				attr.getNodeName(),
				Util.escapeToXML(attr.getNodeValue(), 3, false, null)));
		}
		tmp.append(">");
		// Content
		tmp.append(Util.escapeToXML(content, 0, false, null));
		// End tag
		tmp.append("</"+name+">");
		// Done
		return tmp.toString();
	}

}
