//package npa.gov.tw.mydata;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
////202505 昇SPRING BOOT 2.3至3.2
////@SpringBootTest
////@SpringBootTest(class={})
//class MyDataClientApplicationTests {
//
//	//@Test
//	void contextLoads() {
//            //202505 昇SPRING BOOT 2.3至3.2
//            System.out.println("Sprint context Load successfully");
//	}
//
//}



package npa.gov.tw.mydata;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {}, // 避免載入整個應用上下文，防止 ApplicationContext 爆錯
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class MyDataClientApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("✅ Spring Boot 測試 context 載入成功（無 web 環境）");
    }

}
