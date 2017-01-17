/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package io.debezium.wildfly;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.io.*;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.Test;


public class SubsystemTestCase extends AbstractSubsystemBaseTest {

    public SubsystemTestCase() {
        super(DebeziumExtension.DEBEZIUM_SUBSYSTEM, new DebeziumExtension());
    }

    @Override
    protected String getSubsystemXml() throws IOException {
        return readFile("src/test/resources/sample-config.xml");
    }

    @Override
    protected void compareXml(String configId, String original, String marshalled) throws Exception {
        super.compareXml(configId, original, marshalled, true);
    }

    @Override
    protected String getSubsystemXsdPath() throws Exception {
        return "schema/debezium_1.0.xsd";
    }

    @Test
    public void testDescribeHandler() throws Exception {
    	standardSubsystemTest(null, true);
    }
    
    @Override
	protected String readResource(final String name) throws IOException {
    	String minimum = "<subsystem xmlns=\"urn:jboss:domain:debezium:1.0\"> \n" +     			 
    			"</subsystem>";
        
    	if (name.equals("minimum")) {
        	return minimum;
        }
    	return null;
    }    
    
    @Test
    public void testMinimumConfiguration() throws Exception {
    	standardSubsystemTest("minimum");
    }
    
    @Test
    public void testParseSubsystem() throws Exception {
        //Parse the subsystem xml into operations
    	String subsystemXml = readFile("src/test/resources/sample-config.xml");
        List<ModelNode> operations = super.parse(subsystemXml);

        ///Check that we have the expected number of operations
        Assert.assertEquals(5, operations.size());

        //Check that each operation has the correct content
        ModelNode addSubsystem = operations.get(0);
        Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
        PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
        Assert.assertEquals(1, addr.size());
        PathElement element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(DebeziumExtension.DEBEZIUM_SUBSYSTEM, element.getValue());
    }    
    
	private String readFile(String name) throws IOException {
		InputStream is = new FileInputStream(name);
		BufferedReader buf = new BufferedReader(new InputStreamReader(is));
		String line = buf.readLine();
		StringBuilder sb = new StringBuilder();
		while (line != null) {
			sb.append(line).append("\n");
			line = buf.readLine();
		}
		buf.close();
		return sb.toString();
	}
}