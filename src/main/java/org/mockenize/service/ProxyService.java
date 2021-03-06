package org.mockenize.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mockenize.exception.ProxyPathException;
import org.mockenize.model.ProxyBean;
import org.mockenize.repository.ProxyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;

@Service
public class ProxyService implements ResponseService {

	@Autowired
	private ProxyRepository proxyRepository;

	@Context
	private HttpServletRequest request;

	private Client client = ClientBuilder.newClient();

	public Collection<ProxyBean> getAll() {
		return proxyRepository.findAll();
	}

	public void save(ProxyBean proxyBean) {
		proxyRepository.save(proxyBean);
	}

	public ProxyBean getByKey(String key) {
		return proxyRepository.findByKey(key);
	}

	public ProxyBean delete(ProxyBean proxyBean) {
		return proxyRepository.delete(proxyBean);
	}
	
	public Collection<ProxyBean>  deleteAll(Collection<ProxyBean> proxyBeans) {
		Collection<ProxyBean> deletedProxies = new ArrayList<>();
		if(proxyBeans != null && !proxyBeans.isEmpty()) {
			for (ProxyBean bean : proxyBeans) {
				ProxyBean deleted = proxyRepository.delete(bean);
				if(deleted != null) {
					deletedProxies.add(deleted);
				}
			}
		} else {
			deletedProxies = proxyRepository.deleteAll();
		}
		return deletedProxies;
	}
	
	@Override
	public Response getResponse(HttpServletRequest request, JsonNode requestBody) {
		String path = request.getRequestURI();
		ProxyBean proxyBean = proxyRepository.findByKey(path);
		Builder requestBuilder = client.target(proxyBean.getUrl()).path(getRequestPath(proxyBean.getPath(), path))
				.request();
		addHeaders(request, requestBuilder);
		Entity<JsonNode> entity = Entity.entity(requestBody, getContentType(request));
		Response response = requestBuilder.build(request.getMethod(), entity).invoke();
		response.bufferEntity();
		return response;
	}

	private String getRequestPath(String path, String requestedPath) {
		if (path.matches("/.+")) {
			Pattern pattern = Pattern.compile("/[^/\\.]+(.*)");
			Matcher matcher = pattern.matcher(requestedPath);
			if (matcher.matches()) {
				return matcher.group(1);
			} else {
				throw new ProxyPathException();
			}
		}

		return null;
	}

	private void addHeaders(HttpServletRequest request, Builder requestBuilder) {
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = headerNames.nextElement();
			requestBuilder.header(key, request.getHeader(key));
		}
	}

	private String getContentType(HttpServletRequest request) {
		if (Strings.isNullOrEmpty(request.getContentType())) {
			return MediaType.TEXT_PLAIN;
		}

		return request.getContentType();
	}

	public boolean exists(String path) {
		return proxyRepository.exists(path);
	}

}
