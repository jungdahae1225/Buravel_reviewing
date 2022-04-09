package buravel.buravel.modules.account;

import buravel.buravel.modules.account.dtos.AccountDto;
import buravel.buravel.modules.account.dtos.AccountResponseDto;
import buravel.buravel.modules.account.dtos.AccountWithPlanDto;
import buravel.buravel.modules.account.dtos.EmailDto;
import buravel.buravel.modules.errors.ErrorResource;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import buravel.buravel.modules.account.validator.SignUpFormValidator;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final SignUpFormValidator validator;

    /**
     * 콜라비즈 적용.
     * 1. 이메일인증 버튼 클릭 -> @PostMapping("/emailVerification")으로 들어온다. 이메일 주소가 post 들어와야 한다.
     * 2. EmailDto에 넘어온 이메일 값 담는다.
     * 3. accountRepository.findById(account.getId());이걸로 이미 있는 이메일인가 확인 한다.
     * 4. 그 다음 버라벨의  @PostMapping("/emailCheckToken") 메서드를 여기서 써서 이메일 토큰을 보낸다. -> 이메일 인증번호 전송 완료
     * 5. 프론트에서 받은 토큰의 번호 넘겨줌
     * 6. 넘어온 토큰이 일치 하는지 확인
     * */

    @GetMapping("/emailVerification") //이메일 인증 버튼
    public ResponseEntity emailVerification(@CurrentUser Account account, @RequestParam String token){
        //1.이미 있는 이메일인가 확인
        Optional<Account> byId = accountRepository.findById(account.getId());
        if (byId.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        //2.토큰을 담아 dto에
        AccountResponseDto dto = accountService.emailVerification(byId.get(), token);
        if(dto == null){
            return ResponseEntity.badRequest().build();
        } // 인증번호 맞지 않음

        return ResponseEntity.ok(dto);
    }

    //generate new emailCheckToken & re-send token
    @PostMapping("/emailCheckToken")
    public ResponseEntity resendEmailCheckToken(@CurrentUser Account account) {
        accountService.reSendEmailCheckToken(account);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signUp") //회원가입 완료 버튼
    public ResponseEntity signUp(@RequestBody @Valid AccountDto accountDto, Errors errors) {
        if (errors.hasErrors()) {
            EntityModel<Errors> jsr303error = ErrorResource.modelOf(errors);
            return ResponseEntity.badRequest().body(jsr303error);
        }
        validator.validate(accountDto, errors);
        if (errors.hasErrors()) {
            EntityModel<Errors> customError = ErrorResource.modelOf(errors);
            return ResponseEntity.badRequest().body(customError);
        }
        Account account = accountService.processNewAccount(accountDto); //회원가입
        EntityModel<Account> accountResource = AccountResource.modelOf(account);
        return ResponseEntity.ok(accountResource);
    }

}