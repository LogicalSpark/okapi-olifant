/*===========================================================================
  Copyright (C) 2011-2012 by the Okapi Framework contributors
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

package net.sf.okapi.applications.olifant;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.lib.tmdb.ContentFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

class SegmentEditor {

	private static final Pattern SHORTCODES = Pattern.compile("<[/be]?(\\d+?)/?>");
	private static final Pattern REALCODES = Pattern.compile("(<(bpt|ept|ph|it|ut).*?>(.*?)</\\2>)|(<(bpt|ept|ph|it|ut)[^>]*?/>)");
	
	private static final int MAX_UNDO = 50;

	private final ISegmentEditorUser caller;
	private final TextStyle codeStyle;
	private final ContentFormat cntFmt;
	private final GenericContent genCnt;
	private final boolean alwaysReadOnly;
	private final boolean alwaysShortCodes;
	
	private Stack<TextChange> undoStack;
	private Stack<TextChange> redoStack;
	private boolean ignoreUndo;
	private boolean storeRedo;
	
	private SegmentEditor other;
	private int column = -1;
	private StyledText edit;
	private boolean modified;
	private boolean fullCodesMode;
	private Pattern codesPattern;
	private String oriCodesasString;
	private String oriText = "";
	private String lastOkText = oriText;
	private TextFragment frag;
	private TextOptions textOptions;
	private Color bgColor;

	class TextChange {
		  // The starting offset of the change
		  int start;
		  // The length of the change
		  int length;
		  // The replaced text
		  String replacedText;

		  TextChange (int start,
			int length,
			String replacedText)
		{
		    this.start = start;
		    this.length = length;
		    this.replacedText = replacedText;
		}
	}
	
	public SegmentEditor (Composite parent,
		int flags,
		ISegmentEditorUser p_caller,
		GridData gridData,
		boolean alwaysReadOnly,
		boolean alwaysShortCodes)
	{
		this.alwaysReadOnly = alwaysReadOnly;
		this.alwaysShortCodes = alwaysShortCodes;
		
		if ( flags < 0 ) { // Use the default styles if requested
			flags = SWT.WRAP | SWT.V_SCROLL | SWT.BORDER;
		}
		caller = p_caller;
		
		bgColor = new Color(parent.getDisplay(), 255, 204, 153); //204, 255, 204);
		codeStyle = new TextStyle();
		codeStyle.background = bgColor;
		//codeStyle.borderStyle = SWT.BORDER_DASH;
		
		cntFmt = new ContentFormat();
		genCnt = new GenericContent();

		edit = new StyledText(parent, flags);
		if ( gridData == null ) {
			gridData = new GridData(GridData.FILL_BOTH);
		}
		edit.setLayoutData(gridData);
		if ( alwaysReadOnly ) setEditable(false);
		
		edit.setMargins(2, 2, 2, 2);

		// Set some keys
		edit.setKeyBinding(SWT.CTRL|'a', ST.SELECT_ALL);
//		// Disable Cut/Copy/Paste commands to override them
//		edit.setKeyBinding(SWT.CTRL|'c', SWT.NULL);
//		edit.setKeyBinding(SWT.CTRL|'v', SWT.NULL);
//		edit.setKeyBinding(SWT.SHIFT|SWT.DEL, SWT.NULL);
//		edit.setKeyBinding(SWT.SHIFT|SWT.INSERT, SWT.NULL);
		
		// Create a copy of the default text field options for the source
		textOptions = new TextOptions(parent.getDisplay(), edit, 3);
		textOptions.applyTo(edit);
		
		undoStack = new Stack<TextChange>();
		redoStack = new Stack<TextChange>();
		
		fullCodesMode = false;
		codesPattern = SHORTCODES;
		
		edit.addExtendedModifyListener(new ExtendedModifyListener() {
			@Override
			public void modifyText (ExtendedModifyEvent event) {
				// Keep track of the changes for the undo function
				if ( ignoreUndo ) {
					if ( storeRedo ) {
						redoStack.push(new TextChange(event.start, event.length, event.replacedText));
					}
				}
				else {
					undoStack.push(new TextChange(event.start, event.length, event.replacedText));
					if ( undoStack.size() > MAX_UNDO ) undoStack.remove(0);
					if ( !redoStack.isEmpty() ) redoStack.clear();
				}
				
		        // Color-code the inline codes
				String text = edit.getText();
				java.util.List<StyleRange> ranges = new java.util.ArrayList<StyleRange>();
				Matcher m = codesPattern.matcher(text);
				while ( m.find() ) {
					StyleRange sr = new StyleRange(codeStyle);
					sr.start = m.start();
					sr.length = m.end()-m.start();
					ranges.add(sr);
				}		    	  
				if ( !ranges.isEmpty() ) {
					edit.replaceStyleRanges(0, text.length(),
						(StyleRange[])ranges.toArray(new StyleRange[0]));
				}
				modified = true;
			}
		});

		edit.addVerifyKeyListener(new VerifyKeyListener() {
			@Override
			public void verifyKey (VerifyEvent e) {
				if ( e.stateMask == SWT.CTRL ) {
					switch ( e.keyCode ) {
					case 'c':
						placeIntoClipboard(scanSelection(edit.getSelection()));
						e.doit = false;
						return;
					case 'x':
						placeIntoClipboard(scanSelection(edit.getSelection()));
						edit.insert("");
						e.doit = false;
						return;
					case 'v':
						pasteFromClipboard();
						e.doit = false;
						return;
					case 'z':
						undo();
						e.doit = false;
						return;
					case 'y':
						redo();
						e.doit = false;
						return;
					}
				}
				else if ( e.stateMask == SWT.SHIFT ) {
					switch ( e.keyCode ) {
					case SWT.DEL:
						placeIntoClipboard(scanSelection(edit.getSelection()));
						edit.insert("");
						e.doit = false;
						return;
					case SWT.INSERT:
						pasteFromClipboard();
						e.doit = false;
						return;
					}
				}
				else if ( e.stateMask == SWT.ALT ) {
					switch ( e.keyCode ) {
					case SWT.ARROW_RIGHT:
						selectNextCode(edit.getCaretOffset(), true);
						return;
					case SWT.ARROW_LEFT:
						selectPreviousCode(edit.getCaretOffset(), true);
						return;
					}
				}
				// Other commands
				if ( e.keyCode == 13 ) {
					e.doit = false;
					if ( !validateContent() ) {
						return; // No call to the user: Fix the error first
					}
					if ( caller.returnFromEdit(true) ) {
						setModified(false);
					}
				}
				else if ( e.keyCode == SWT.ESC ) {
					caller.returnFromEdit(false);
					e.doit = false;
				}
			}
		});
		
		edit.addFocusListener(new FocusListener() {
			@Override
			public void focusLost (FocusEvent e) {
			}
			@Override
			public void focusGained (FocusEvent e) {
				caller.notifyOfFocus(column);
			}
		});
		
		edit.setMenu(createContextMenu());
	}
	
	/**
	 * Sets the other segment editor.
	 * @param other the other segment editor.
	 */
	public void setOther (SegmentEditor other) {
		this.other = other;
	}
	
	private Menu createContextMenu () {
		final Menu contextMenu = new Menu(edit.getShell(), SWT.POP_UP);

		MenuItem item = new MenuItem(contextMenu, SWT.CHECK);
		item.setText("Full Code Display Mode");
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				setFullCodesMode(!getFullCodesMode());
				if ( other != null ) other.setFullCodesMode(getFullCodesMode());
            }
		});
		
		new MenuItem(contextMenu, SWT.SEPARATOR);
		
		item = new MenuItem(contextMenu, SWT.PUSH);
		item.setText("Remove All Codes");
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				clearCodes();
            }
		});

		new MenuItem(contextMenu, SWT.SEPARATOR);
			
		item = new MenuItem(contextMenu, SWT.CHECK);
		item.setText("Read-Only Mode");
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				setEditable(!getEditable());
			}
		});
		
		contextMenu.addListener (SWT.Show, new Listener () {
			public void handleEvent (Event event) {
				contextMenu.getItems()[0].setEnabled(!alwaysShortCodes);
				contextMenu.getItems()[0].setSelection(getFullCodesMode());

				contextMenu.getItems()[4].setEnabled(!alwaysReadOnly);
				contextMenu.getItems()[4].setSelection(!getEditable());
			}
		});
		
		return contextMenu;
	}
	
	
	@Override
	protected void finalize () {
		dispose();
	}

	public void dispose () {
		if ( textOptions != null ) {
			textOptions.dispose();
			textOptions = null;
		}
		if ( bgColor != null ) {
			bgColor.dispose();
			bgColor = null;
		}
		UIUtil.disposeTextStyle(codeStyle);
	}
	
	public void setFullCodesMode (boolean fullCodesMode) {
		if ( alwaysShortCodes ) return; // Ignore this if we are in Short Codes Only mode
		// Save and validate text in current mode
		if ( !validateContent() ) return; // Do not change if there is an error

		// Set the new mode and the associated pattern
		this.fullCodesMode = fullCodesMode;
		if ( fullCodesMode ) {
			codesPattern = REALCODES;
		}
		else {
			codesPattern = SHORTCODES;
		}
		displayText();
	}

	private boolean validateContent () {
		if ( !modified ) return true;
		
		try {
			// Save and validate text in current mode
			if ( fullCodesMode ) {
				return saveTextWithFullCodes();
			}
			return saveTextWithShortCodes();
		}
		catch ( Throwable e ) {
			Dialogs.showError(edit.getShell(), e.getMessage(), null);
			return false;
		}
	}
	
	private void ensureFragmentExists () {
		try {
			if ( frag == null ) {
				frag = cntFmt.tmFieldsToFragment(oriText, oriCodesasString);
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(edit.getShell(), e.getMessage(), null);
		}
	}
	
	private void displayText () {
		try {
			// A display action should not count as a modification
			// Remember the modify state and set the ignore flag
			boolean prevModified = modified;
			ignoreUndo = true;
			// Do the display
			if ( fullCodesMode ) {
				ensureFragmentExists();
				edit.setText(cntFmt.fragmentToFullCodesText(frag));
			}
			else {
				edit.setText(lastOkText);
			}
			// Restore the modify state and ignore flag
			modified = prevModified;
			ignoreUndo = false;
		}
		catch ( Throwable e ) {
			Dialogs.showError(edit.getShell(), e.getMessage(), null);
		}
	}
	
	private boolean saveTextWithFullCodes () {
		try {
			// Get the edited text
			String text = edit.getText();
			// Check if it's an empty text
			if ( text.isEmpty() ) {
				frag = new TextFragment();
				lastOkText = text;
				return true;
			}

			// Else: parse the text with XML reader to catch syntax errors
			TextFragment tf = cntFmt.fullCodesTextToFragment(text);
			// Convert the fragment to numeric coded text
			frag = tf;
			String[] res = cntFmt.fragmentToTmFields(frag);
			lastOkText = res[0];
			return true;
		}
		catch ( Throwable e ) {
			Dialogs.showError(edit.getShell(), e.getMessage(), null);
			return false;
		}
	}
	
	private boolean saveTextWithShortCodes () {
		// Get the edited text
		String text = edit.getText();
		// Check if it's an empty text
		if ( text.isEmpty() ) {
			frag = new TextFragment();
			lastOkText = text;
			return true;
		}
		// Else: try the validate
		try {
			ensureFragmentExists();
			genCnt.updateFragment(text, frag, true);
			lastOkText = text;
		}
		catch ( Throwable e ) {
			Dialogs.showError(edit.getShell(), e.getMessage(), null);
			return false;
		}
		return true;
	}

	public boolean getFullCodesMode () {
		return fullCodesMode;
	}
	
	public boolean setFocus () {
		return edit.setFocus();
	}
	
	/**
	 * Sets the selected text for this segment.
	 * @param start the start position (0 for the beginning of the text)
	 * @param end the end position (use -1 for the end)
	 */
	public void setSelection (int start,
		int end)
	{
		if ( end < 0 ) end = edit.getText().length();
		edit.setSelection(start, end);
	}
	
	public void setEnabled (boolean enabled) {
		edit.setEnabled(enabled);
	}
	
	public void setEditable (boolean editable) {
		if ( alwaysReadOnly ) return; // Ignore this if we are in Always Read-Only mode
		edit.setEditable(editable);
	}
	
	public boolean getEditable () {
		return edit.getEditable();
	}

	public void clear () {
		edit.setText("");
		oriCodesasString = null;
		oriText = "";
		frag = null;
		modified = false;
		undoStack.clear();
		redoStack.clear();
	}
	
	public boolean isModified () {
		return modified;
	}
	
	public void setModified (boolean modified) {
		this.modified = modified;
	}

	/**
	 * Sets the text of the field, and its originating column.
	 * @param text the text
	 * @param codesAsString the codes as a string (can be null if there are no codes)
	 * @param column the column value to use when calling the ISegmentEditorUser.notifyOfFocus().
	 * Use -1 for no column, -2 for not changing the column currently set.
	 */
	public void setText (String text,
		String codesAsString,
		int column)
	{
		if ( column != -2 ) this.column = column;
		edit.setEnabled(text != null);
		oriCodesasString = codesAsString;
		if ( text == null ) oriText = "";
		else oriText = text;
		lastOkText = oriText;
		modified = false;
		frag = null;
		undoStack.clear();
		redoStack.clear();
		displayText();
	}

	public String[] getText () {
		validateContent();
		String[] res = new String[2];
		res[0] = lastOkText;
		// get the codes from either the original or the fragment
		if ( frag == null ) res[1] = oriCodesasString;
		else res[1] = Code.codesToString(frag.getCodes());
		return res;
	}
	
	public String getShortCodedText () {
		validateContent();
		return lastOkText;
	}
	
	private String scanSelection (Point selection) {
		if ( selection.x >= selection.y ) return ""; // Nothing selected
		return edit.getText(selection.x, selection.y-1);
	}
	
	private void placeIntoClipboard (String textData) {
		Clipboard clipboard = new Clipboard(edit.getDisplay());
		try {
			if ( Util.isEmpty(textData) ) return;
			TextTransfer textTrans = TextTransfer.getInstance();
			clipboard.setContents(new Object[]{textData}, new Transfer[]{textTrans});
		}
		finally {
			if ( clipboard != null ) {
				clipboard.dispose();
			}
		}
	}

	private void pasteFromClipboard () {
		Clipboard clipboard = new Clipboard(edit.getDisplay());
		try {
			TransferData[] transferDatas = clipboard.getAvailableTypes();
			for ( TransferData transData : transferDatas ) {
				if ( TextTransfer.getInstance().isSupportedType(transData) ) {
					String text = (String)clipboard.getContents(TextTransfer.getInstance());
					int pos = edit.getCaretOffset();
					edit.insert(text);
					edit.setCaretOffset(pos+text.length());
					break;
				}
			}
		}
		finally {
			if ( clipboard != null ) {
				clipboard.dispose();
			}
		}
	}

	private void clearCodes () {
		int[] ranges = edit.getRanges();
		if ( ranges.length == 0 ) return;
		StringBuilder tmp = new StringBuilder(edit.getText());
		int offset = 0;
		for ( int i=0; i<ranges.length; i+=2 ) {
			int start = ranges[i]-offset;
			tmp.delete(start, start+ranges[i+1]);
			offset += ranges[i+1];
		}
		frag = new TextFragment(tmp.toString());
		lastOkText = tmp.toString();
		edit.setText(lastOkText);
	}

	private void selectNextCode (int position,
		boolean cycle)
	{
		int[] ranges = edit.getRanges();
		if ( ranges.length == 0 ) return;
		while ( true ) {
			for ( int i=0; i<ranges.length; i+=2 ) {
				int value = ranges[i];
				if ( position <= value ) {
					edit.setSelection(value, value+ranges[i+1]);
					return;
				}
			}
			// Not found yet: Stop here if we don't cycle to the first
			if ( !cycle ) return;
			position = 0; // Otherwise: re-start from front
		}
	}
		
	private void selectPreviousCode (int position,
		boolean cycle)
	{
		int[] ranges = edit.getRanges();
		if ( ranges.length == 0 ) return;
		while ( true ) {
			for ( int i=ranges.length-2; i>=0; i-=2 ) {
				int value = ranges[i];
				int len = ranges[i+1];
				if ( value < position ) {
					Point pt = edit.getSelection();
					if (( pt.x == value ) && ( pt.x != pt.y )) {
						if ( ranges.length < 3 ) return;
						else continue;
					}
					edit.setSelection(value, value+len);
					return;
				}
			}
			// Not found yet: Stop here if we don't cycle to the first
			if ( !cycle ) return;
			position = edit.getCharCount()-1; // Otherwise: re-start from the end
		}			
	}

	private void undo () {
		// Make sure the stack isn't empty
		if ( undoStack.isEmpty() ) return;
		// Get the last change
		TextChange change = undoStack.pop();
		// Set the ignore flag. Otherwise, the replaceTextRange call will get placed on the undo stack
		// Set also the storeRedo flag to be able to re-do the undo changes.
		ignoreUndo = storeRedo = true;
		// Replace the changed text
		edit.replaceTextRange(change.start, change.length, change.replacedText);
		// Move the caret
		edit.setCaretOffset(change.start);
		// Scroll the screen
		edit.setTopIndex(edit.getLineAtOffset(change.start));
		// Reset the flags
		ignoreUndo = storeRedo = false;
	}

	private void redo () {
		// Make sure the stack isn't empty
		if ( redoStack.isEmpty() ) return;
		// Get the last change
		TextChange change = redoStack.pop();
		// Set the ignore flag. Otherwise, the replaceTextRange call will get placed on the undo stack
		ignoreUndo = true;
		// Replace the changed text
		edit.replaceTextRange(change.start, change.length, change.replacedText);
		// Move the caret
		int pos = change.start+change.replacedText.length();
		edit.setCaretOffset(pos);
		// Scroll the screen
		edit.setTopIndex(edit.getLineAtOffset(pos));
		// Reset the ignore flag
		ignoreUndo = false;
	}
	
//	private void moveCaretToEnd () {
//		edit.setCaretOffset(edit.getText().length());
//	}

//	private void placeText (String text) {
//		Point pt = edit.getSelection();
//		edit.replaceTextRange(pt.x, pt.y-pt.x, text);
//		edit.setCaretOffset(pt.x+text.length());
//	}
//	

}
