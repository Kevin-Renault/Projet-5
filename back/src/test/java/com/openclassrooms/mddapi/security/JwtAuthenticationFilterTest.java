package com.openclassrooms.mddapi.security;

import com.openclassrooms.mddapi.entity.MddUserEntity;
import com.openclassrooms.mddapi.repository.MddUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtCookieService cookieService;

    @Mock
    private MddUserRepository userRepository;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_skips_when_authentication_already_present() throws ServletException, IOException {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("already", null));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        Mockito.verify(chain).doFilter(request, response);
        Mockito.verifyNoInteractions(jwtService, cookieService, userRepository);
    }

    @Test
    void doFilter_skips_when_no_token() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        Mockito.verify(chain).doFilter(request, response);
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        Mockito.verifyNoInteractions(jwtService);
    }

    @Test
    void doFilter_skips_when_bearer_token_is_blank() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        Mockito.verify(chain).doFilter(request, response);
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        Mockito.verifyNoInteractions(jwtService);
    }

    @Test
    void doFilter_skips_when_token_invalid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        Mockito.when(jwtService.isTokenValid("invalid")).thenReturn(false);

        filter.doFilter(request, response, chain);

        Mockito.verify(chain).doFilter(request, response);
        Mockito.verify(jwtService).isTokenValid("invalid");
        Mockito.verify(jwtService, Mockito.never()).extractSubject(Mockito.anyString());
        Mockito.verifyNoInteractions(userRepository);
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_skips_when_subject_is_not_a_number() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        Mockito.when(jwtService.isTokenValid("valid")).thenReturn(true);
        Mockito.when(jwtService.extractSubject("valid")).thenReturn("not-a-number");

        filter.doFilter(request, response, chain);

        Mockito.verify(chain).doFilter(request, response);
        Mockito.verify(jwtService).extractSubject("valid");
        Mockito.verifyNoInteractions(userRepository);
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_skips_when_user_not_found() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        Mockito.when(jwtService.isTokenValid("valid")).thenReturn(true);
        Mockito.when(jwtService.extractSubject("valid")).thenReturn("42");
        Mockito.when(userRepository.findById(42L)).thenReturn(Optional.empty());

        filter.doFilter(request, response, chain);

        Mockito.verify(chain).doFilter(request, response);
        Mockito.verify(userRepository).findById(42L);
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_sets_authentication_when_token_valid_from_header() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        MddUserEntity user = new MddUserEntity();
        user.setId(1L);

        Mockito.when(jwtService.isTokenValid("valid")).thenReturn(true);
        Mockito.when(jwtService.extractSubject("valid")).thenReturn("1");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        filter.doFilter(request, response, chain);

        Mockito.verify(chain).doFilter(request, response);
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isSameAs(user);
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication().getDetails()).isNotNull();
    }

    @Test
    void doFilter_extracts_token_from_cookie_when_no_bearer_header() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("access_token", "cookieToken"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        MddUserEntity user = new MddUserEntity();
        user.setId(5L);

        Mockito.when(cookieService.getCookieName()).thenReturn("access_token");
        Mockito.when(jwtService.isTokenValid("cookieToken")).thenReturn(true);
        Mockito.when(jwtService.extractSubject("cookieToken")).thenReturn("5");
        Mockito.when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        filter.doFilter(request, response, chain);

        Mockito.verify(chain).doFilter(request, response);
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isSameAs(user);
    }
}
