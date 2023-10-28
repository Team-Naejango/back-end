package com.example.naejango.domain.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.Preprocessors;

import static org.springframework.restdocs.snippet.Attributes.Attribute;

@TestConfiguration
public class RestDocsConfig {

    @Bean
    public RestDocumentationResultHandler write(){
        return MockMvcRestDocumentation.document(
                "{class-name}/{method-name}", // build/generated-snippets에 생기는 .adoc 파일이 저장될 폴더명 설정
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()), // request body의 JSON을 보기 좋게 출력
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()) // response body의 JSON을 보기 좋게 출력
        );
    }

    // 커스텀으로 넣는 컬럼을 넣기 편하게 사용할 메서드
    public static final Attribute field(final String key, final String value){
        return new Attribute(key,value);
    }
}
