package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.entity.TopicEntity;
import com.openclassrooms.mddapi.mapper.TopicMapper;
import com.openclassrooms.mddapi.repository.TopicRepository;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private TopicMapper topicMapper;

    @InjectMocks
    private TopicService service;

    @Test
    void getAll_uses_sort_and_maps() {
        TopicEntity entity = new TopicEntity();
        entity.setId(1L);
        Mockito.when(topicRepository.findAll(Mockito.any(Sort.class))).thenReturn(List.of(entity));

        TopicDto dto = new TopicDto(1L, "t", "d");
        Mockito.when(topicMapper.toDto(entity)).thenReturn(dto);

        List<TopicDto> out = service.getAll();
        Assertions.assertThat(out).containsExactly(dto);

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        Mockito.verify(topicRepository).findAll(sortCaptor.capture());
        Assertions.assertThat(sortCaptor.getValue().getOrderFor("name")).isNotNull();
    }

    @Test
    void getById_not_found_throws_404() {
        Mockito.when(topicRepository.findById(123L)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> service.getById(123L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }
}
