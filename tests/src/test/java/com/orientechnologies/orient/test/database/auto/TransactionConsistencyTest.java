/*
 * Copyright 1999-2010 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.test.database.auto;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.orientechnologies.orient.client.remote.OEngineRemote;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

@Test(groups = "dictionary")
public class TransactionConsistencyTest {
	private String	url;

	@Parameters(value = "url")
	public TransactionConsistencyTest(String iURL) {
		Orient.instance().registerEngine(new OEngineRemote());
		url = iURL;
	}

	@Test
	public void setup() throws IOException {
		ODatabaseDocumentTx database = new ODatabaseDocumentTx(url);
		database.open("admin", "admin");

		long tot = database.countClass("Account");

		// DELETE ALL THE Account OBJECTS
		int i = 0;
		for (ODocument rec : database.browseClass("Account")) {
			rec.delete();
			i++;
		}

		Assert.assertEquals(i, tot);
		Assert.assertEquals(database.countClass("Account"), 0);

		database.close();
	}

	@Test(dependsOnMethods = "setup")
	public void createRecords() throws IOException {
		ODatabaseDocumentTx database = new ODatabaseDocumentTx(url);
		database.open("admin", "admin");

		ODocument record1 = new ODocument(database, "Account");
		record1.field("name", "Creation test").save();

		ODocument record2 = new ODocument(database, "Account");
		record2.field("name", "Update test").save();

		ODocument record3 = new ODocument(database, "Account");
		record3.field("name", "Delete test").save();

		database.close();
	}

	@Test(dependsOnMethods = "createRecords")
	public void testTransactionRecovery() throws IOException {
		ODatabaseDocumentTx db1 = new ODatabaseDocumentTx(url);
		db1.open("admin", "admin");

		// long tot = db1.countClass("Account");

		db1.begin();

		ODocument record1 = db1.newInstance("Account");
		record1.field("location", "This is the first version").save();

		ODocument record2 = db1.newInstance("Account");
		record2.field("location", "This is the first version").save();

		db1.commit();

		// Assert.assertEquals(db1.getClusterSize(db1.getMetadata().getSchema().getClass("Account").getDefaultClusterId()), rec);

		db1.close();
	}
}
