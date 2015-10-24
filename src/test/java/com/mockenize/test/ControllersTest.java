package com.mockenize.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mockenize.controller.AdminController;
import com.mockenize.controller.MockenizeController;
import com.mockenize.model.MockBeanList;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ControllersTest.class })
@ComponentScan(basePackages = { "com.mockenize" })
public class ControllersTest {

	@Autowired
	private AdminController adminController;

	@Autowired
	private MockenizeController mockenizeController;

	@Test
	public void addMock() {
		String url = "/test/200";
		String body = "{\"msg\":\"success\"}";
		int status = 200;
		String contentType = "application/json";
		String key = "X-KEY";
		String key2 = "X-KEY";
		String value = "X-VALUE";

		MockBeanList mockBeanList = new MockBeanList();
		mockBeanList.setResponseCode(status);
		mockBeanList.setBody(body);
		mockBeanList.setUrl(url);
		mockBeanList.setContentType(contentType);
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(key, value);
		headers.put(key2, value);
		mockBeanList.setHeaders(headers);
		adminController.insert(mockBeanList);
		
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getRequestURI()).thenReturn(url);
		Response response = mockenizeController.get(request);
		
		assertEquals(status, response.getStatus());
		assertEquals(contentType, response.getMediaType().toString());
		assertEquals(body, response.getEntity());
		assertEquals(value, response.getHeaderString(key));
		assertEquals(value, response.getHeaderString(key2));
	}
	
	@Test
	public void deleteMock() {
		String url = "/test/200";
		String body = "{\"msg\":\"success\"}";

		MockBeanList mockBeanList = new MockBeanList();
		mockBeanList.setBody(body);
		mockBeanList.setUrl(url);
		
		adminController.insert(mockBeanList);
		
		Map<String, String> values = new HashMap<String, String>();
		values.put("url", url);
		adminController.delete(Arrays.asList(values));
		
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getRequestURI()).thenReturn(url);
		Response response = mockenizeController.get(request);
		
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
	}
	
	@Test
	public void getNotFound() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getRequestURI()).thenReturn("");
		Response response = mockenizeController.get(request);
		
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
	}
	
	
}
