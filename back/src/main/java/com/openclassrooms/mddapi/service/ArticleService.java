package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.ArticleDto;
import com.openclassrooms.mddapi.dto.article.CreateArticleRequest;
import com.openclassrooms.mddapi.entity.ArticleEntity;
import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.entity.TopicEntity;
import com.openclassrooms.mddapi.mapper.ArticleMapper;
import com.openclassrooms.mddapi.repository.ArticleRepository;
import com.openclassrooms.mddapi.repository.MddUserRepository;
import com.openclassrooms.mddapi.repository.TopicRepository;
import com.openclassrooms.mddapi.repository.UserTopicSubscriptionRepository;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final TopicRepository topicRepository;
    private final MddUserRepository userRepository;
    private final UserTopicSubscriptionRepository subscriptionRepository;
    private final ArticleMapper articleMapper;

    public ArticleService(
            ArticleRepository articleRepository,
            TopicRepository topicRepository,
            MddUserRepository userRepository,
            UserTopicSubscriptionRepository subscriptionRepository,
            ArticleMapper articleMapper) {
        this.articleRepository = articleRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.articleMapper = articleMapper;
    }

    @Transactional(readOnly = true)
    public List<ArticleDto> getAll(MddUserEntity principal) {

        Long principalId = requireAuthenticatedUserId(principal);
        // Récupère les IDs des topics auxquels l'utilisateur est abonné
        List<Long> topicIds = subscriptionRepository.findAllByUser_Id(principalId)
                .stream()
                .map(sub -> sub.getTopic().getId())
                .toList();

        // Filtre les articles par ces topicIds
        return articleRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(article -> topicIds.contains(article.getTopic().getId()))
                .map(articleMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArticleDto getById(Long id) {
        ArticleEntity article = articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));
        return articleMapper.toDto(article);
    }

    @Transactional
    public ArticleDto create(MddUserEntity principal, CreateArticleRequest request) {
        Long principalId = requireAuthenticatedUserId(principal);

        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payload");
        }
        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
        if (request.content() == null || request.content().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content is required");
        }

        TopicEntity topic = topicRepository.findById(request.topicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));

        ArticleEntity entity = new ArticleEntity();
        entity.setTitle(request.title().trim());
        entity.setContent(request.content().trim());
        entity.setTopic(topic);
        entity.setAuthor(userRepository.getReferenceById(principalId));

        ArticleEntity saved = articleRepository.save(entity);
        return articleMapper.toDto(saved);
    }

    private static Long requireAuthenticatedUserId(MddUserEntity principal) {
        if (principal == null || principal.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return principal.getId();
    }
}
