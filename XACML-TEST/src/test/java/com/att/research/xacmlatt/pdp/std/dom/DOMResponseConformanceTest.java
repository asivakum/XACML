/*
 *
 *          Copyright (c) 2013,2019  AT&T Knowledge Ventures
 *                     SPDX-License-Identifier: MIT
 */
package com.att.research.xacmlatt.pdp.std.dom;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.att.research.xacml.api.Response;
import com.att.research.xacml.std.dom.DOMResponse;

/**
 * Tests for handling the XML version of the XACML Response object.
 * 
 * TO RUN - use jUnit
 * In Eclipse select this file or the enclosing directory, right-click and select Run As/JUnit Test
 * 
 * Normally the Response is generated by the PDP and returned through the RESTful interface as JSON.
 * Testing of the XML interface is minimal and not complete.
 * 
 * 
 * 
 * @author glenngriffin
 *
 */
public class DOMResponseConformanceTest {
	
	// where to find the conformance test XML files
	private final String CONFORMANCE_DIRECTORY_PATH = "src/test/resources/testsets/conformance/xacml3.0-ct-v.0.4";
	
	// The request object output from each test conversion from JSON string
	Response response;

	
	
	// Load the Conformance test responses into Response objects, generate the output XML for that Response and compare with the original files.
	@Test
	public void testDOMResponse() {
		List<File> filesInDirectory = null;
		
		File conformanceDirectory = null;
		
		File currentFile = null;
		
		try {
			conformanceDirectory = new File(CONFORMANCE_DIRECTORY_PATH);
			filesInDirectory = getRequestsInDirectory(conformanceDirectory);
		} catch (Exception e) {
			fail("Unable to set up Conformance tests for dir '" + conformanceDirectory.getAbsolutePath()+"' e="+ e);
		}
		
		// run through each XML file
		//	- load the file from XML into an internal Response object
		//	- generate the XML representation from that Response object
		// 	- reload the file into a String
		//	- compare the 2 XML strings
		Response xmlResponse = null;
		try {
			for (File f : filesInDirectory) {
				currentFile = f;

//// This is a simple way to select just one file for debugging - comment out when not being used
//if ( ! f.getName().equals("IID302Response.xml")) {   continue;  }

// during debugging it is helpful to know what file it is starting to work on
//				System.out.println("starting file="+currentFile.getName());
				
				
				BufferedReader br = new BufferedReader(new FileReader(f));
				StringBuffer sb = new StringBuffer();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				br.close();
				
				String xmlFromFile = sb.toString();
				
				try {
					// load XML into a Response object
					xmlResponse = DOMResponse.load(xmlFromFile);
				} catch (Exception e) {
					// if XML does not load, just note it and continue with next file
					System.out.println("XML file did not load: '" + f.getName() + "  e=" + e);
					continue;
				}
//System.out.println(xmlFromFile);				
				
				// create String version from the Response object
				String xmlResponseString = DOMResponse.toString(xmlResponse, false);
				
				// Comparing the string directly to the String from the file is difficult.
				// We can minimize the problems with newlines and whitespace, but we have other issues with how various object values are represented.
				// For instance, and input double of "23.50" is output as "23.5" which is the same value but not identical strings.
				// Therefore we take the XML output and use it to create a new Response object, then compare the two objects.

//System.out.println(xmlResponseString);			
				Response reGeneratedResponse = DOMResponse.load(xmlResponseString);
				
				if ( ! xmlResponse.equals(reGeneratedResponse)) {
					String normalizedFromFile = xmlFromFile.replaceAll("\\r|\\n", "");
					normalizedFromFile = normalizedFromFile.replaceAll("\\s+", " ");
					normalizedFromFile = normalizedFromFile.replaceAll(">\\s*<", "><");
					System.out.println("File="+normalizedFromFile);
					System.out.println("Gend="+ xmlResponseString);
					
					System.out.println(DOMResponse.toString(xmlResponse, true));
				
					fail("Output string did not re-generate eqivilent object.");
				}

//				// Normally whitespace is significant in XML.
//				// However in this case we are generating an XML string for output and comparing it to a hand-made file.
//				// The file may contain extra newlines or fewer spaces then our prettyPrinted output version.
//				// Therefore we do the comparison on the un-prettyPrinted generated string.
//				// To do this we have to remove the extra whitespace from the version read from the file.
//				String normalizedFromFile = xmlFromFile.replaceAll("\\r|\\n", "");
//				normalizedFromFile = normalizedFromFile.replaceAll("\\s+", " ");
//				normalizedFromFile = normalizedFromFile.replaceAll(">\\s*<", "><");
//			
//				if ( ! xmlResponseString.equals(normalizedFromFile)) {
//					System.out.println("file="+normalizedFromFile+"\ngend="+xmlResponseString);
//					fail("file not same as generated string: " + f.getName()+ "\nFile="+xmlFromFile + "\nString="+xmlResponseString);
//				}


			}			

		} catch (Exception e) {
			fail ("Failed test with '" + currentFile.getName() + "', e=" + e);
		}

		
	}
	
	
	
	//
	// HELPER to get list of all Request files in the given directory
	//
	
	private List<File> getRequestsInDirectory(File directory) {
		List<File> fileList = new ArrayList<File>();
		
		File[] fileArray = directory.listFiles();
		for (File f : fileArray) {
			if (f.isDirectory()) {
				List<File> subDirList = getRequestsInDirectory(f);
				fileList.addAll(subDirList);
			}
			if (f.getName().endsWith("Response.xml")) {
				fileList.add(f);
			}
		}
		return fileList;
		
	}
	
	
}


/*
Place to edit long strings output during tests








*/
