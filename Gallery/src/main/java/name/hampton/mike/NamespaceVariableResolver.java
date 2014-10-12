package name.hampton.mike;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamespaceVariableResolver {

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	private HashMap<String, Object> keyObjects;

	public NamespaceVariableResolver(HashMap<String, Object> keyObjects) {
		this.keyObjects = keyObjects;
	}

	
	public Object resolveValue(String key) {
		return this.resolveValue(key, keyObjects);
	}
	/**
	 * This could be more powerful later.  For now it only deals with namespaced individual items - no indexing into arrays or iterating.
	 * 
	 * @param key - a string, if of the form {objectname.propertyname.propertyname} then an attempt is made to 
	 * 	resolve the value using the passed hashmap to resolve the initial 'objectname' followed by bean introspection
	 * 	for each propertyname.
	 * @param keyObjects - the map of named object instances used for the initial object resolution
	 * @return
	 */
	public Object resolveValue(String key, HashMap<String, Object> keyObjects) {
		if(null!= key)
		{
			String pattern = "\\{([^\\{].[^\\}]*)\\}";
			// Create a Pattern object
			Pattern r = Pattern.compile(pattern);
	
			// Now create matcher object.
			Matcher m = r.matcher(key);
			
			// For the following example:
			//
			//	{request.userPrincipal.name} has a session {request.session.id} that was last accessed at {request.session.lastAccessedTime}.
			//
			// This will loop, returning in order:
			//		{request.userPrincipal.name}
			//		{request.session.id}
			//		{request.session.lastAccessedTime}
			//
			//	
			
			// This is here to allow us to relace the values EXACTLY.  We will change the keys to strip out whitespace,
			// so we need this to ensure we do proper replacements of everything between the { and }
			HashMap<String,Object> replacementValues = new HashMap<String, Object>();
			while(m.find()) {
				String unchangedMatchedVariable = m.group(0);
				String matchedVariable = new String(unchangedMatchedVariable);
				// Get rid of the leading and trailing curly braces '{' '}'
				matchedVariable = matchedVariable.replace('{', ' ');
				matchedVariable = matchedVariable.replace('}', ' ');
				matchedVariable = matchedVariable.trim();
				
				// Split the namespaced elements
				// for request.userPrincipal.name this will give the following
				// ["request", "userPrincipal", "name"]
				String[] elements = matchedVariable.split("\\.");
				Object currentObject = null;
				try {
					currentObject = resolveNamespaceVariable(keyObjects, elements);
					if(null!=currentObject)
					{
						replacementValues.put(unchangedMatchedVariable,currentObject);
					}
				} catch (IllegalAccessException e) {
					logger.error("Error gettign named property.  Key=" + key + ", last resolved = "+currentObject, e);
				} catch (InvocationTargetException e) {
					logger.error("Error gettign named property.  Key=" + key + ", last resolved = "+currentObject, e);
				} catch (NoSuchMethodException e) {
					logger.error("Error gettign named property.  Key=" + key + ", last resolved = "+currentObject, e);
				}
			}
			// Done resolving, time to replace in the string.
			// Note: we could make this work with recursive replacements someday, but I have goldplated this enough for now.
			Iterator<String> keys = replacementValues.keySet().iterator();
			while(keys.hasNext())
			{
				String replacementKey = keys.next();
				String replacementValue;
				if (null != replacementValues.get(replacementKey))
				{
					replacementValue = (String)replacementValues.get(replacementKey);
					key = key.replace(replacementKey, replacementValue);
				}
			}
		}
		return key;
	}

	private Object resolveNamespaceVariable(HashMap<String, Object> keyObjects,
			String[] elements) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		Object currentObject;
		String currentKey = elements[0].trim();
		// For ["request", "userPrincipal", "name"]
		// the following will try to find an object with the key "request"
		Object baseObject = keyObjects.get(currentKey);
		currentObject = baseObject;
		if(null!=baseObject)
		{					
			// For ["request", "userPrincipal", "name"]
			// request has been resolved, the following will try to find the "userPrincipal" property of the object resolved to "request",
			// then the the "name" property of the object resolved to "userPrincipal"
			for(int idx=1;idx<elements.length;idx++)
			{
				String strippedKey = elements[idx].trim();
				currentKey = currentKey + "." + strippedKey;
				// See if the key has been resolved previously.
				// for our example, 
				//		{request.userPrincipal.name}
				//			will find "request" above as the baseObject (assuming it is there!)
				//			will resolve "userPrincipal" from the currentObject (which is currently the request object)
				//				using PropertyUtils.getProperty(currentObject,"userPrincipal")
				//			will resolve "name" from the currentObject (which is currently the userPrincipal object)
				//				using PropertyUtils.getProperty(currentObject,"name")
				//		{request.session}
				//			will find "request" above as the baseObject (assuming it is there!)
				//			will resolve "session" from the currentObject (which is currently the request object)
				//				using PropertyUtils.getProperty(currentObject,"session")
				//			will resolve "id" from the currentObject (which is currently the session object)
				//				using PropertyUtils.getProperty(currentObject,"id")
				//		{request.session.lastAccessedTime}
				//			will find "request" above as the baseObject (assuming it is there!)
				//			will find "request.session" in the keyObjects 
				//			will resolve "lastAccessedTime" from the currentObject (which is currently the session object)
				//				using PropertyUtils.getProperty(currentObject,"lastAccessedTime")
				Object resolvedCurrentObject = keyObjects.get(currentKey);
				if(null==resolvedCurrentObject)
				{
					resolvedCurrentObject = PropertyUtils.getProperty(currentObject, strippedKey);
					keyObjects.put(currentKey, resolvedCurrentObject);
				}
				currentObject = resolvedCurrentObject;
			}
		}
		return currentObject;
	}
}
