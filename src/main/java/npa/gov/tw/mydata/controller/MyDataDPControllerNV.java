package npa.gov.tw.mydata.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
//@RequestMapping("/test")
@RequestMapping("/prod")
public class MyDataDPControllerNV {

	@PostMapping("/post-to-108")
	public ResponseEntity<byte[]> postTo108(@RequestBody Map<String, String> input) {
		try {
			String targetUrl = "http://localhost:18081/NM108-604Client/api/v1/myDataDp01";
			System.out.println("###### 準備 POST 給 108：" + targetUrl);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<Map<String, String>> request = new HttpEntity<>(input, headers);

			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<byte[]> response = restTemplate.postForEntity(targetUrl, request, byte[].class);

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mydata-output.zip")
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(response.getBody());
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(("錯誤：" + e.getMessage()).getBytes());
		}
	}
}
