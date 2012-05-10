package net.sf.okapi.lib.tmdb.filter;

import static org.junit.Assert.*;

import net.sf.okapi.lib.tmdb.filter.ValueNode.TYPE;

import org.junit.Test;

public class FilterNodeTest {
	
	@Test
	public void test1 () {
		FilterNode root = new OperatorNode(Operator.OP_EQUALS, "F1", true);
		assertTrue(root.isOperator());
		OperatorNode opn = ((OperatorNode)root);
		assertEquals(Operator.OP_EQUALS, opn.getOperator());
		assertFalse(opn.getLeft().isOperator());
		ValueNode vn = (ValueNode)opn.getLeft();
		assertEquals("F1", vn.getStringValue());
		assertFalse(opn.getRight().isOperator());
		vn = (ValueNode)opn.getRight();
		assertTrue(vn.getBooleanValue());
		assertEquals("([F1] equals true)", convert(root));
	}
	
	@Test
	public void test2 () {
		FilterNode root = new OperatorNode(Operator.OP_OR,
			new OperatorNode(Operator.OP_EQUALS, "F1", true),
			new OperatorNode(Operator.OP_EQUALS, "F2", false));
		assertEquals("(([F1] equals true) or ([F2] equals false))", convert(root));
	}
	
	@Test
	public void test3 () {
		FilterNode root = new OperatorNode(Operator.OP_NOT,
			new OperatorNode(Operator.OP_EQUALS, "F1", true));
		assertEquals("(not ([F1] equals true))", convert(root));
	}
	
	private String convert (FilterNode node) {
		String tmp = "";
		if ( node.isOperator() ) {
			OperatorNode on = (OperatorNode)node;
			if ( on.isBinary() ) {
				tmp = convert(on.getRight());
				tmp = on.getOperator().getName() + " " + tmp + ")";
				tmp = "(" + convert(on.getLeft()) + " " + tmp;
			}
			else { // Unary operator
				tmp = "(" + on.getOperator().getName() + " ";
				tmp = tmp + convert(on.getRight()) + ")"; 
			}
		}
		else {
			ValueNode vn = (ValueNode)node;
			if ( vn.isField() ) {
				return "["+vn.getStringValue()+"]";
			}
			// Else: constant
			if ( vn.getType() == TYPE.BOOLEAN ) {
				return vn.getBooleanValue() ? "true" : "false";
			}
			if ( vn.getType() == TYPE.STRING ) {
				return vn.getStringValue();
			}
			return "ERROR";
			
		}
		return tmp;
	}
}
