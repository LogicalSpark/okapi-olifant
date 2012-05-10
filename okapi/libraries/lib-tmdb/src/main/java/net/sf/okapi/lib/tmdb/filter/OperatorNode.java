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

package net.sf.okapi.lib.tmdb.filter;

public class OperatorNode extends FilterNode {

	private Operator operator;
	private FilterNode left;
	private FilterNode right;
	
	public OperatorNode (Operator operator,
		FilterNode expression)
	{
		super();
		this.operator = operator;
		this.right = expression;
	}
	
	public OperatorNode (Operator operator,
		FilterNode leftExpression,
		FilterNode rightExpression)
	{
		super();
		this.operator = operator;
		this.left = leftExpression;
		this.right = rightExpression;
	}
	
	public OperatorNode (Operator operator,
		String leftFieldName,
		boolean rightBoolean)
	{
		super();
		this.operator = operator;
		this.left = new ValueNode(true, leftFieldName);
		this.right = new ValueNode(rightBoolean);
	}
	
	public boolean isBinary () {
		return ((right != null) && ( left != null));
	}
	
	public Operator getOperator () {
		return operator;
	}
	
	public FilterNode getLeft () {
		return left;
	}
	
	public FilterNode getRight () {
		return right;
	}
	
	public FilterNode getExpression () {
		// Same as getRight()
		return right;
	}
	
}
