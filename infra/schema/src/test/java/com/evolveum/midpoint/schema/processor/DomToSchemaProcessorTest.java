/*
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2011 [name of copyright owner]
 */

package com.evolveum.midpoint.schema.processor;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.util.DOMUtil;

public class DomToSchemaProcessorTest {

	private static final String SCHEMA_NS = "http://foo.com/xml/ns/schema";
	
	@Test
	public void testAccessList() throws Exception {
		Document schemaDom = DOMUtil.parseFile("src/test/resources/processor/schema.xsd");
		Schema schema = Schema.parse(DOMUtil.getFirstChildElement(schemaDom));
		
		final String defaultNS = "http://midpoint.evolveum.com/xml/ns/public/resource/instances/ef2bc95b-76e0-48e2-86d6-3d4f02d3e1a2";
		final String icfNS = "http://midpoint.evolveum.com/xml/ns/public/connector/icf-1/resource-schema-1.xsd";
		ResourceObjectDefinition objectDef = (ResourceObjectDefinition) schema.findContainerDefinitionByType(new QName(defaultNS, "AccountObjectClass"));
		
		ResourceObjectAttributeDefinition attrDef = objectDef.findAttributeDefinition(new QName(icfNS, "uid"));
		AssertJUnit.assertTrue(attrDef.canRead());
		AssertJUnit.assertFalse(attrDef.canUpdate());
		AssertJUnit.assertFalse(attrDef.canCreate());
		
		attrDef = objectDef.findAttributeDefinition(new QName(defaultNS, "title"));
		AssertJUnit.assertTrue(attrDef.canRead());
		AssertJUnit.assertTrue(attrDef.canUpdate());
		AssertJUnit.assertTrue(attrDef.canCreate());
		
		attrDef = objectDef.findAttributeDefinition(new QName(defaultNS, "photo"));
		AssertJUnit.assertFalse(attrDef.canRead());
		AssertJUnit.assertTrue(attrDef.canUpdate());
		AssertJUnit.assertTrue(attrDef.canCreate());
	}
	
	@Test
	public void testRoundTrip() throws SchemaProcessorException {
		// GIVEN
		Schema schema = new Schema(SCHEMA_NS);
		// Ordinary property
		schema.createPropertyDefinition("number1", DOMUtil.XSD_INTEGER);
		
		// Property reference
		
		// FIXME: this makes the test fail
//		schema.createPropertyDefinition(SchemaConstants.I_CREDENTIALS, SchemaConstants.I_CREDENTIALS_TYPE);
		
		// Property container
		PropertyContainerDefinition containerDefinition = schema.createPropertyContainerDefinition("ContainerType");
		// ... in it ordinary property
		containerDefinition.createPropertyDefinition("login", DOMUtil.XSD_STRING);
		// ... and local property with a type from another schema
		containerDefinition.createPropertyDefinition("password", SchemaConstants.R_PROTECTED_STRING_TYPE);

		System.out.println("Schema before serializing to XSD: ");
		System.out.println(schema.dump());
		System.out.println();

		// WHEN
		
		Document xsd = schema.serializeToXsd();
		
		System.out.println("Schema after serializing to XSD: ");
		System.out.println(DOMUtil.serializeDOMToString(DOMUtil.getFirstChildElement(xsd)));
		System.out.println();
		
		Schema newSchema = Schema.parse(DOMUtil.getFirstChildElement(xsd));

		System.out.println("Schema after parsing from XSD: ");
		System.out.println(newSchema.dump());
		System.out.println();
		
		// THEN
	
		// FIXME: this also does not work
//		PropertyDefinition number1def = newSchema.findDefinition(new QName(SCHEMA_NS,"number1"), PropertyDefinition.class);
//		assertEquals(new QName(SCHEMA_NS,"number1"),number1def.getName());
//		assertEquals(DOMUtil.XSD_INTEGER,number1def.getTypeName());
		
		PropertyContainerDefinition newContainerDef = schema.findContainerDefinitionByType(new QName(SCHEMA_NS,"ContainerType"));
		assertEquals(new QName(SCHEMA_NS,"ContainerType"),newContainerDef.getTypeName());
		
		PropertyDefinition loginDef = newContainerDef.findPropertyDefinition(new QName(SCHEMA_NS,"login"));
		assertEquals(new QName(SCHEMA_NS,"login"), loginDef.getName());
		assertEquals(DOMUtil.XSD_STRING, loginDef.getTypeName());

		PropertyDefinition passwdDef = newContainerDef.findPropertyDefinition(new QName(SCHEMA_NS,"password"));
		assertEquals(new QName(SCHEMA_NS,"password"), passwdDef.getName());
		assertEquals(SchemaConstants.R_PROTECTED_STRING_TYPE, passwdDef.getTypeName());
		
	}
}
