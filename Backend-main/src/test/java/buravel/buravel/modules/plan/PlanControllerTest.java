package buravel.buravel.modules.plan;

import buravel.buravel.infra.mail.EmailService;
import buravel.buravel.modules.account.*;
import buravel.buravel.modules.account.dtos.AccountDto;
import buravel.buravel.modules.plan.dtos.PlanDto;
import buravel.buravel.modules.planTag.PlanTagRepository;
import buravel.buravel.modules.post.Post;
import buravel.buravel.modules.post.dtos.PostDto;
import buravel.buravel.modules.post.PostRepository;
import buravel.buravel.modules.postTag.PostTagRepository;
import buravel.buravel.modules.tag.TagRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class PlanControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    @MockBean
    EmailService emailService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    AccountService accountService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    PlanService planService;
    @Autowired
    PlanRepository planRepository;
    @Autowired
    PlanTagRepository planTagRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    PostTagRepository postTagRepository;
    @Autowired
    TagRepository tagRepository;

/**
 * ?????? create-drop?????? ???????????? ????????? ?????? ??? drop??? ????????????. ????????? application.properties?????? ???????????? db???
 * ?????????????????? ?????? ???????????? ?????????????????? ????????? - drop table if exists??? ?????? ?????? ??? ??? create?????? ????????? ?????? ??? ?????????
 * application.properties?????? buravel db??? ???????????? ?????? dialect?????? ????????? ????????? ??????
 * */
    @BeforeEach
    public void setUp() {
        tagRepository.deleteAll();
        postTagRepository.deleteAll();
        planTagRepository.deleteAll();
        postRepository.deleteAll();
        planRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("?????? ?????? ??????_??????")
    void createPlan() throws Exception {
        String token = getAccessToken();
        //login????????? security context holder??? ????????? ???????????? ????????????
        mockMvc.perform(post("/plans")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(createPlanDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("accountResponseDto").exists())
                .andExpect(jsonPath("planTagResponseDtos").exists())
                .andExpect(jsonPath("_links").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.search").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.updatePlan").exists())
                .andExpect(jsonPath("_links.deletePlan").exists())
                .andExpect(jsonPath("_links.getMyClosedPlans").exists())
                .andExpect(jsonPath("_links.getMyPublishedPlans").exists())
                .andDo(print());

    }
    @Test
    @DisplayName("?????? ?????? ??????_??????_top3??????")
    void createPlan_withTop3List() throws Exception {
        String token = getAccessToken();
        //login????????? security context holder??? ????????? ???????????? ????????????
        mockMvc.perform(post("/plans")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(createPlanDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("accountResponseDto").exists())
                .andExpect(jsonPath("planTagResponseDtos").exists())
                .andExpect(jsonPath("_links").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.search").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.updatePlan").exists())
                .andExpect(jsonPath("_links.deletePlan").exists())
                .andExpect(jsonPath("_links.getMyClosedPlans").exists())
                .andExpect(jsonPath("_links.getMyPublishedPlans").exists())
                .andDo(print());

        Account kiseok = accountRepository.findByUsername("kiseok");
        List<Plan> allByPlanManager = planRepository.findAllByPlanManager(kiseok);
        Plan plan = allByPlanManager.get(0);
        //?????? ?????? ???????????? 4?????? post??? ???????????? ??????????????? ?????? ????????? flight dish shopping  // etc
        assertThat(plan.getTop3List().contains("FLIGHT")).isTrue();
        assertThat(plan.getTop3List().contains("DISH")).isTrue();
        assertThat(plan.getTop3List().contains("SHOPPING")).isTrue();
    }
    @Test
    @DisplayName("?????? ?????? ??????_?????? / ????????? ????????? ??????????????? closed ")
    void createPlan_notPublishedPlansWithClosedPosts() throws Exception {
        String token = getAccessToken();
        //login????????? security context holder??? ????????? ???????????? ????????????
        mockMvc.perform(post("/plans")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(createPlanDto())))
                .andExpect(status().isOk())
                .andDo(print());

        Account kiseok = accountRepository.findByUsername("kiseok");
        List<Plan> allByPlanManager = planRepository.findAllByPlanManager(kiseok);
        Plan plan = allByPlanManager.get(0);
        List<Post> allByPlanOf = postRepository.findAllByPlanOf(plan);
        for (Post post : allByPlanOf) {
            assertThat(post.isClosed()).isTrue();
        }
    }
    @Test
    @DisplayName("?????? ?????? ??????_?????? / ?????? ????????? ??????????????? notClosed ")
    void createPlan_PublishedPlansWithNotClosedPosts() throws Exception {
        String token = getAccessToken();
        //login????????? security context holder??? ????????? ???????????? ????????????
        mockMvc.perform(post("/plans")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(createPlanDtoWithPublished())))
                .andExpect(status().isOk())
                .andDo(print());

        Account kiseok = accountRepository.findByUsername("kiseok");
        List<Plan> allByPlanManager = planRepository.findAllByPlanManager(kiseok);
        Plan plan = allByPlanManager.get(0);
        List<Post> allByPlanOf = postRepository.findAllByPlanOf(plan);
        for (Post post : allByPlanOf) {
            assertThat(post.isClosed()).isFalse();
        }
    }
    @Test
    @DisplayName("?????? ?????? ??????_??????_?????? ?????? ????????? ???????????? post??? ???????????? ?????????.")
    void createPlan_withOutPosts() throws Exception {
        String token = getAccessToken();
        //login????????? security context holder??? ????????? ???????????? ????????????
        mockMvc.perform(post("/plans")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(createPlanDto_withOutPosts())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("accountResponseDto").exists())
                .andExpect(jsonPath("planTagResponseDtos").exists())
                .andExpect(jsonPath("_links").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.search").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.updatePlan").exists())
                .andExpect(jsonPath("_links.deletePlan").exists())
                .andExpect(jsonPath("_links.getMyClosedPlans").exists())
                .andExpect(jsonPath("_links.getMyPublishedPlans").exists())
                .andDo(print());
        Account kiseok = accountRepository.findByUsername("kiseok");
        List<Plan> allByPlanManager = planRepository.findAllByPlanManager(kiseok);
        Plan plan = allByPlanManager.get(0);
        assertThat(postRepository.countByPlanOf(plan)).isEqualTo(0);

    }

    @Test
    @DisplayName("?????? ?????? ?????? - ??????(?????? ??????????????? ??? ?????? ??? ??????.)")
    void createPlan_fail() throws Exception {
        String token = getAccessToken();
        //login????????? security context holder??? ????????? ???????????? ????????????

        mockMvc.perform(post("/plans")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(createWrongPlanDto())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].defaultMessage").exists())
                .andDo(print());
    }
    @Test
    @DisplayName("?????? ?????? ?????? - ??????(without plan title.)")
    void createPlan_fail_withoutPlanTitle() throws Exception {
        String token = getAccessToken();
        //login????????? security context holder??? ????????? ???????????? ????????????

        PlanDto planDto = new PlanDto();
        planDto.setPublished(false);
        planDto.setStartDate(LocalDate.now());
        planDto.setEndDate(LocalDate.now().minusDays(1));
        planDto.setPlanTag("spring,java");
        planDto.setPostDtos(createPostDtos());

        mockMvc.perform(post("/plans")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(planDto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
    @Test
    @DisplayName("?????? ?????? ?????? - ??????(without plan startdate.)")
    void createPlan_fail_withoutPlanStartDate() throws Exception {
        String token = getAccessToken();
        //login????????? security context holder??? ????????? ???????????? ????????????

        Random rand = new Random();
        PlanDto planDto = new PlanDto();
        planDto.setPlanTitle(rand.nextInt(100)+"test");
        planDto.setPublished(false);
        planDto.setEndDate(LocalDate.now().plusDays(1));
        planDto.setPlanTag("spring,java");
        planDto.setPostDtos(createPostDtos());

        mockMvc.perform(post("/plans")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(planDto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
    @Test
    @DisplayName("?????? ?????? ?????? - ??????(without plan enddate.)")
    void createPlan_fail_withoutPlanEndDate() throws Exception {
        String token = getAccessToken();
        //login????????? security context holder??? ????????? ???????????? ????????????

        Random rand = new Random();
        PlanDto planDto = new PlanDto();
        planDto.setPlanTitle(rand.nextInt(100)+"test");
        planDto.setPublished(false);
        planDto.setStartDate(LocalDate.now());
        planDto.setPlanTag("spring,java");
        planDto.setPostDtos(createPostDtos());

        mockMvc.perform(post("/plans")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(planDto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("?????? ?????? ?????? ??????")
    void getPlan() throws Exception {
        String token = getAccessToken();
        //login????????? security context holder??? ????????? ???????????? ????????????
        mockMvc.perform(post("/plans")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(createPlanDto())));
        Long id = planRepository.findAll().get(0).getId();

        // ??????
        mockMvc.perform(get("/plans/{id}", id))
                .andDo(print())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("accountResponseDto").exists())
                .andExpect(jsonPath("planTagResponseDtos").exists())
                .andExpect(jsonPath("postForPlanResponseDtos").exists())
                .andExpect(jsonPath("_links").exists())
                .andExpect(jsonPath("_links.search").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.updatePlan").exists())
                .andExpect(jsonPath("_links.deletePlan").exists())
                .andExpect(jsonPath("_links.createPlan").exists())
                .andExpect(jsonPath("_links.getMyClosedPlans").exists())
                .andExpect(jsonPath("_links.getMyPublishedPlans").exists())
        ;

        Plan plan = planRepository.findById(id).get();
        //post 4??? ????????????
        assertThat(postRepository.countByPlanOf(plan)).isEqualTo(4);
        //planTag 2??? ???????????????
        assertThat(planTagRepository.count()).isEqualTo(2);
    }
    @Test
    @DisplayName("?????? ?????? ?????? ?????? ??????")
    void getPlan_fail() throws Exception {
        String token = getAccessToken();
        //login????????? security context holder??? ????????? ???????????? ????????????
        mockMvc.perform(post("/plans")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(createPlanDto())));
        // ??????
        mockMvc.perform(get("/plans/{id}", 10000))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("??? ????????? ?????? ?????? ??????")
    void getMyClosedPlans()throws Exception {
        String token = setting(false);

        mockMvc.perform(get("/plans/mine/closed")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(jsonPath("_embedded.planResponseDtoList[0].accountResponseDto.username").value("kiseok"))
                .andExpect(jsonPath("_embedded.planResponseDtoList[4].accountResponseDto.username").value("kiseok"))
                .andExpect(jsonPath("_embedded.planResponseDtoList[4]").exists()) // ???????????? 5???
                .andExpect(jsonPath("_embedded.planResponseDtoList[0].published").value(false))
                .andExpect(jsonPath("_embedded.planResponseDtoList[4].published").value(false))
                .andExpect(jsonPath("_embedded.planResponseDtoList[0]._links").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[0]._links.search").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[0]._links.profile").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[0]._links.deletePlan").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[0]._links.updatePlan").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[4]._links").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[4]._links.search").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[4]._links.profile").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[4]._links.self").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[4]._links.deletePlan").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[4]._links.updatePlan").exists())
                .andExpect(jsonPath("_links").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.getMyPublishedPlans").exists())
                .andExpect(jsonPath("_links.createPlan").exists())
                .andExpect(jsonPath("page.size").value(5))
                .andExpect(jsonPath("page.totalElements").value(11))
                .andExpect(jsonPath("page.totalPages").value(3))
                .andDo(print());
        Account kiseok = accountRepository.findByUsername("kiseok");
        List<Plan> plans = planRepository.findByPlanManagerAndPublished(kiseok, false);
        assertThat(plans.size()).isEqualTo(11);
    }

    @Test
    @DisplayName("??? ?????? ?????? ?????? ??????")
    void getMyPublishedPlans()throws Exception {
        String token = setting(true);

        mockMvc.perform(get("/plans/mine/published")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(jsonPath("_embedded.planResponseDtoList[0].accountResponseDto.username").value("kiseok"))
                .andExpect(jsonPath("_embedded.planResponseDtoList[4].accountResponseDto.username").value("kiseok"))
                .andExpect(jsonPath("_embedded.planResponseDtoList[4]").exists()) // ???????????? 5???
                .andExpect(jsonPath("_embedded.planResponseDtoList[0].published").value(true))
                .andExpect(jsonPath("_embedded.planResponseDtoList[4].published").value(true))
                .andExpect(jsonPath("_embedded.planResponseDtoList[0]._links").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[0]._links.search").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[0]._links.profile").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[0]._links.self").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[0]._links.deletePlan").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[0]._links.updatePlan").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[4]._links").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[4]._links.search").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[4]._links.profile").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[4]._links.self").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[4]._links.deletePlan").exists())
                .andExpect(jsonPath("_embedded.planResponseDtoList[4]._links.updatePlan").exists())
                .andExpect(jsonPath("_links").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.getMyClosedPlans").exists())
                .andExpect(jsonPath("_links.createPlan").exists())
                .andExpect(jsonPath("page.size").value(5))
                .andExpect(jsonPath("page.totalElements").value(11))
                .andExpect(jsonPath("page.totalPages").value(3))
                .andDo(print());
        Account kiseok = accountRepository.findByUsername("kiseok");
        List<Plan> plans = planRepository.findByPlanManagerAndPublished(kiseok, true);
        assertThat(plans.size()).isEqualTo(11);
    }

    private String setting(boolean published) throws Exception {
        String token = getAccessToken();
        //login????????? security context holder??? ????????? ???????????? ????????????
        if (published == true) {
            for (int i = 0; i < 11; i++) {
                mockMvc.perform(post("/plans")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(createPlanDtoWithPublished())));
            }
        } else {
            for (int i = 0; i < 11; i++) {
                mockMvc.perform(post("/plans")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(createPlanDto())));
            }
        }
        return token;
    }
    public PlanDto createPlanDtoWithPublished() {
        Random rand = new Random();
        PlanDto planDto = new PlanDto();
        planDto.setPlanTitle(rand.nextInt(100)+"test");
        planDto.setPublished(true);
        planDto.setStartDate(LocalDate.now());
        planDto.setEndDate(LocalDate.now().plusDays(1));
        planDto.setPlanTag("spring,java");
        planDto.setPostDtos(createPostDtos());
        return planDto;
    }
    public PlanDto createPlanDto() {
        Random rand = new Random();
        PlanDto planDto = new PlanDto();
        planDto.setPlanTitle(rand.nextInt(100)+"test");
        planDto.setPublished(false);
        planDto.setStartDate(LocalDate.now());
        planDto.setEndDate(LocalDate.now().plusDays(1));
        planDto.setPlanTag("spring,java");
        planDto.setPostDtos(createPostDtos());
        return planDto;
    }

    public PlanDto createPlanDto_withOutPosts() {
        Random rand = new Random();
        PlanDto planDto = new PlanDto();
        planDto.setPlanTitle(rand.nextInt(100)+"test");
        planDto.setPublished(false);
        planDto.setStartDate(LocalDate.now());
        planDto.setEndDate(LocalDate.now().plusDays(1));
        planDto.setPlanTag("spring,java");
        return planDto;
    }

    public PlanDto createWrongPlanDto() {
        PlanDto planDto = new PlanDto();
        planDto.setPlanTitle("test");
        planDto.setPublished(false);
        planDto.setStartDate(LocalDate.now());
        planDto.setEndDate(LocalDate.now().minusDays(1));
        planDto.setPlanTag("spring,java");
        planDto.setPostDtos(createPostDtos());
        return planDto;
    }
    private PostDto[][] createPostDtos() {
        PostDto[][] postDtos = new PostDto[2][2];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                PostDto postDto = new PostDto();
                postDto.setPostTitle("posts");
                postDto.setPrice(i+j+10000l);
                if (i == 0 && j == 0) {
                    postDto.setCategory("ETC");
                } else if (i == 0 && j == 1) {
                    postDto.setCategory("FLIGHT");
                } else if (i == 1 && j == 0) {
                    postDto.setCategory("DISH");
                } else {
                    postDto.setCategory("SHOPPING");
                }
                postDto.setRating(4.0f);
                postDto.setLng(12.345);
                postDto.setLat(54.321);
                postDto.setLocation("?????????");
                postDto.setTags("posts,tag");
                postDtos[i][j] = postDto;
            }
        }
        return postDtos;
    }

    private String getAccessToken() throws Exception {
        AccountDto accountDto = new AccountDto();
        accountDto.setUsername("kiseok");
        accountDto.setEmail("kisa0828@naver.com");
        accountDto.setPassword("123456789");
        accountDto.setNickname("hello");
        accountService.processNewAccount(accountDto);

        ResultActions perform = mockMvc.perform(post("/login")
                .content(objectMapper.writeValueAsString(accountDto)));
        String token = perform.andReturn().getResponse().getHeader("Authorization");
        return token;
    }

}