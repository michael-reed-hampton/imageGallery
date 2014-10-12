package test.name.hampton.mike;

import java.util.HashMap;

import name.hampton.mike.NamespaceVariableResolver;
import junit.framework.TestCase;

public class NamespaceVariableResolverTest extends TestCase {

	public void testResolveValueSimple() {
		
		HashMap<String, Object> keyObjects = new HashMap<String, Object>();
		
		keyObjects.put("mike", "a String");
		
		NamespaceVariableResolver resolver = new NamespaceVariableResolver(keyObjects);
		
		String resolved = (String) resolver.resolveValue("{mike}");
		System.out.println(resolved);
		
		assertTrue("Resolved value is " + resolved, resolved.equals("a String"));
	}


	public void testResolveValueNamespaced() {
		
		HashMap<String, Object> keyObjects = new HashMap<String, Object>();
		
		keyObjects.put("mike", "a String");
		
		NamespaceVariableResolver resolver = new NamespaceVariableResolver(keyObjects);
		
		String resolved = (String) resolver.resolveValue("{mike.class.name}");
		System.out.println(resolved);
		
		assertTrue("Resolved value is " + resolved, resolved.equals("java.lang.String"));
	}

	public void testResolveValueMultipleString() {
		
		HashMap<String, Object> keyObjects = new HashMap<String, Object>();
		
		keyObjects.put("first", "mike");
		keyObjects.put("last", "hampton");
		
		NamespaceVariableResolver resolver = new NamespaceVariableResolver(keyObjects);
		
		String resolved = (String) resolver.resolveValue("Hello {first} {last}");
		System.out.println(resolved);
		
		assertTrue("Resolved value is " + resolved, resolved.equals("Hello mike hampton"));
	}

	public void testResolveValueMultipleNamespace() {
		
		HashMap<String, Object> keyObjects = new HashMap<String, Object>();
		
		keyObjects.put("first", new Object());
		keyObjects.put("last", "hampton");
		
		NamespaceVariableResolver resolver = new NamespaceVariableResolver(keyObjects);
		
		String resolved = (String) resolver.resolveValue("{first.class.name} {last.class.name}");
		System.out.println(resolved);
		
		assertTrue("Resolved value is " + resolved, resolved.equals("java.lang.Object java.lang.String"));
	}
}
