package com.increff.account.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class QueryTokenData {

	private boolean successful;
	private String token;
	private String message;

}
