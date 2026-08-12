package com.campusgo.api.config;

import com.campusgo.domain.model.KnowledgeDocument;
import com.campusgo.domain.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 知识库种子数据初始化
 */
@Slf4j
@Component
@Order(200)
@RequiredArgsConstructor
public class KnowledgeBaseSeeder implements ApplicationRunner {

    private final KnowledgeDocumentRepository knowledgeDocumentRepo;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<KnowledgeDocument> existing = knowledgeDocumentRepo.findAll();
        if (!existing.isEmpty()) {
            log.info("Knowledge base already has {} documents, skipping seed", existing.size());
            return;
        }

        log.info("Seeding knowledge base with CampusGo platform knowledge...");

        seedPlatformKnowledge();
        seedTaskKnowledge();
        seedPointsKnowledge();
        seedVoucherKnowledge();
        seedWalletKnowledge();
        seedCheckinKnowledge();
        seedFaqKnowledge();

        log.info("Knowledge base seeded successfully");
    }

    private void seedPlatformKnowledge() {
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("CampusGo 平台介绍")
                .category("PLATFORM")
                .content("CampusGo 是一个校园跑腿互助平台，连接校园内的任务发布者和跑腿员。用户可以在平台上发布各种跑腿任务（如代取快递、代买餐饮、送文件等），也可以注册成为跑腿员接单赚钱。平台支持发布者模式和跑腿员模式切换。")
                .tags("平台,介绍,校园跑腿,互助")
                .build());
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("发布者与跑腿员角色")
                .category("PLATFORM")
                .content("发布者：可以发布任务，设置酬劳，等待跑腿员接单。跑腿员：需要完成校园卡认证后才能切换为跑腿员身份，跑腿员可以接单完成任务赚取酬劳。切换跑腿员需要信用分不低于400分。")
                .tags("发布者,跑腿员,角色,认证,信用分")
                .build());
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("校园卡认证流程")
                .category("PLATFORM")
                .content("要切换为跑腿员身份，需要先完成校园卡认证。在「我的」页面点击「校园卡认证」，上传学生证或校园卡照片，提交后等待审核。审核通过后即可切换为跑腿员接单。")
                .tags("校园卡,认证,跑腿员,审核")
                .build());
    }

    private void seedTaskKnowledge() {
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("任务发布指南")
                .category("TASK")
                .content("发布任务的步骤：1. 在首页点击「发布任务」或选择快捷发布；2. 选择任务类型（代取快递、代买物品、代办事务、拼单）；3. 填写任务标题、描述、取件地点、送达地点、期望完成时间；4. 设置任务酬劳；5. 确认信息并支付发布。发布后任务进入「待接单」状态，等待跑腿员接单。")
                .tags("发布任务,指南,步骤,类型")
                .build());
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("任务模式说明")
                .category("TASK")
                .content("平台支持四种任务模式：1. 普通任务 - 标准跑腿，一人承担费用；2. 拼单任务 - 多人凑单，费用分摊，目标人数2-10人；3. 紧急任务 - 加价50%，优先推送给跑腿员；4. 预约任务 - 指定时间，跑腿员可先占位再确认接单。")
                .tags("任务模式,普通,拼单,紧急,预约")
                .build());
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("任务状态说明")
                .category("TASK")
                .content("任务状态包括：待接单 - 等待跑腿员接单；已接单 - 跑腿员已接单，等待开始配送；配送中 - 跑腿员正在配送；待确认 - 跑腿员已送达，等待发布者确认；已完成 - 任务完成。如果发布者24小时未操作确认，系统将自动完成。")
                .tags("任务状态,接单,配送,完成,自动确认")
                .build());
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("跑腿员接单指南")
                .category("TASK")
                .content("跑腿员接单流程：1. 在「任务大厅」浏览可接任务；2. 点击感兴趣的任务查看详情；3. 点击「立即抢单」；4. 接单后开始配送；5. 到达后拍照确认送达；6. 等待发布者确认完成。跑腿员可以查看自己的接单记录和收益。")
                .tags("跑腿员,接单,指南,抢单,配送")
                .build());
    }

    private void seedPointsKnowledge() {
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("积分系统介绍")
                .category("POINTS")
                .content("CampusGo 积分系统是平台的虚拟货币体系。积分可以通过每日签到、完成任务等方式获得。积分可以在积分商城中兑换各种商品和优惠券。每次签到可获得5积分，连续签到天数越多，获得的奖励越丰厚。")
                .tags("积分,系统,签到,商城,兑换")
                .build());
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("积分商城使用指南")
                .category("POINTS")
                .content("在「我的」页面点击「积分商城」进入。积分商城提供各种商品兑换，包括优惠券、实物商品等。选择商品后点击「立即兑换」，系统会扣除相应积分，兑换成功后可在「我的券包」中查看。注意：商品库存有限，先到先得。")
                .tags("积分商城,兑换,商品,优惠券,券包")
                .build());
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("积分查看与历史记录")
                .category("POINTS")
                .content("用户可以在「我的」页面查看当前积分余额。点击积分余额可查看积分明细，包括收入（签到奖励、任务奖励等）和支出（商品兑换等）的完整记录。")
                .tags("积分,余额,明细,记录,查看")
                .build());
    }

    private void seedVoucherKnowledge() {
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("优惠券使用指南")
                .category("VOUCHER")
                .content("优惠券可在积分商城兑换获得。兑换后的优惠券会存放在「我的券包」中。在发布任务时可以选择使用优惠券来抵扣部分费用。每张优惠券有有效期，请在有效期内使用。")
                .tags("优惠券,券包,兑换,使用,有效期")
                .build());
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("我的券包说明")
                .category("VOUCHER")
                .content("在「我的」页面点击「我的券包」可以查看所有已兑换的优惠券。券包中会显示优惠券的面值、有效期和状态（未使用/已使用/已过期）。可以在发布任务时选择使用合适的优惠券。")
                .tags("券包,优惠券,查看,使用,状态")
                .build());
    }

    private void seedWalletKnowledge() {
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("钱包与充值指南")
                .category("WALLET")
                .content("钱包是 CampusGo 的资金管理工具。发布任务时需要将酬劳托管到平台钱包中。用户可以通过充值向钱包添加资金。在「我的」页面点击「钱包」可以查看余额、充值或查看交易记录。")
                .tags("钱包,充值,余额,资金,托管")
                .build());
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("提现操作指南")
                .category("WALLET")
                .content("跑腿员完成任务后，获得的酬劳会进入钱包余额。余额满10元即可提现。在「钱包」页面点击「提现」，绑定提现账户（支付宝或微信），输入提现金额提交即可。提现通常在1-3个工作日内到账。")
                .tags("提现,余额,跑腿员,酬劳,支付宝,微信")
                .build());
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("交易记录查看")
                .category("WALLET")
                .content("在钱包页面可以查看所有交易记录，包括充值、任务收入、提现等。每笔交易都记录有金额、时间、类型和状态，方便用户核对资金流水。")
                .tags("交易记录,流水,充值,收入,提现")
                .build());
    }

    private void seedCheckinKnowledge() {
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("每日签到指南")
                .category("POINTS")
                .content("每日签到是获取积分的主要方式之一。在首页点击「每日签到」即可完成签到，每次签到可获得5积分。连续签到7天可获得额外奖励积分。如果中断签到，连续天数会重置。记得每天来签到哦！")
                .tags("签到,每日,积分,连续签到,奖励")
                .build());
    }

    private void seedFaqKnowledge() {
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("常见问题 - 任务发布")
                .category("FAQ")
                .content("Q: 发布任务后多久有人接单？A: 通常在几分钟内就会有跑腿员接单，具体取决于任务酬劳、距离等因素。如果长时间无人接单，可以尝试加价或转为紧急任务。Q: 可以取消已发布的任务吗？A: 在任务被接单前可以取消。如果已被接单，需要联系跑腿员协商取消。")
                .tags("FAQ,常见问题,发布,接单,取消,加价")
                .build());
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("常见问题 - 积分与投诉")
                .category("FAQ")
                .content("Q: 积分会过期吗？A: 目前积分长期有效，不会过期。Q: 遇到服务问题怎么办？A: 可以在任务详情页点击「投诉」或「有异议」提交投诉，平台会介入处理。Q: 如何联系跑腿员或发布者？A: 在任务详情页点击「联系ta」可以发送消息沟通。")
                .tags("FAQ,积分,过期,投诉,联系,沟通")
                .build());
        knowledgeDocumentRepo.save(KnowledgeDocument.builder()
                .title("常见问题 - 账号与安全")
                .category("FAQ")
                .content("Q: 忘记密码怎么办？A: 在登录页面点击「忘记密码」，通过手机验证码重置密码。Q: 如何修改个人信息？A: 在「我的」页面点击头像或编辑按钮可以修改昵称等个人信息。Q: 账号安全问题？A: 建议使用强密码，不要将账号信息透露给他人。")
                .tags("FAQ,密码,账号,安全,个人信息,修改")
                .build());
    }
}