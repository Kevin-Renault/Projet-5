package com.openclassrooms.mddapi.dto.article;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CreateArticleRequestTest {

    @Test
    void record_exposes_components() {
        CreateArticleRequest req = new CreateArticleRequest("title", "content", 42L);

        Assertions.assertThat(req.title()).isEqualTo("title");
        Assertions.assertThat(req.content()).isEqualTo("content");
        Assertions.assertThat(req.topicId()).isEqualTo(42L);
    }
}
