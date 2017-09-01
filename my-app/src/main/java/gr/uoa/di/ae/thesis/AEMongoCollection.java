package gr.uoa.di.ae.thesis;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.bson.Document;
//import org.bson.codecs.configuration.CodecRegistry;
//import org.bson.conversions.Bson;


import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
/*import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.ListCollectionsIterable;*/
import com.mongodb.client.MongoCollection;
/*import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.CreateViewOptions;*/

public class AEMongoCollection {
	
	private Map<String,EncryptionType> encrypted_fields;
	
	private MongoCollection<Document> collection;
	
	public AEMongoCollection(MongoCollection<Document> collection) {
		// TODO Auto-generated constructor stub
		encrypted_fields=new HashMap<String,EncryptionType>();
		this.collection=collection;
	}

	public void AEMongoCollectionCreateDoc() {	
		Scanner keyboard = new Scanner(System.in);
		while (true) {
			Document doc = new Document();
			System.out.println("Enter your name");
			String name = keyboard.next();
			doc.append("name", name);
			System.out.println("Enter your surname");
			String surname = keyboard.next();
			doc.append("surname", surname);
			System.out.println("Enter your age");
			int age = keyboard.nextInt();
			doc.append("age", age);
			System.out.println("Enter your class");
			int classof = keyboard.nextInt();
			doc.append("class", classof);
			collection.insertOne(doc);
			System.out.println("Do you want to create another document? (yes/no)");
			String answer = keyboard.next();
			if (answer.equals("no")) 
				break;
		} 
		keyboard.close();
	}
	
	public void setEncryptedField(String field,EncryptionType enc)
	{
		if(encrypted_fields.containsKey(field))
			throw new IllegalStateException("You have already defined an encryption type for the field "+field);
		else
			encrypted_fields.put(field, enc);
	}
	
	public Set<String> getEncryptedFields()
	{
		return encrypted_fields.keySet();
	}
	
	
	public void insertOne(Document document) 
	{
		for(Entry<String, Object> field:document.entrySet())
		{
			String field_name=field.getKey();
			String field_value=(String) field.getValue();
			if(encrypted_fields.containsKey(field_name))
			{
				EncryptionType enc=encrypted_fields.get(field_name);
				System.out.println("I have to encrypt field "+field_name+" the value "+field_value);
//				byte[]   bytesEncoded = Base64.encodeBase64(field_value.getBytes());
//				String encoded = Base64.getEncoder().encodeToString(field_value.getBytes());
//				byte [] encoded = Base64.getEncoder().withoutPadding().encodeToString();
//				field.setValue(new String(encoded));
				field.setValue("sha256 of "+field_value);
			}
					
		}
		collection.insertOne(document);
	}
	
	public FindIterable<Document> find(Document document)
	{
		FindIterable<Document> doc=collection.find(document);
		if(doc==null)
			return null;
		else
		{
			for(Document current:doc)
			{
				for(Entry<String,Object> field:current.entrySet())
				{
					String field_name=field.getKey();
					Object field_value=field.getValue();
					if(encrypted_fields.containsKey(field_name))
					{
						EncryptionType enc=encrypted_fields.get(field_name);
						System.out.println("I have to decrypt field "+field_name+" the value "+field_value);
						String decrypted=((String) field_value).replaceAll("sha256 of ","");
						field.setValue(decrypted);
						System.out.println("The decrypted field should be"+decrypted);
						System.out.println("the decrypted field is "+field.getValue());
					}
				}
			}
			
		}
		return doc;
	}
	
	
	public void insertMany(List<Document> records) {
		for(Document doc:records)
		{
			collection.insertOne(doc);
		}
	}
	
}
