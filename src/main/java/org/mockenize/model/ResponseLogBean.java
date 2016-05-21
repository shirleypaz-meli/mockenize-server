package org.mockenize.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class ResponseLogBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer status;

	private Map<String, String> headers = new HashMap<>();

	private transient JsonNode body;
}
