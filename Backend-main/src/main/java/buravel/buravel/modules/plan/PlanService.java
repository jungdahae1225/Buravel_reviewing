package buravel.buravel.modules.plan;

import buravel.buravel.modules.account.Account;
import buravel.buravel.modules.account.AccountRepository;
import buravel.buravel.modules.account.dtos.AccountResponseDto;
import buravel.buravel.modules.bookmarkPost.BookmarkPost;
import buravel.buravel.modules.bookmarkPost.BookmarkPostRepository;
import buravel.buravel.modules.bookmarkPost.BookmarkPostService;
import buravel.buravel.modules.plan.dtos.*;
import buravel.buravel.modules.planTag.PlanTag;
import buravel.buravel.modules.planTag.PlanTagRepository;
import buravel.buravel.modules.planTag.PlanTagResponseDto;
import buravel.buravel.modules.post.*;
import buravel.buravel.modules.post.dtos.PatchPostReponseDto;
import buravel.buravel.modules.post.dtos.PostDto;
import buravel.buravel.modules.post.dtos.PostForPlanResponseDto;
import buravel.buravel.modules.postTag.PostTag;
import buravel.buravel.modules.postTag.PostTagRepository;
import buravel.buravel.modules.postTag.PostTagResponseDto;
import buravel.buravel.modules.tag.Tag;
import buravel.buravel.modules.tag.TagRepository;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Service
@Transactional
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final TagRepository tagRepository;
    private final PlanTagRepository planTagRepository;
    private final PostTagRepository postTagRepository;
    private final BookmarkPostRepository bookmarkPostRepository;
    private final BookmarkPostService bookmarkPostService;
    private final ModelMapper modelMapper;


    public Plan createPlan(PlanDto planDto, Account account) {
        Account user = accountRepository.findById(account.getId()).get();

        Plan plan = generatePlan(planDto, user);
        PostDto[][] postDtos = planDto.getPostDtos();
        if (postDtos == null) {
            return plan;
        }
        generatePosts(plan,postDtos,user);
        settingOutputPlanTotalPrice(plan);
        settingPlanRating(plan);
        settingTop3ListOfPlan(plan);
        return plan;
    }

    private void settingPlanRating(Plan plan) {
        int count = postRepository.countByPlanOf(plan);
        Float planRating = plan.getPlanRating();
        String val = (planRating / count) + "";
        val = val.substring(0, 3);
        float result = Float.parseFloat(val);
        plan.setPlanRating(result);
    }

    private void settingTop3ListOfPlan(Plan plan) {
        // ????????? ??? ????????? ?????? ??????????????? ?????? ????????????. ??????
        HashMap<String,Long> map = new HashMap<>();
        if (plan.getFlightTotalPrice() > 0) {
            map.put("FLIGHT",plan.getFlightTotalPrice());
        }
        if (plan.getDishTotalPrice() > 0) {
            map.put("DISH",plan.getDishTotalPrice());
        }
        if (plan.getShoppingTotalPrice() > 0) {
            map.put("SHOPPING",plan.getShoppingTotalPrice());
        }
        if (plan.getHotelTotalPrice() > 0) {
            map.put("HOTEL",plan.getHotelTotalPrice());
        }
        if (plan.getTrafficTotalPrice() > 0) {
            map.put("TRAFFIC",plan.getTrafficTotalPrice());
        }
        if (plan.getEtcTotalPrice() > 0) {
            map.put("ETC",plan.getEtcTotalPrice());
        }
        Object[] objects = map.values().toArray();
        Arrays.sort(objects,Collections.reverseOrder());
        List<Long> target = new ArrayList<>();
        for (Object value : objects) {
            target.add((Long)value);
            if (target.size() >= 3) {
                break;
            }
        }

        for (Long aLong : target) {
            for (String keyset : map.keySet()) {
                if (map.get(keyset) == aLong) {
                    plan.getTop3List().add(keyset);
                }
            }
        }

    }

    private void settingOutputPlanTotalPrice(Plan plan) {
        Long price = plan.getTotalPrice();
        if (price < 10000) {
            plan.setOutputPlanTotalPrice(price + "???");
        } else {
            String string = price.toString();
            string = string.substring(0, string.length()-4);
            plan.setOutputPlanTotalPrice(string+"??????");
        }
    }


    private void generatePosts(Plan plan, PostDto[][] postDtos, Account user) {
        for (int i = 0; i < postDtos.length; i++) {
            for (int j = 0; j < postDtos[i].length; j++) {
                PostDto postDto = postDtos[i][j];
                Post post = new Post();
                post.setPostTitle(postDto.getPostTitle());
                post.setPrice(postDto.getPrice());
                plan.setTotalPrice(plan.getTotalPrice()+postDto.getPrice());
                post.setOutputPrice(generateOutputPrice(postDto.getPrice()));
                settingPostCategory(plan,post, postDto);
                if (postDto.getPostImage() != null) {
                    post.setPostImage(postDto.getPostImage());
                }else{
                    setPostImageWithCategory(post);
                }

                post.setRating(postDto.getRating());
                plan.setPlanRating(plan.getPlanRating()+ postDto.getRating());
                post.setLat(postDto.getLat());
                post.setLng(postDto.getLng());
                post.setLocation(postDto.getLocation());
                post.setDay(i);
                post.setOrdering(j);
                post.setMemo(postDto.getMemo());
                post.setPostManager(user);
                post.setPlanOf(plan);
                post.setClosed(false);

                if (plan.isPublished() != true) {
                    post.setClosed(true);
                }
                Post saved = postRepository.save(post);

                generatePostTags(saved, postDto.getTags());
            }
        }
    }

    private void setPostImageWithCategory(Post saved) {
        String temp = "";
        PostCategory category = saved.getCategory();
        switch (category.toString()) {
            case "FLIGHT":
                temp = "FLIGHT";
                break;
            case "DISH":
                temp = "DISH";
                break;
            case "SHOPPING":
                temp = "SHOPPING";
                break;
            case "HOTEL":
                temp = "HOTEL";
                break;
            case "TRAFFIC":
                temp = "TRAFFIC";
                break;
            case "ETC":
                temp = "ETC";
                break;
        }
        String uri = imageToDatUri(temp);
        saved.setPostImage(uri);
    }

    private String imageToDatUri(String keyword) {
        byte[] bytes = new byte[0];
        try {
            bytes = getClass().getResourceAsStream("/static/images/"+keyword + ".png").readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String dataUri = new String(Base64.getEncoder().encode(bytes));
        return dataUri;
    }


    private void generatePostTags(Post post, String tags) {
        String[] split = tags.split(",");
        for (String title : split) {
            Tag tag = new Tag();
            tag.setTagTitle(title);
            Tag saved = tagRepository.save(tag);

            PostTag postTag = new PostTag();
            postTag.setPost(post);
            postTag.setTag(tag);
            PostTag save = postTagRepository.save(postTag);

            post.getPostTagList().add(save);
        }
    }

    private void settingPostCategory(Plan plan, Post post, PostDto dto) {
        String category = dto.getCategory();
        switch (category) {
            case "FLIGHT":
                post.setCategory(PostCategory.FLIGHT);
                plan.setFlightTotalPrice(plan.getFlightTotalPrice()+dto.getPrice());
                break;
            case "DISH":
                post.setCategory(PostCategory.DISH);
                plan.setDishTotalPrice(plan.getDishTotalPrice()+dto.getPrice());
                break;
            case "SHOPPING":
                post.setCategory(PostCategory.SHOPPING);
                plan.setShoppingTotalPrice(plan.getShoppingTotalPrice()+dto.getPrice());
                break;
            case "HOTEL":
                post.setCategory(PostCategory.HOTEL);
                plan.setHotelTotalPrice(plan.getHotelTotalPrice()+dto.getPrice());
                break;
            case "TRAFFIC":
                post.setCategory(PostCategory.TRAFFIC);
                plan.setTrafficTotalPrice(plan.getTrafficTotalPrice()+dto.getPrice());
                break;
            case "ETC":
                post.setCategory(PostCategory.ETC);
                plan.setEtcTotalPrice(plan.getEtcTotalPrice()+dto.getPrice());
                break;
        }
    }

    private String generateOutputPrice(Long price) {
        if (price < 10000) {
            return price + "???";
        }
        String string = price.toString();
        string = string.substring(0, string.length()-4);
        return string+"??????";
        // 40000 = 4/0000 = 4??????
    }

    private Plan generatePlan(PlanDto planDto, Account user) {
        Plan plan = new Plan();
        plan.setPlanManager(user);
        plan.setLastModified(LocalDate.now());
        plan.setPlanTitle(planDto.getPlanTitle());
        if (planDto.getPlanImage() != null) {
            plan.setPlanImage(planDto.getPlanImage());
        } else {
            String defaultPlanImage = imageToDatUri("DefaultPlan");
            plan.setPlanImage(defaultPlanImage);
        }
        // image??? null?????? ??????????????? ????????? ?????????
        plan.setPublished(planDto.isPublished());
        plan.setStartDate(planDto.getStartDate());
        plan.setEndDate(planDto.getEndDate());
        //create planTags
        String planTag = planDto.getPlanTag();
        generatePlanTags(plan, planTag);

        return planRepository.save(plan);
    }

    private void generatePlanTags(Plan plan, String planTag) {
        String[] split = planTag.split(",");
        for (String title : split) {
            Tag tag = new Tag();
            tag.setTagTitle(title);
            Tag saved = tagRepository.save(tag);

            PlanTag newPlanTag = new PlanTag();
            newPlanTag.setPlan(plan);
            newPlanTag.setTag(saved);
            PlanTag made = planTagRepository.save(newPlanTag);
            //?????????
            plan.getPlanTagList().add(made);
        }
    }

    public PlanResponseDto createPlanResponse(Account account, Plan plan) {
        PlanResponseDto planResponseDto = modelMapper.map(plan, PlanResponseDto.class);
        AccountResponseDto accountResponseDto = createAccountResponseDto(account);
        planResponseDto.setAccountResponseDto(accountResponseDto);
        List<PlanTag> list = plan.getPlanTagList();
        for (PlanTag planTag : list) {
            PlanTagResponseDto planTagResponseDto = createPlanTagResponseDto(planTag);
            planResponseDto.getPlanTagResponseDtos().add(planTagResponseDto);
        }
        return planResponseDto;
    }

    private AccountResponseDto createAccountResponseDto(Account account) {
        return modelMapper.map(account,AccountResponseDto.class);
    }

    private PlanTagResponseDto createPlanTagResponseDto(PlanTag planTag) {
        PlanTagResponseDto planTagResponseDto = new PlanTagResponseDto();
        planTagResponseDto.setPlanTagTitle(planTag.getTag().getTagTitle());
        return planTagResponseDto;
    }

    public CollectionModel<EntityModel<PlanResponseDto>> findAllPlans() {
        List<Plan> plans = planRepository.findAllByPublishedOrderByLastModified(true);
        List<PlanResponseDto> planResponseDtos = new ArrayList<>();

        for (Plan plan : plans) {
            Long id = plan.getPlanManager().getId();
            Account account = accountRepository.findById(id).get();
            PlanResponseDto planResponse = createPlanResponse(account, plan);
            planResponseDtos.add(planResponse);
        }

        List<EntityModel<PlanResponseDto>> collect =
                planResponseDtos.stream().map(p -> PlanResource.modelOf(p)).collect(Collectors.toList());
        CollectionModel<EntityModel<PlanResponseDto>> result = CollectionModel.of(collect);
        return result;
    }

    public EntityModel<PlanWithPostResponseDto> getPlanWithPlanId(Long id) throws NotFoundException {
        Optional<Plan> byId = planRepository.findById(id);
        if (byId.isEmpty()) {
            throw new NotFoundException("?????? ??????????????? ???????????? ????????????.");
        }
        Plan plan = byId.get();
        List<Post> posts = postRepository.findAllByPlanOf(plan);
        //planWithPostResponseDto??? plan?????? ??????
        PlanWithPostResponseDto ppdto = createPlanWithPostResponseDto(plan);
        //planWithPostResponseDto??? post?????? setting
        for (Post post : posts) {
            PostForPlanResponseDto dto = createPostForPlanResponse(post);
            ppdto.getPostForPlanResponseDtos().add(dto);
        }
        EntityModel<PlanWithPostResponseDto> result = PlanWithPostResource.modelOf(ppdto);
        return result;
    }

    private PlanWithPostResponseDto createPlanWithPostResponseDto(Plan plan) {
        PlanWithPostResponseDto map = modelMapper.map(plan, PlanWithPostResponseDto.class);
        //planManager??????
        AccountResponseDto aDto = createAccountResponseDto(plan.getPlanManager());
        map.setAccountResponseDto(aDto);
        //planTag??????
        List<PlanTag> planTagList = planTagRepository.findAllByPlan(plan);
        for (PlanTag planTag : planTagList) {
            PlanTagResponseDto ptDto = createPlanTagResponseDto(planTag);
            map.getPlanTagResponseDtos().add(ptDto);
        }
        return map;
    }

    private PostForPlanResponseDto createPostForPlanResponse(Post post) {
        // postTagResponseDto?????? ????????? ?????? set
        PostForPlanResponseDto map = modelMapper.map(post, PostForPlanResponseDto.class);
        //?????? ???????????? ?????? ????????? -> postTagResponsDto
        List<PostTag> postTagList = postTagRepository.findAllByPost(post);
        for (PostTag postTag : postTagList) {
            PostTagResponseDto postTagResponseDto = new PostTagResponseDto();
            Tag tag = postTag.getTag();
            postTagResponseDto.setPostTagTitle(tag.getTagTitle());
            //PostForPlanResponseDto??? postTagResponseDto???????????? add
            map.getPostTagResponseDtoList().add(postTagResponseDto);
        }
        return map;
    }

    public Page<Plan> search(String keyword, long min, long max, Pageable pageable) {
        // ?????? ?????? x
        if (max == 0) {
            return null;
        }
        // ?????? ?????? o
        Page<Plan> planWithPrice = planRepository.findWithSearchCondContainsPrice(keyword, min, max, pageable);
        return planWithPrice;
    }

    public Plan updatePlan(PatchPlanRequestDto patchplanRequestDto, Account account) throws NotFoundException {
        Optional<Plan> foundPlanOptional = planRepository.findById(patchplanRequestDto.getPlanId());

        // ?????? ????????? ??????????????? ??????
        if (foundPlanOptional.isEmpty()) {
            throw new NotFoundException("??????????????? ?????? ??? ????????????.");
        }
        Plan plan = foundPlanOptional.get();
        Account user = accountRepository.findById(account.getId()).get();

        // ????????? ???????????? ???????????? ????????? ??????
        if (!user.equals(plan.getPlanManager())) {
            throw new AccessDeniedException("?????? ????????? ????????? ????????? ????????????.");
        }

        //Post, PostTag, Tag ??????
        deletePostTags(plan);
        postRepository.deleteAllByPlanOf(plan);

        //planTag, Tag ??????
        deletePlanTags(plan);

        //plan ?????? ??????
        updatePlanInfo(patchplanRequestDto, plan, user);

        //post??? ??? ???????????? ?????? ?????? (postTag)
        PostDto[][] postDtos = patchplanRequestDto.getPostDtos();
        generatePosts(plan, postDtos, user);

        // Plan ??? ??????/???3 ??? ??????
        settingOutputPlanTotalPrice(plan);
        settingTop3ListOfPlan(plan);
        settingPlanRating(plan);

        return plan;
    }

    private void deletePlanTags(Plan plan) {
        List<PlanTag> planTagList = plan.getPlanTagList();
        for (PlanTag plantag : planTagList) {
            Tag tag = plantag.getTag();
            tagRepository.delete(tag);
            plantag.setTag(null);
        }
        plan.getPlanTagList().clear();
        planTagRepository.deleteAllByPlan(plan);
    }

    private void deletePostTags(Plan plan) {
        List<Post> beforePostList = postRepository.findAllByPlanOf(plan);
        for (Post beforePost : beforePostList) {
            List<PostTag> postTagList = beforePost.getPostTagList();
            for (PostTag postTag : postTagList) {
                Tag tag = postTag.getTag();
                tagRepository.delete(tag);
                postTag.setTag(null);
            }
            beforePost.setPostTagList(null);

            // ??? post??? ?????? ???????????? ????????? ??? ???????????? list??? ??????
            //Optional<BookmarkPost> bookmarkPost = bookmarkPostRepository.findByPost(beforePost);
            List<BookmarkPost> bookmarkPostList = bookmarkPostRepository.findAllByPost(beforePost);
            for(BookmarkPost find : bookmarkPostList){
                //find.setPost(null); post ?????? ??? bookmarkpost??? ??????????????? 
                // bookmarkpost ??????. but, transaction??? ?????? ????????? ?????? ????????? ???
                bookmarkPostService.deleteBookmarkPost(find);
            }
            postTagRepository.deleteAllByPost(beforePost);
        }
    }

    private void updatePlanInfo(PatchPlanRequestDto patchplanRequestDto, Plan plan, Account user) {
        plan.setPlanTitle(patchplanRequestDto.getPlanTitle());
        plan.setLastModified(LocalDate.now());

        // ???????????? ????????? ?????? or ????????? ????????? ??? ????????? ????????? ??????
        if (patchplanRequestDto.getPlanImage() != null) {
            plan.setPlanImage(patchplanRequestDto.getPlanImage());
        } else {
            String defaultPlanImage = imageToDatUri("DefaultPlan");
            plan.setPlanImage(defaultPlanImage);
        }

        plan.setPublished(patchplanRequestDto.isPublished());
        plan.setStartDate(patchplanRequestDto.getStartDate());
        plan.setEndDate(patchplanRequestDto.getEndDate());

        plan.getTop3List().clear();
        plan.setTotalPrice(0L);
        plan.setFlightTotalPrice(0L);
        plan.setDishTotalPrice(0L);
        plan.setDishTotalPrice(0L);
        plan.setShoppingTotalPrice(0L);
        plan.setHotelTotalPrice(0L);
        plan.setTrafficTotalPrice(0L);
        plan.setEtcTotalPrice(0L);
        plan.setPlanRating(0F);

        // ???????????? ??????
        String planTag = patchplanRequestDto.getPlanTag();
        generatePlanTags(plan, planTag);
    }

    public PatchPlanResponseDto updatePlanResponse(Plan plan) {
        PatchPlanResponseDto planResponseDto = modelMapper.map(plan, PatchPlanResponseDto.class);

        List<PlanTag> list = plan.getPlanTagList();
        for (PlanTag planTag : list) {
            PlanTagResponseDto planTagResponseDto = createPlanTagResponseDto(planTag);
            planResponseDto.getPlanTagResponseDtos().add(planTagResponseDto);
        }

        List<Post> posts = postRepository.findAllByPlanOf(plan);
        for (Post post : posts) {
            PatchPostReponseDto patchPostReponseDto = modelMapper.map(post, PatchPostReponseDto.class);
            List<PostTag> postTags = post.getPostTagList();
            List<PostTagResponseDto> lists = new ArrayList<>();
            for (PostTag tag : postTags) {
                PostTagResponseDto postTagDto = patchPostTagResponseDto(tag);
                lists.add(postTagDto);
                patchPostReponseDto.getPostTagList().add(postTagDto);
            }
            patchPostReponseDto.setPostTagList(lists);
            planResponseDto.getPostResponseDtos().add(patchPostReponseDto);
        }
        return planResponseDto;
    }

    private PostTagResponseDto patchPostTagResponseDto(PostTag postTag) {
        PostTagResponseDto responseDto = new PostTagResponseDto();
        responseDto.setPostTagTitle(postTag.getTag().getTagTitle());
        return responseDto;
    }

    public void deletePlan(Long planId, Account account) throws NotFoundException {
        // ?????? ?????? ??????
        Optional<Plan> postOptional = planRepository.findById(planId);
        if (postOptional.isEmpty()) {
            throw new NotFoundException("?????? ??????????????? ???????????? ????????????.");
        }
        Plan plan = postOptional.get();

        //?????? ???????????? ????????? ???????????? ??????
        Account user = accountRepository.findById(account.getId()).get();
        if (!plan.getPlanManager().equals(user)) {
            throw new AccessDeniedException("???????????? ????????? ??? ????????????.");
        }

        //Post, PostTag, Tag ??????
        deletePostTags(plan);
        postRepository.deleteAllByPlanOf(plan);

        //Plan, planTag, Tag ??????
        deletePlanTags(plan);
        planRepository.delete(plan);
    }

    public Page<Plan> getMyClosedPlans(Account account, Pageable pageable) {
        Account user = accountRepository.findByUsername(account.getUsername());
        Page<Plan> plans = planRepository.findByPlanManagerAndPublished(user, false, pageable);
        return plans;
    }

    public Page<Plan> getMyPublishedPlans(Account account, Pageable pageable) {
        Account user = accountRepository.findByUsername(account.getUsername());
        Page<Plan> plans = planRepository.findByPlanManagerAndPublished(user, true, pageable);
        return plans;
    }

    public EntityModel<PlanResponseDto> addLinksWithCreate(EntityModel<PlanResponseDto> resultResource)  {
        resultResource.add(linkTo(PlanController.class).slash(resultResource.getContent().getId()).withSelfRel());
        resultResource.add(linkTo(PlanController.class).withRel("updatePlan"));
        resultResource.add(linkTo(PlanController.class).slash(resultResource.getContent().getId()).withRel("deletePlan"));
        resultResource.add(linkTo(PlanController.class).slash("mine").slash("closed").withRel("getMyClosedPlans"));
        resultResource.add(linkTo(PlanController.class).slash("mine").slash("published").withRel("getMyPublishedPlans"));
        return resultResource;
    }

    public PagedModel<EntityModel<PlanResponseDto>> addLinksWithClosedPlans(PagedModel<EntityModel<PlanResponseDto>> entityModels) {
        Collection<EntityModel<PlanResponseDto>> content = entityModels.getContent();
        for (EntityModel<PlanResponseDto> planResponseDtoEntityModel : content) {
            planResponseDtoEntityModel.add(linkTo(PlanController.class).slash(planResponseDtoEntityModel.getContent().getId()).withSelfRel());
            planResponseDtoEntityModel.add(linkTo(PlanController.class).slash(planResponseDtoEntityModel.getContent().getId()).withRel("deletePlan"));
            planResponseDtoEntityModel.add(linkTo(PlanController.class).withRel("updatePlan"));
        }
        entityModels.add(linkTo(PlanController.class).slash("mine").slash("published").withRel("getMyPublishedPlans"));
        entityModels.add(linkTo(PlanController.class).withRel("createPlan"));
        return entityModels;
    }

    public PagedModel<EntityModel<PlanResponseDto>> addLinksWithPublishedPlans(PagedModel<EntityModel<PlanResponseDto>> entityModels) {
        Collection<EntityModel<PlanResponseDto>> content = entityModels.getContent();
        for (EntityModel<PlanResponseDto> planResponseDtoEntityModel : content) {
            planResponseDtoEntityModel.add(linkTo(PlanController.class).slash(planResponseDtoEntityModel.getContent().getId()).withSelfRel());
            planResponseDtoEntityModel.add(linkTo(PlanController.class).slash(planResponseDtoEntityModel.getContent().getId()).withRel("deletePlan"));
            planResponseDtoEntityModel.add(linkTo(PlanController.class).withRel("updatePlan"));
        }
        entityModels.add(linkTo(PlanController.class).slash("mine").slash("closed").withRel("getMyClosedPlans"));
        entityModels.add(linkTo(PlanController.class).withRel("createPlan"));
        return entityModels;
    }

    public EntityModel<PlanWithPostResponseDto> addLinksWithGetPlan(EntityModel<PlanWithPostResponseDto> model) {
        model.add(linkTo(PlanController.class).withRel("createPlan"));
        model.add(linkTo(PlanController.class).slash(model.getContent().getId()).withRel("deletePlan"));
        model.add(linkTo(PlanController.class).slash("mine").slash("closed").withRel("getMyClosedPlans"));
        model.add(linkTo(PlanController.class).slash("mine").slash("published").withRel("getMyPublishedPlans"));
        model.add(linkTo(PlanController.class).withRel("updatePlan"));
        return model;
    }

    public PagedModel<EntityModel<PlanResponseDto>> addLinksWithSearch(PagedModel<EntityModel<PlanResponseDto>> model) {
        Collection<EntityModel<PlanResponseDto>> content = model.getContent();
        for (EntityModel<PlanResponseDto> planResponseDtoEntityModel : content) {
            planResponseDtoEntityModel.add(linkTo(PlanController.class).slash(planResponseDtoEntityModel.getContent().getId()).withSelfRel());
            planResponseDtoEntityModel.add(linkTo(PlanController.class).slash(planResponseDtoEntityModel.getContent().getId()).withRel("deletePlan"));
            planResponseDtoEntityModel.add(linkTo(PlanController.class).withRel("updatePlan"));
        }
        model.add(linkTo(PlanController.class).withRel("createPlan"));
        return model;
    }

    public EntityModel<PatchPlanResponseDto> addLinksPatchPlan(EntityModel<PatchPlanResponseDto> model) {
        model.add(linkTo(PlanController.class).slash(model.getContent().getId()).withRel("getPlan"));
        model.add(linkTo(PlanController.class).withRel("createPlan"));
        model.add(linkTo(PlanController.class).slash(model.getContent().getId()).withRel("deletePlan"));
        model.add(linkTo(PlanController.class).slash("mine").slash("closed").withRel("getMyClosedPlans"));
        model.add(linkTo(PlanController.class).slash("mine").slash("published").withRel("getMyPublishedPlans"));
        return model;
    }
}
  