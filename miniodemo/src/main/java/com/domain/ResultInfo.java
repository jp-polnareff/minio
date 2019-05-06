package com.domain;

import java.io.Serializable;

public class ResultInfo<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String code = "00";
	public boolean success;
	public String message = "";
	public T data;
	
	

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	

}

