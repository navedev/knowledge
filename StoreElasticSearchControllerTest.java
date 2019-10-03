package com.lowes.storeelasticsearch;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.ResourceUtils;

import com.lowes.storeelasticsearch.controller.StoreElasticSearchController;
import com.lowes.storeelasticsearch.service.StoreElasticSearchService;

/**
 * Class to Unit Test Controller
 * 
 * @author ndevara
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = StoreElasticSearchController.class)
public class StoreElasticSearchControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private StoreElasticSearchService storeElasticSearchService;

	@Test
	public void testCreateIndex() throws Exception {
		String uri = "/stores/create/nodelist_test/1/123456789";
		JSONObject jsonObject = null;
		try {
			File file = ResourceUtils.getFile("classpath:nodelist_test-123456789.json");
			String content = new String(Files.readAllBytes(file.toPath()));
			jsonObject = (JSONObject) new JSONParser().parse(content);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(uri).content(jsonObject.toString())
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());

	}

	@Test
	public void testRetrieveIndex() throws Exception {
		String uri = "/stores/retrieve/nodelist_test/1/123456789";
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());

	}

	@Test
	public void testUpdateIndex() throws Exception {
		String uri = "/stores/update/nodelist_test/1/123456789";
		JSONObject jsonObject = null;
		try {
			File file = ResourceUtils.getFile("classpath:nodelist_test-123456789.json");
			String content = new String(Files.readAllBytes(file.toPath()));
			jsonObject = (JSONObject) new JSONParser().parse(content);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put(uri)
				.header("Authorization", "Basic ZXN1c2VyOmVzcGFzc3dvcmQ=").content(jsonObject.toString())
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());

	}

	@Test
	public void testRemoveIndex() throws Exception {
		String uri = "/stores/remove/nodelist_test/1/123456789";
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete(uri)
				.header("Authorization", "Basic ZXN1c2VyOmVzcGFzc3dvcmQ=").accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());

	}

	@Test
	public void testDeleteAllIndex() throws Exception {
		String uri = "/stores/deleteAll/nodelist_test";
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete(uri)
				.header("Authorization", "Basic ZXN1c2VyOmVzcGFzc3dvcmQ=").accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());

	}

	@Test
	public void testLoadCache() throws Exception {
		String uri = "/stores/loadCache";
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put(uri)
				.header("Authorization", "Basic ZXN1c2VyOmVzcGFzc3dvcmQ=").accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());
	}

	@Test
	public void testClearCache() throws Exception {
		String uri = "/stores/clearCache";
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete(uri)
				.header("Authorization", "Basic ZXN1c2VyOmVzcGFzc3dvcmQ=").accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());
	}

	@Test
	public void testSendMsgToKafka() throws Exception {
		String uri = "/stores/sendKafkaMsg";
		JSONObject jsonObject = null;
		try {
			File file = ResourceUtils.getFile("classpath:yfsshipnode-S3.json");
			String content = new String(Files.readAllBytes(file.toPath()));
			jsonObject = (JSONObject) new JSONParser().parse(content);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(uri).content(jsonObject.toString())
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andReturn();

		assertEquals(200, mvcResult.getResponse().getStatus());
	}

}
