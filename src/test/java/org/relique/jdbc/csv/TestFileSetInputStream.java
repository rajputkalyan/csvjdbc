/*
 *  CsvJdbc - a JDBC driver for CSV files
 *  Copyright (C) 2008  Mario Frasca
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.relique.jdbc.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.relique.io.FileSetInputStream;

public class TestFileSetInputStream
{
	private static String filePath;

	@BeforeClass
	public static void setUp()
	{
		filePath = ".." + File.separator + "src" + File.separator + "testdata";
		if (!new File(filePath).isDirectory())
			filePath = "src" + File.separator + "testdata";
		assertTrue("Sample files directory not found: " + filePath, new File(filePath).isDirectory());
		filePath = filePath + File.separator;

		// load CSV driver
		try
		{
			Class.forName("org.relique.jdbc.csv.CsvDriver");
		}
		catch (ClassNotFoundException e)
		{
			fail("Driver is not in the CLASSPATH -> " + e);
		}
	}

	@Test
	public void testGlueAsTrailing() throws IOException
	{
		try (BufferedReader inputRef = new BufferedReader(new InputStreamReader(
				new FileInputStream(filePath + "test-glued-trailing.txt")));

			BufferedReader inputTest = new BufferedReader(new InputStreamReader(
				new FileSetInputStream(filePath,
						"test-([0-9]{3})-([0-9]{8}).txt", new String[] {
								"location", "file_date" }, ",", false, false, null, 0, null))))
		{
			Set<String> refSet = new HashSet<String>();
			Set<String> testSet = new HashSet<String>();
			inputRef.readLine();
			inputTest.readLine();
			String lineRef, lineTest;
			do
			{
				lineRef = inputRef.readLine();
				lineTest = inputTest.readLine();
				refSet.add(lineRef);
				testSet.add(lineTest);
			}
			while (lineRef != null && lineTest != null);
			assertTrue("refSet contains testSet", refSet.containsAll(testSet));
			assertTrue("testSet contains refSet", testSet.containsAll(refSet));
		}
	}

	@Test
	public void testGlueAsLeading() throws IOException
	{
		try (BufferedReader inputRef = new BufferedReader(new InputStreamReader(
				new FileInputStream(filePath + "test-glued-leading.txt")));

			BufferedReader inputTest = new BufferedReader(new InputStreamReader(
				new FileSetInputStream(filePath,
						"test-([0-9]{3})-([0-9]{8}).txt", new String[] {
								"location", "file_date" }, ",", true, false, null, 0, null))))
		{
			Set<String> refSet = new HashSet<String>();
			Set<String> testSet = new HashSet<String>();
			inputRef.readLine();
			inputTest.readLine();
			String lineRef, lineTest;
			do
			{
				lineRef = inputRef.readLine();
				lineTest = inputTest.readLine();
				refSet.add(lineRef);
				testSet.add(lineTest);
			}
			while (lineRef != null && lineTest != null);
			assertTrue("refSet contains testSet", refSet.containsAll(testSet));
			assertTrue("testSet contains refSet", testSet.containsAll(refSet));
		}
	}

	@Test
	public void testGlueAsLeadingHeaderless() throws IOException
	{
		try (BufferedReader inputRef = new BufferedReader(new InputStreamReader(
				new FileInputStream(filePath + "headerless-glued-leading.txt")));

			BufferedReader inputTest = new BufferedReader(new InputStreamReader(
				new FileSetInputStream(filePath,
						"headerless-([0-9]{3})-([0-9]{8}).txt", new String[] {
								"location", "file_date" }, ",", true, true, null, 0, null))))
		{
			Set<String> refSet = new HashSet<String>();
			Set<String> testSet = new HashSet<String>();
			String lineRef, lineTest;
			do
			{
				lineRef = inputRef.readLine();
				lineTest = inputTest.readLine();
				refSet.add(lineRef);
				testSet.add(lineTest);
			}
			while (lineRef != null && lineTest != null);
			assertTrue("refSet contains testSet", refSet.containsAll(testSet));
			assertTrue("testSet contains refSet", testSet.containsAll(refSet));
		}
	}

	@Test
	public void testGlueAsEmpty() throws IOException
	{
		try (BufferedReader inputRef = new BufferedReader(new InputStreamReader(
				new FileInputStream(filePath + "empty-glued.txt")));

			BufferedReader inputTest = new BufferedReader(new InputStreamReader(
				new FileSetInputStream(filePath,
						"empty-([0-9]+).txt", new String[] {
							"EMPTY_ID"}, ",", false, false, null, 0, null))))
		{
			Set<String> refSet = new HashSet<String>();
			Set<String> testSet = new HashSet<String>();
			String lineRef, lineTest;
			do
			{
				lineRef = inputRef.readLine();
				lineTest = inputTest.readLine();
				refSet.add(lineRef);
				testSet.add(lineTest);
			}
			while (lineRef != null && lineTest != null);
			assertTrue("refSet contains testSet", refSet.containsAll(testSet));
			assertTrue("testSet contains refSet", testSet.containsAll(refSet));
		}
	}

	@Test
	public void testGlueAsLeadingLastLineNoNewline() throws IOException
	{
		String separator = ",";

		// Test CSV files with no '\n' on last line
		try (BufferedReader inputTest = new BufferedReader(new InputStreamReader(
				new FileSetInputStream(filePath,
						"petr-([0-9]{3})-([0-9]{3}).csv", new String[] {
								"part1", "part2" }, separator, true, true, null, 0, null))))
		{
			String lineTest = inputTest.readLine();
			while (lineTest != null)
			{
				// Files have no special characters, so we can simply split on separator
				String []columns = lineTest.split(separator);
				// CSV file contains two columns, plus two extra columns from filename
				int expectedColumns = 4;
				assertEquals("Expected number of columns", expectedColumns, columns.length);
				lineTest = inputTest.readLine();
			}
		}
	}

	@Test
	public void testFileSetInputStreamClose() throws IOException
	{
		try (FileSetInputStream in = new FileSetInputStream(filePath,
					"test-([0-9]{3})-([0-9]{8}).txt", new String[] {
					"location", "file_date"}, ",", false, false, null, 0, null))
		{
			in.read();
			in.read();
			in.close();
			try
			{
				in.read();
				fail("expected exception java.io.IOException");
			}
			catch (IOException e)
			{
				assertTrue(("" + e).contains("IOException"));
			}
		}
	}

	@Test
	public void testFileSetCharsetUtf16le() throws IOException
	{
		String charset = "UTF-16LE";
		try (BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileSetInputStream(filePath,
						"utf16le_(\\d+).txt", new String[] {
						"date"}, "|", false, false, null, 0, charset), charset)))
		{
			String line = in.readLine();
			assertEquals("Col1|Col2|Some third|4th|date", line);
			line = in.readLine();
			assertEquals("1|01.10.2018|-4,4|1111|01102018", line);
			line = in.readLine();
			assertEquals("2|04.11.2020|-2,2|2222|04112020", line);
		}
	}
}
