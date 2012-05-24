package okapi.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import okapi.shared.Record;
import okapi.shared.RestServices;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class RestServicesTest {

	final static String TEST_TM = "Test TM";
	final static String RENAMED_TEST_TM = "Renamed Test TM";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
		
		RestServices tmClient = ProxyFactory.create(RestServices.class, "http://localhost:8080/olifant-tmserver/rest");

		//--RENAME TM--
		tmClient.rename(null, TEST_TM, RENAMED_TEST_TM);
		
		String uuid = tmClient.getUUID(null, RENAMED_TEST_TM);
		System.out.println("uuid: "+uuid);
        Assert.assertNotNull(uuid);
        
		String desc = tmClient.getDescription(null, RENAMED_TEST_TM);
		System.out.println("desc: "+desc);
        Assert.assertNotNull(desc);
        
		long count = tmClient.getSegmentCount(null, RENAMED_TEST_TM);
		System.out.println("Number of segments: "+ count);
        Assert.assertNotNull(count);

		long key = tmClient.addRecord(null, RENAMED_TEST_TM, createRecord("a"));
		System.out.println("Key of added: " + key);
        Assert.assertNotNull(key);
        
		key = tmClient.addRecord(null, RENAMED_TEST_TM, createRecord("b"));
		System.out.println("Key of added: " + key);
        Assert.assertNotNull(key);
        
		count = tmClient.getSegmentCount(null, RENAMED_TEST_TM);
		System.out.println("Number of segments: " + count);
        Assert.assertNotNull(count);
        
		tmClient.updateRecord(null, RENAMED_TEST_TM, 1, createRecord("c"));
	    
		count = tmClient.getSegmentCount(null, RENAMED_TEST_TM);
		System.out.println("Number of segments: " + count);
        Assert.assertNotNull(count);

		System.out.println("Deleting record "+ 2);
		tmClient.deleteRecord(null, RENAMED_TEST_TM, 2);

		count = tmClient.getSegmentCount(null, RENAMED_TEST_TM);
		System.out.println("Number of segments: " + count);
        Assert.assertNotNull(count);
        Assert.assertEquals(1, count);
        
		HashMap<String, String> mockRecord = tmClient.getRecord(null, RENAMED_TEST_TM, 1);
		System.out.println("mock record: "+ mockRecord);
        Assert.assertNotNull(mockRecord);
 
        
        //--LOCALE RELATED--
        //LIST
        List<String> locales = tmClient.getLocales(null, RENAMED_TEST_TM);
        System.out.println("Locales: " + locales);

        //CREATE
        tmClient.addLocale(null, RENAMED_TEST_TM, "FR");
        locales = tmClient.getLocales(null, RENAMED_TEST_TM);
        System.out.println("Locales: " + locales);
        
        //UPDATE
        tmClient.renameLocale(null, RENAMED_TEST_TM, "FR", "DE");
        locales = tmClient.getLocales(null, RENAMED_TEST_TM);
        System.out.println("Locales: " + locales);
        
        //DELETE
        tmClient.deleteLocale(null, RENAMED_TEST_TM, "DE");
        locales = tmClient.getLocales(null, RENAMED_TEST_TM);
        System.out.println("Locales: " + locales);
        
        
        //--FIELD RELATED--
        //LIST
        List<String> fields = tmClient.getFields(null, RENAMED_TEST_TM);
        System.out.println("Fields: " + locales);

        //CREATE
        tmClient.addField(null, RENAMED_TEST_TM, "NewTuField");
        fields = tmClient.getFields(null, RENAMED_TEST_TM);
        System.out.println("Fields: " + fields);
        
        //UPDATE
        tmClient.renameField(null, RENAMED_TEST_TM, "NewTuField", "UpdatedTuField");
        fields = tmClient.getFields(null, RENAMED_TEST_TM);
        System.out.println("Fields: " + fields);
        
        //DELETE
        tmClient.deleteField(null, RENAMED_TEST_TM, "UpdatedTuField");
        fields = tmClient.getFields(null, RENAMED_TEST_TM);
        System.out.println("Fields: " + fields);
        
        
        //--PAGE RELATED
        count = tmClient.getPageCount(null, RENAMED_TEST_TM);
        System.out.println("Number of pages: " + count);
        
        List<HashMap<String, String>> page = tmClient.getPage(null, RENAMED_TEST_TM, 0, 500, "iterator");
        System.out.println("Page 1: " + page);        
}
	
	
	static Record createRecord(String index){
        
		Record rec = new Record();
		Map<String, Object> tuFields = new HashMap<String, Object>();
        Map<String, Object> segFields = new HashMap<String, Object>();
        
        tuFields.put("Tu Field 1", "Tu Field Val 1"+index);
        tuFields.put("Tu Field 2", "Tu Field Val 2"+index);
        segFields.put("Text~EN", "EN Seg Text 1"+index);
        segFields.put("Field~EN", "EN Seg Field 1"+index);
        rec.tuKey = -1;
        rec.tuFields = tuFields;
        rec.segFields = segFields;
        
        return rec;
	}
}
