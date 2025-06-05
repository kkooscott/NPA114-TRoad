/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package npa.gov.tw.mydata.bpo.intf;

/**
 *
 * @author Administrator
 */
import java.util.Map;
import org.springframework.http.ResponseEntity;

public interface DataProviderIntf {

    public ResponseEntity<byte[]> handleResource(
            String resource,
            Map<String, String> headers,
            Boolean heartbeat) throws Exception;
}
