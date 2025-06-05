package npa.gov.tw.mydata.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import npa.gov.tw.mydata.bpo.impl.DataProviderBPO;


@RestController
@RequestMapping("/api/v1/mydata-dp")
public class MyDataDPController {
	
	Logger log = LoggerFactory.getLogger(MyDataDPController.class);
	
	@ResponseBody
    	@GetMapping(value = "/hello" ,produces=MediaType.APPLICATION_JSON_VALUE,headers="Accept=*/*")
      //@GetMapping(value = "/hello" ,produces=MediaType.APPLICATION_JSON_VALUE)
        public String printHello1() {
		String val = "{ \"userName\": \"OWEN\"}";
        log.info("printhello1 return : {}" , val);
        return val;
        }

	@ResponseBody
	@GetMapping(value = "/{resource}" ,produces=MediaType.APPLICATION_JSON_VALUE,headers="Accept=*/*")
	public ResponseEntity<byte[]> DPController(
			@PathVariable("resource") String resource,
            @RequestHeader Map<String, String> headers,
            @RequestParam(defaultValue="false") String heartbeat
			) throws Exception {
		
		log.info("DPController heartbeat={}", heartbeat);
		
        DataProviderBPO dpBpo = new DataProviderBPO() {};
        return dpBpo.handleResource(resource, headers, heartbeat.equals("true"));
		
	}




}
