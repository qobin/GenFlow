/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd.test.links;

import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasAtomNsDeclaration;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasMaxOccurs;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasMinOccurs;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.isUnbounded;
import static com.reprezen.genflow.rapidml.xsd.test.XsdFunctions.getAllBlockItem;
import static com.reprezen.genflow.rapidml.xsd.test.XsdFunctions.getAtomLinkAttribute;
import static com.reprezen.genflow.rapidml.xsd.test.XsdFunctions.getChildAttributeWithName;
import static com.reprezen.genflow.rapidml.xsd.test.XsdFunctions.getListItem;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;

import com.reprezen.genflow.rapidml.xsd.test.XsdGeneratorIntegrationTestFixture;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("links/DefaultDescriptor_MultipleDescriptorsWODefault.rapid")
public class DefaultLinkDescriptor_WODefault {
    @Rule
    public XsdGeneratorIntegrationTestFixture fixture = new XsdGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedXsdIsValidSchema() {
        fixture.assertGeneratedXsdIsValidSchema();
    }

    @Test
    public void testAtomNsDeclaration() throws Exception {
        Node schema = fixture.requireSchema();
        assertThat(schema, hasAtomNsDeclaration());
    }

    @Test
    public void testGlobalTypeForDataType1Resource() throws Exception {
        Node complexType = fixture.requireGlobalComplexType("DataType1Object");
        assertNotNull(getChildAttributeWithName("dataType1ID").apply(complexType));
    }

    @Test
    public void testGlobalTypeForDataType1() throws Exception {
        Node complexType = fixture.getGlobalComplexType("DataType1");
        assertNull(complexType);
    }

    @Test
    public void testGlobalTypeForDataType2() throws Exception {
        Node complexType = fixture.getGlobalComplexType("DataType2");
        assertNull(complexType);
    }

    @Test
    public void testDataType1_NonContainmentReference1() throws Exception {
        Node sequence = fixture.requireAllBlockElement("DataType1Object", "dataType1_reference1List");
        assertThat(sequence, hasMinOccurs(0));
        assertThat(sequence, hasMaxOccurs(1));

        Node listItem = getListItem().apply(sequence);
        assertNotNull(listItem);
        assertThat(listItem, hasMinOccurs(1));
        assertThat(listItem, isUnbounded());

        // testing ReferenceLink to
        // refProp dataType1_reference1 lower 0 upper -1 : DataType2
        Node atomLink = getAllBlockItem().apply(listItem);

        assertNotNull(atomLink);
        fixture.assertIsCorrectAtomLink(atomLink, "dataType1_reference1");
        // linkDescriptor DataType2ResourceLink2
        // DataType2.dataType2ID, DataType2.aProperty

        Node attr1 = getAtomLinkAttribute("dataType2ID").apply(listItem);
        assertNull(attr1);

        Node attr2 = getAtomLinkAttribute("aProperty").apply(listItem);
        assertNull(attr2);

    }

}
