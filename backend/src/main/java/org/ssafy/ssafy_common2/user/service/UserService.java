package org.ssafy.ssafy_common2.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.ssafy.ssafy_common2._common.exception.CustomException;
import org.ssafy.ssafy_common2._common.exception.ErrorType;
import org.ssafy.ssafy_common2._common.infra.oauth.entity.KakaoProfile;
import org.ssafy.ssafy_common2._common.infra.oauth.entity.OauthToken;
import org.ssafy.ssafy_common2._common.jwt.JwtUtil;
import org.ssafy.ssafy_common2.user.entity.DynamicUserInfo;
import org.ssafy.ssafy_common2.user.entity.User;
import org.ssafy.ssafy_common2.user.repository.AliasRepository;
import org.ssafy.ssafy_common2.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AliasRepository aliasRepository;

    @Value("${kakao.clientId}")
    String client_id;

    @Value("${kakao.secret}")
    String client_secret;

    public OauthToken getAccessTokenDist(String code) {

        OauthToken oauthToken = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
            // HttpBody 오브젝트 생성
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", client_id);
            params.add("redirect_uri", "http://i10d110.p.ssafy.io:3000/api/oauth/callback/kakao/token");
            params.add("code", code);
            params.add("client_secret", client_secret);

//            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            RestTemplate rt = new RestTemplate();
            HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                    new HttpEntity<>(params, headers);


            ObjectMapper objectMapper =
                    new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//                    new ObjectMapper();

            // POST 방식으로 key=value 데이터 요청

            ResponseEntity<String> accessTokenResponse = rt.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    kakaoTokenRequest,
                    String.class
            );
            oauthToken = objectMapper.readValue(accessTokenResponse.getBody(), OauthToken.class);
            System.out.println(oauthToken.getAccess_token());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return oauthToken;
    }

    public OauthToken getAccessTokenLocal(String code) {

        OauthToken oauthToken = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
            // HttpBody 오브젝트 생성
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", client_id);
            params.add("redirect_uri", "http://localhost:3000/api/oauth/callback/kakao/token");
            params.add("code", code);
            params.add("client_secret", client_secret);

//            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            RestTemplate rt = new RestTemplate();
            HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                    new HttpEntity<>(params, headers);


            ObjectMapper objectMapper =
                    new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//                    new ObjectMapper();

            // POST 방식으로 key=value 데이터 요청

            ResponseEntity<String> accessTokenResponse = rt.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    kakaoTokenRequest,
                    String.class
            );
            oauthToken = objectMapper.readValue(accessTokenResponse.getBody(), OauthToken.class);
            System.out.println(oauthToken.getAccess_token());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return oauthToken;
    }

    public List<String> SaveUserAndGetToken(String token, HttpServletResponse response) {

        //(1)
        KakaoProfile profile = findProfile(token);

        //(2)
        User user = userRepository.findByKakaoEmailAndDeletedAtIsNull(profile.getKakao_account().getEmail()).orElse(null);

        System.out.println("카카오 이메일 : " + profile.getKakao_account().getProfile().getProfile_image_url());

        boolean isUserNull = false;

        //(3)
        if (user == null) {

            DynamicUserInfo userInfo = DynamicUserInfo.of(100, false, 0, "https://ssafys3.s3.ap-northeast-2.amazonaws.com/static/%ED%8B%B0%EB%AA%A8+%EB%B0%B0%EA%B2%BD.jpg");
            user = User.of(
                    profile.getId(),
                    profile.getKakao_account().getProfile().getProfile_image_url(),
                    profile.getKakao_account().getProfile().getNickname(),
                    profile.getKakao_account().getEmail(),
                    "ROLE_USER",
                    userInfo);

            userRepository.save(user);
            isUserNull = true;
        }
        String jwtToken = jwtUtil.createToken(user.getKakaoEmail());
        response.addHeader("Authorization", jwtToken);

        List<String> ans = new ArrayList<>();
        ans.add(jwtToken);
        ans.add(String.valueOf(isUserNull));
        return ans;
    }

    public User getUser(User nowUser) {

//        Long userCode = (Long) nowUser.getAttribute("userCode");
//
//        User user = userRepository.findById(userCode).orElseThrow(
//                ()->new CustomException(ErrorType.NOT_FOUND_USER)
//        );

        return nowUser;
    }


    //(1-1)
    public KakaoProfile findProfile(String token) {

        //(1-2)
        RestTemplate rt = new RestTemplate();

        System.out.println("token : " + token);

        //(1-3)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token); //(1-4)
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        //(1-5)
        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest =
                new HttpEntity<>(headers);

        //(1-6)
        // Http 요청 (POST 방식) 후, response 변수에 응답을 받음
        ResponseEntity<String> kakaoProfileResponse = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest,
                String.class
        );

        //(1-7)
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoProfile kakaoProfile = null;
        try {
            kakaoProfile = objectMapper.readValue(kakaoProfileResponse.getBody(), KakaoProfile.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return kakaoProfile;
    }

   /* public JsonNode Logout(String autorize_code){

        final String RequestUrl = "https://kapi.kakao.com/v1/user/logout";

        final HttpClient client = HttpClientBuilder.create().build();

        final HttpPost post =new HttpPost(RequestUrl);

        post.addHeader("Authorization","Bearer" + autorize_code);

        JsonNode returnNode =null;

        try{
            final HttpResponse response = client.execute(post);

             ObjectMapper mapper = new ObjectMapper();

             returnNode = mapper.readTree(response.getEntity().getContent());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch(ClientProtocolException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        } finally{

        }
        return returnNode;}*/

    public User validateUserByEmail(String email){

        return userRepository.findByKakaoEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND_USER));
    }
}
