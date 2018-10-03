package org.isf.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class Logging{
	static {
        SLF4JBridgeHandler.install();
    }
    private final Logger logger = LoggerFactory.getLogger(Logging.class);
    public void info(String message){
    	logger.info(message);
    }
}