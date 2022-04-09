package buravel.buravel.modules.plan;

import buravel.buravel.modules.account.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;


public class PlanRepositoryExtensionImpl extends QuerydslRepositorySupport implements PlanRepositoryExtension {

    public PlanRepositoryExtensionImpl() {
        super(Plan.class);
    }

    // 연관관계에 있어서는 쿼리 , 카운트 쿼리 2방으로 최적화
    // elementCollection까지 한 방에 가져오는 방법은 마땅히 없는듯
    @Override
    public void findWithSearchCond(String keyword, Pageable pageable) {
        // 공개된 plan 중 제목 / 태그에 해당 키워드가 존재하는 plan 끌어오기
    }

    @Override
    public Page<Plan> findWithSearchCondContainsPrice(String keyword, long min, long max, Pageable pageable) {
        return null;
    }

    @Override
    public Plan findPlanSoon(Account account) {
        return null;
    }
}
