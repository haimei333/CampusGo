package com.campusgo.data.mock;

import com.campusgo.R;
import com.campusgo.domain.model.MallProduct;

import java.util.ArrayList;
import java.util.List;

/**
 * G01 积分商城演示数据
 */
public final class MockMallRepository {

    private MockMallRepository() {
    }

    public static List<MallProduct> all() {
        List<MallProduct> list = new ArrayList<>();
        list.add(new MallProduct("m1", "蜜雪冰城代金券", "5元代金券", "🍦", 300,
                MallProduct.Category.VOUCHER, R.drawable.bg_icon_accent));
        list.add(new MallProduct("m2", "麦当劳红包", "满30减10元", "🍔", 500,
                MallProduct.Category.VOUCHER, R.drawable.bg_icon_soft));
        list.add(new MallProduct("m3", "万达电影票", "2D/3D通兑券", "🎬", 800,
                MallProduct.Category.VOUCHER, R.drawable.bg_icon_blue));
        list.add(new MallProduct("m4", "瑞幸咖啡券", "生椰拿铁兑换券", "☕", 200,
                MallProduct.Category.VOUCHER, R.drawable.bg_icon_neutral));
        list.add(new MallProduct("m5", "校园文创帆布包", "限定校园主题", "🎒", 1500,
                MallProduct.Category.GOODS, R.drawable.bg_icon_soft));
        list.add(new MallProduct("m6", "316不锈钢保温杯", "500ml 长效保温", "🫖", 2000,
                MallProduct.Category.GOODS, R.drawable.bg_icon_neutral));
        list.add(new MallProduct("m7", "小米充电宝", "10000mAh快充", "🔋", 1200,
                MallProduct.Category.FLASH, R.drawable.bg_icon_accent, true, 1800));
        list.add(new MallProduct("m8", "蓝牙耳机", "降噪无线耳机", "🎧", 2500,
                MallProduct.Category.FLASH, R.drawable.bg_icon_blue, true, 3200));
        return list;
    }

    public static List<MallProduct> byCategory(MallProduct.Category category) {
        if (category == MallProduct.Category.ALL) {
            return all();
        }
        List<MallProduct> filtered = new ArrayList<>();
        for (MallProduct product : all()) {
            if (product.category == category) {
                filtered.add(product);
            }
        }
        return filtered;
    }
}
