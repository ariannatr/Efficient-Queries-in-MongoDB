package gr.uoa.di.ae.thesis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import com.github.fakemongo.Fongo;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

public class AEMongoCollectionTest {

	AEMongoCollection aeMongoCollection;
	
	Fongo fongo;

	MongoCollection<Document> collection;
	
	@Before
	public void init() {
		fongo = new Fongo("fongo db");
		collection = fongo.getDatabase("db").getCollection("collection");
		aeMongoCollection = new AEMongoCollection(collection);
	}
	
	@Test
	public void shouldAllowForSpecifyingEncryptedFields() {
		aeMongoCollection.setEncryptedField("e-mail", EncryptionType.HASH);
		assertTrue(aeMongoCollection.getEncryptedFields().contains("e-mail"));
	}
	
	@Test
	public void shouldAllowForInsertingDocuments() {
		
		Document document = new Document("name", "Michael").append("e-mail", "mike@bulls.com");
		
		aeMongoCollection.insertOne(document);
		FindIterable<Document> result = collection.find(document);
		assertEquals(document, result.first());
	}
	
	
	@Test
	public void shouldAllowForInsertingMultipleDocuments() {
		
		Document mike = new Document("name", "Michael").append("e-mail", "mike@bulls.com");
		Document scottie = new Document("name", "Scottie").append("e-mail", "scottie@bulls.com");
		
		aeMongoCollection.insertMany((Arrays.asList(mike, scottie)));
		FindIterable<Document> result = aeMongoCollection.find(mike);
		assertEquals(mike, result.first());
		result = aeMongoCollection.find(scottie);
		assertEquals(scottie, result.first());
		
	}
	
	@Test
	public void shouldStoreHashEncryptedFieldsUsingSHA256Hash() {
		aeMongoCollection.setEncryptedField("e-mail", EncryptionType.HASH);
		Document document = new Document("name", "Michael").append("e-mail", "mike@bulls.com");
		aeMongoCollection.insertOne(document);
		
		FindIterable<Document> result = collection.find(new Document("name", "Michael"));
		assertEquals("sha256 of mike@bulls.com", result.first().get("e-mail"));
		
		result = aeMongoCollection.find(new Document("name", "Michael"));
		assertEquals("mike@bulls.com", result.first().get("e-mail"));
		
	}
	
}
