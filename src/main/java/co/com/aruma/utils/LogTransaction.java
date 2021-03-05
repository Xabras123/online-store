package co.com.aruma.utils;
import java.util.Date;
import java.util.UUID;

import org.springframework.http.HttpHeaders;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogTransaction {
	
	private static final String LBL_START = "[{}] TRANSACTION STARTED";
    private static final String LBL_END = "[{}] TRANSACTION FINISHED";
    private static final String LBL_TRANSACTION_WSO2_USERNAME = "[{}] TRANSACTION_WSO2_USER: {}";
    private static final String LBL_REQUEST = "[{}] REST REQUEST  - Value [{}]";
    private static final String LBL_REQUEST_TYPE = "[{}] REST REQUEST  - Type  [{}]";
    private static final String LBL_RESPONSE = "[{}] REST RESPONSE - Value [{}]";
    private static final String LBL_TRANSACTION_DURATION = "[{}] TRANSACTION DURATION - {} milliseconds ";
    
    
    private String uuidOperation;
    private HttpHeaders headers;
    private String userTransaction;
    private Date start;
    
    public LogTransaction(HttpHeaders httpHeaders) {
    	this.uuidOperation = UUID.randomUUID().toString().replace("-", "");
    	this.headers = httpHeaders;
    	this.userTransaction = getUserFromToken();
    	this.start = new Date();
    }
    
	public String getUserTransaction() {
		return userTransaction;
	}
	public void startTransaction(String requestType,Object request) {
		
    	//logging
        log.info("--------------------------------------------------------------");
        log.info(LBL_START, uuidOperation);
        log.info(LBL_TRANSACTION_WSO2_USERNAME, uuidOperation, this.userTransaction);
        log.info(LBL_REQUEST_TYPE, uuidOperation, requestType);
        log.info(LBL_REQUEST, uuidOperation, request);        
    }
	
	private String getUserFromToken() {
		try {
			String token=this.headers.get("Authorization").get(0);
			token=token.replace("Bearer ", "");
			DecodedJWT jwt = JWT.decode(token);
			return jwt.getSubject();	
		}catch (Exception e) {
			return "ERROR_GET_USER_FROM_TOKEN";
		}		
	}

	public void endTransaction(Object response) {
		//logging                	
		log.info(LBL_RESPONSE, uuidOperation, response);
	    log.info(LBL_END, uuidOperation);
	    log.info(LBL_TRANSACTION_DURATION, uuidOperation, new Date().getTime()-this.start.getTime());
	    log.info("--------------------------------------------------------------");
    }
}
